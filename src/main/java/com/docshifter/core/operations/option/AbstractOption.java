package com.docshifter.core.operations.option;

import com.docshifter.core.config.entities.Node;
import com.docshifter.core.config.wrapper.ModuleWrapper;
import com.docshifter.core.exceptions.ConfigFileNotFoundException;
import com.docshifter.core.exceptions.EmptyOperationException;
import com.docshifter.core.exceptions.InputCorruptException;
import com.docshifter.core.exceptions.InputRejectedException;
import com.docshifter.core.exceptions.InvalidConfigException;
import com.docshifter.core.exceptions.UnsupportedInputFormatException;
import com.docshifter.core.logging.appenders.TaskMessageAppender;
import com.docshifter.core.operations.DirectoryHandling;
import com.docshifter.core.operations.FailureLevel;
import com.docshifter.core.operations.ModuleOperation;
import com.docshifter.core.operations.OperationParams;
import com.docshifter.core.operations.OptionParams;
import com.docshifter.core.operations.WrappedOptionParams;
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
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public abstract class AbstractOption<T> extends ModuleOperation {
	protected String option="Abstract Option";

	private Path rootSourcePath;

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

		Path inFilePath = parameters.getSourcePath();
		if (inFilePath != null && !Files.exists(inFilePath)) {
			log.error("{} does not exist!", inFilePath);
			parameters.setSuccess(TaskStatus.BAD_INPUT);
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
		if (inFilePath == null || !Files.isDirectory(inFilePath) || (directoryHandling = getDirectoryHandling()) == DirectoryHandling.AS_IS) {
			rootSourcePath = inFilePath;
			try {
				result = getResult();
				log.info("The option module returned the result: {}", result);
			} catch (InvalidConfigException ex) {
				log.error("The module indicated an invalid configuration", ex);
				parameters.setSuccess(TaskStatus.BAD_CONFIG);
				return parameters;
			} catch (ConfigFileNotFoundException ex) {
				log.error("The module indicated that it could not find a configuration file", ex);
				parameters.setSuccess(TaskStatus.BAD_CONFIG);
				return parameters;
			} catch (UnsupportedInputFormatException ex) {
				log.error("The module encountered an input format it cannot handle", ex);
				parameters.setSuccess(TaskStatus.BAD_INPUT);
				return parameters;
			} catch (InputRejectedException ex) {
				log.error("The module indicated that an input was rejected (possibly due to configuration)", ex);
				parameters.setSuccess(TaskStatus.BAD_INPUT);
				return parameters;
			} catch (InputCorruptException ex) {
				log.error("The module indicated bad/corrupt input", ex);
				parameters.setSuccess(TaskStatus.BAD_INPUT);
				return parameters;
			} catch (InterruptedException ex) {
				log.error("The task has reached its configured timeout value", ex);
				parameters.setSuccess(TaskStatus.TIMED_OUT);
				Thread.currentThread().interrupt();
				return parameters;
			} catch (TimeoutException ex) {
				log.error("The module performed an operation that timed out", ex);
				parameters.setSuccess(TaskStatus.TIMED_OUT);
				return parameters;
			} catch (Exception ex) {
				log.error("The module indicated a failure", ex);
				parameters.setSuccess(TaskStatus.FAILURE);
				return parameters;
			} finally {
				rootSourcePath = null;
				cleanup();
			}
		} else {
			// File groups are important to keep track of for the failure level that was set. Furthermore, groups
			// help several modules in differentiating which input is similar to one another or originated from a
			// single ancestor file (but was then split in multiple split files by the Splitter module for example).
			// Here we walk through all files in the directory hierarchy but keep them grouped according to their
			// parent path.
			Map<Path, List<Path>> groupedPaths;
			try (Stream<Path> stream = Files.walk(inFilePath)) {
				groupedPaths = stream.filter(Files::isRegularFile)
						.collect(Collectors.groupingBy(path -> inFilePath.relativize(path.getParent())));
			} catch (Exception ex) {
				log.error("Error while walking source path of operation: {}", inFilePath, ex);
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
			WrappedOptionParams mergedResult = WrappedOptionParams.fromOptionParams(parameters);
			String thisOperation = getClass().getName();
			// Handle the results for all groups
			OptionParams opResult = handleResult(groupedPathStream.map(groupedPath -> {
				Stream<Path> fileStream = directoryHandling == DirectoryHandling.PARALLEL_FOREACH ?
						groupedPath.getValue().parallelStream() : groupedPath.getValue().stream();
				// ...And the results for each file in the group
				return handleResult(fileStream.map(path -> {
					if (directoryHandling == DirectoryHandling.PARALLEL_FOREACH) {
						TaskMessageAppender.registerCurrentThread(task);
					}
					OptionParams fileOptionParams = new OptionParams(parameters);
					fileOptionParams.setSourcePath(path);
					OptionParams res = null;
					try {
						AbstractOption<?> op = getOption(thisOperation);
						if (op == this) {
							throw new IllegalStateException("A module should not be registered as a " +
									"singleton scoped bean in order to use automatic directory handling, " +
									"otherwise unintended behavior might happen.");
						}
						op.rootSourcePath = inFilePath;
						res = op.execute(nodes, fileOptionParams, failureLevel);
						// Never move over the result paths if the current module is of type RELEASE. We
						// never have any modules following such a module and moving over files might result
						// in the output being moved to somewhere the user doesn't want it (e.g. in case of
						// FSExport).
						if (res.isSuccess() &&
								(moduleWrapper == null
										|| !moduleWrapper.getType().equalsIgnoreCase("release"))) {
							Path newGroupedPath = folder.resolve(groupedPath.getKey());
							Files.createDirectories(newGroupedPath);
							Files.move(res.getResultPath(), newGroupedPath.resolve(res.getResultPath().getFileName()));
						}
					} catch (Exception ex) {
						log.error("Got an exception while trying to process a nested operation.", ex);
						if (res == null) {
							res = fileOptionParams;
						} else {
							log.error("...But the nested operation did appear to return a result. So we got " +
									"an exception while trying to move over the result path?");
						}
						res.setSuccess(TaskStatus.FAILURE);
					}
					mergedResult.wrap(res);
					return mergedResult;
				}), mergedResult, failureLevel.isHigherThan(FailureLevel.FILE)); // No need to process other
				// files if the failure level is higher.
			}), mergedResult, failureLevel.isHigherThan(FailureLevel.GROUP)); // No need to process other
			// groups if the failure level is higher.
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
			parameters.setResultPath(inFilePath);
		}
		// This is either the result of evaluating a single file or a module with AS_IS DirectoryHandling set. In
		// case of directory input (and FOREACH DirectoryHandling), the parameters will be wrapped and this singleton
		// Map will be combined with the results for other files.
		parameters.setSelectedNodes(Collections.singletonMap(parameters.getResultPath(), returnNodes));
		return parameters;
	}

	/**
	 * Collects results of a {@link Stream<OperationParams>} into a single {@link OperationParams}, depending on
	 * whether we need an early return (short-circuit) after failure or not.
	 * @param resultStream The {@link Stream<OperationParams>} of results to handle.
	 * @param mergedResult A
	 * @param shortCircuitOnFailure Whether to return early after a failure.
	 * @return
	 */
	private OptionParams handleResult(Stream<OptionParams> resultStream,
										 WrappedOptionParams mergedResult, boolean shortCircuitOnFailure) {
		if (shortCircuitOnFailure) {
			// TODO: interrupt running threads when we short-circuit?
			return resultStream.filter(res -> !res.isSuccess())
					.findAny()
					.orElse(mergedResult);
		}
		return resultStream.reduce((first, second) -> second).orElse(mergedResult);
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

	/**
	 * Returns whether the operation being executed is a nested one (when handling directory input). This can be
	 * useful to check if resulting files need to be named in a different way for example.
	 * @return
	 */
	protected final boolean isNestedOperation() {
		return !Objects.equals(rootSourcePath, operationParams.getSourcePath());
	}

	/**
	 * If we're in a nested operation, returns the source path (i.e. a directory) that the main operation received.
	 * Otherwise, this always equals the path returned by {@link OperationParams#getSourcePath()} of this instance's
	 * {@code operationParams}.
	 * @return The source path as received by the main operation.
	 */
	protected final Path getRootSourcePath() {
		return rootSourcePath;
	}
}
