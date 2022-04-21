package com.docshifter.core.operations.option;

import com.docshifter.core.config.entities.Node;
import com.docshifter.core.config.wrapper.ModuleWrapper;
import com.docshifter.core.exceptions.ConfigFileNotFoundException;
import com.docshifter.core.exceptions.EmptyOperationException;
import com.docshifter.core.exceptions.InputCorruptException;
import com.docshifter.core.exceptions.InvalidConfigException;
import com.docshifter.core.logging.appenders.TaskMessageAppender;
import com.docshifter.core.operations.DirectoryHandling;
import com.docshifter.core.operations.FailureLevel;
import com.docshifter.core.operations.ModuleOperation;
import com.docshifter.core.operations.OptionParams;
import com.docshifter.core.task.TaskStatus;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public abstract class AbstractOption<T> extends ModuleOperation {
	protected String option="Abstract Option";

	public String toString(){
		return option;
	}

	protected abstract T getResult() throws Exception;


	//overwrite to process the condition e.g. get the extension of the filename
	protected Map<String, String> processCondition(OptionParams parameters){
		String conditionsString=moduleWrapper.getString("optionJson");
		Map<String, String> conditions = null;

		
		if (conditionsString == null){
			return null;
		}

		log.debug("processing options json: {}", conditionsString);

		try {
			conditions = new ObjectMapper().readValue(conditionsString,  new TypeReference<Map<String,String>>() {} );
		} catch (JsonParseException | JsonMappingException e) {
			log.error("Invalid JSON specified in optionJson while processing conditions", e);
			parameters.setSuccess(TaskStatus.BAD_CONFIG);
		} catch (IOException e) {
			log.error("Could not open JSON file specified in optionJson while processing conditions", e);
			parameters.setSuccess(TaskStatus.FAILURE);
		} catch (Exception e) {
			log.error("An unexplainable exception occurred while processing conditions",e);
			parameters.setSuccess(TaskStatus.FAILURE);
		}

		return conditions;

	}
	
	
	public final OptionParams execute(List<Node> nodes, OptionParams parameters, ModuleWrapper moduleWrapper,
									  FailureLevel failureLevel) {
		this.moduleWrapper = moduleWrapper;
		return execute(nodes, parameters, failureLevel);
	}

	public final OptionParams execute(List<Node> nodes, OptionParams parameters) {
		return execute(nodes, parameters, FailureLevel.FILE);
	}

	public final OptionParams execute(List<Node> nodes, OptionParams parameters, FailureLevel failureLevel)
			throws IllegalArgumentException {
		log.info("initClassLoader execute");

		this.operationParams = parameters;

		boolean valid = fillInParameters();
		log.info("Executing option: {}", operation);
		log.info("Parameter result: {}", valid);
		log.info("with parameters: {}", moduleWrapper);
		

		if (!valid) {
			parameters.setSuccess(TaskStatus.FAILURE);
			return parameters;
		}

		Map<String, String> conditions = processCondition(parameters);
		if (conditions == null) {
			if (parameters.isSuccess()) {
				log.error("Conditions were set to null and no explicit failure was specified so will assume " +
						"generic failure while processing conditions.");
				parameters.setSuccess(TaskStatus.FAILURE);
			}
			return parameters;
		}

		log.info("initClassLoader Option execute");

		T result;
		DirectoryHandling directoryHandling;
		if (!Files.isDirectory(operationParams.getSourcePath()) || (directoryHandling = getDirectoryHandling()) == DirectoryHandling.AS_IS) {
			try {
				result = getResult();
				log.info("The option module returned the result: {}", result);
			} catch (InvalidConfigException | ConfigFileNotFoundException ex) {
				log.error("The module indicated an invalid configuration", ex);
				parameters.setSuccess(TaskStatus.BAD_CONFIG);
				return parameters;
			} catch (InputCorruptException ex) {
				log.error("The module indicated bad input", ex);
				parameters.setSuccess(TaskStatus.BAD_INPUT);
				return parameters;
			} catch (Exception ex) {
				log.error("The module indicated a failure", ex);
				parameters.setSuccess(TaskStatus.FAILURE);
				return parameters;
			} finally {
				cleanup();
			}
		} else {
			Map<Path, List<Path>> groupedPaths;
			try (Stream<Path> stream = Files.walk(operationParams.getSourcePath())) {
				groupedPaths = stream.filter(Files::isRegularFile)
						.collect(Collectors.groupingBy(path -> operationParams.getSourcePath().relativize(path.getParent())));
			} catch (Exception ex) {
				log.error("Error while walking source path of operation: {}", operationParams.getSourcePath(), ex);
				parameters.setSuccess(TaskStatus.FAILURE);
				return parameters;
			}

			if (groupedPaths.isEmpty()) {
				log.error("Got a directory as input, but it seems to be empty!");
				parameters.setSuccess(TaskStatus.BAD_INPUT);
				return parameters;
			}

			Stream<Map.Entry<Path, List<Path>>> groupedPathStream =
					directoryHandling == DirectoryHandling.PARALLEL_FOREACH ?
							groupedPaths.entrySet().parallelStream() : groupedPaths.entrySet().stream();
			Path folder = task.getWorkFolder().getNewFolderPath();
			AtomicReference<OptionParams> mergedResult = new AtomicReference<>(parameters);
			String thisOperation = getClass().getName();
			OptionParams opResult = handleResult(groupedPathStream.map(groupedPath -> {
				Stream<Path> fileStream = directoryHandling == DirectoryHandling.PARALLEL_FOREACH ?
						groupedPath.getValue().parallelStream() : groupedPath.getValue().stream();
				return handleResult(fileStream.map(path -> {
					if (directoryHandling == DirectoryHandling.PARALLEL_FOREACH) {
						TaskMessageAppender.registerCurrentThread(task);
					}
					OptionParams fileOptionParams = (OptionParams) parameters.clone();
					fileOptionParams.setSourcePath(path);
					OptionParams res;
					try {
						res = getOption(thisOperation).execute(nodes, fileOptionParams, failureLevel);
						if (res.isSuccess()) {
							Files.move(res.getResultPath(), folder.resolve(groupedPath.getKey()).resolve(res.getResultPath().getFileName()));
						}
					} catch (Exception ex) {
						return mergedResult.updateAndGet(curr -> (OptionParams) curr.merge(TaskStatus.FAILURE));
					}
					return mergedResult.updateAndGet(curr -> (OptionParams) curr.merge(res));
				}), mergedResult, failureLevel != FailureLevel.FILE);
			}), mergedResult, failureLevel != FailureLevel.FILE && failureLevel != FailureLevel.GROUP);
			if (opResult.isSuccess()) {
				opResult.setResultPath(folder);
			}
			return opResult;
		}

		Set<String> nodeIds = new HashSet<>();
		Set<Node> returnNodes = new LinkedHashSet<>();

		for (String condition : conditions.keySet()) {
			log.info("Condition: {}", condition);
			if (condition.equalsIgnoreCase("all")) {
				nodeIds.add(conditions.get(condition));
			} else if (!condition.equalsIgnoreCase("default")) {
				String[] breakFirst = null;
				String[] conditionArray;
				if (!condition.contains("||")) {
					conditionArray = new String[] {condition};
				}
				else {
					breakFirst = condition.split(Pattern.quote("'"));
					conditionArray = breakFirst[1].split(Pattern.quote("||"));
				}
				for (String aCondition : conditionArray) {
					if (!aCondition.startsWith("#result")) {
						aCondition = breakFirst[0] + "'" + aCondition + "'";
					}
				if (evaluateExpression(result, aCondition)) {
						log.debug("Match found adding node");
						nodeIds.add(conditions.get(condition));
					} else {
						log.debug("Expression did not match not adding node");
					}
				}
			}

		}
		if (nodeIds.isEmpty()) {
			if (conditions.containsKey("default")) {
				log.info("result does not match any of the configured options, using default.");
				nodeIds.add(conditions.get("default"));
			} else {
				log.error("result does not match any of the configured options and no default is set.");
				parameters.setSuccess(TaskStatus.BAD_CONFIG);
				return parameters;
			}
		}

		log.debug("looping nodes list " + Arrays.toString(nodes.toArray()));
		for (Node cn : nodes) {


			log.debug("checking if the list contains node {}", cn.getModuleConfiguration().getName());
			if (nodeIds.contains(cn.getModuleConfiguration().getName())) {
				log.debug("node found adding to returns {}", cn.getModuleConfiguration().getName());
				returnNodes.add(cn);
			}
		}

		parameters.setSuccess(TaskStatus.SUCCESS);
		if (parameters.getResultPath() == null) {
			parameters.setResultPath(parameters.getSourcePath());
		}
		parameters.setSelectedNodes(Collections.singletonMap(parameters.getResultPath(), returnNodes));
		return parameters;
	}

	private OptionParams handleResult(Stream<OptionParams> resultStream,
										 AtomicReference<OptionParams> mergedResult, boolean shortCircuitOnFailure) {
		if (shortCircuitOnFailure) {
			// TODO: interrupt running threads when we short-circuit?
			return resultStream.filter(res -> !res.isSuccess())
					.findAny()
					.orElse(mergedResult.get());
		}
		return resultStream.reduce((first, second) -> second).orElse(mergedResult.get());
	}

	protected boolean evaluateExpression(T result, String condition) {

		if (result instanceof String) {
			result = (T)((String)result).toUpperCase();
		}

		StandardEvaluationContext context = new StandardEvaluationContext();
		context.setVariable("RESULT", result);

		condition = condition.toUpperCase();

		ExpressionParser parser = new SpelExpressionParser();
		Expression exp = parser.parseExpression(condition);

		return (Boolean)exp.getValue(context);
	}


	public static AbstractOption<?> getOption(String op) throws EmptyOperationException {
		ModuleOperation operation = ModuleOperation.getModuleOperation(op);

		if (operation instanceof AbstractOption) {
			return (AbstractOption<?>) operation;
		} else {
			log.error("Incorrect operation, please check your configuration");
			throw new EmptyOperationException();
		}
	}
}
