package com.docshifter.core.operations.option;

import com.docshifter.core.config.entities.Node;
import com.docshifter.core.config.wrapper.ModuleWrapper;
import com.docshifter.core.exceptions.EmptyOperationException;
import com.docshifter.core.operations.ModuleOperation;
import com.docshifter.core.operations.OptionParams;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public abstract class AbstractOption<T> extends ModuleOperation {
	protected String option="Abstract Option";

	private static final Logger logger = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

	public String toString(){
		return option;
	}

	abstract protected T getResult();


	//overwrite to process the condition e.g. get the extension of the filename
	protected Map<String, String> processCondition(){
		String conditionsString=moduleWrapper.getString("optionJson");
		Map<String, String> conditions = null;

		
		if (conditionsString == null){
			return null;
		}

		logger.debug("processing options json: " + conditionsString, null);

		try {
			conditions = new ObjectMapper().readValue(conditionsString,  new TypeReference<Map<String,String>>() {} );
		} catch (JsonParseException e) {
			logger.error("Invalid JSON", e);
		} catch (JsonMappingException e) {
			logger.error("Invalid JSON", e);
		} catch (IOException e) {
			logger.error("Could not open JSON file", e);
		} catch (Exception e) {
			logger.error("An unexplainable exception occured",e);
		}

		return conditions;

	}
	
	
	public OptionParams execute(List<Node> nodes, OptionParams parameters, ModuleWrapper moduleWrapper) {
		this.moduleWrapper = moduleWrapper;
		return execute(nodes, parameters);
	}

	public OptionParams execute(List<Node> nodes, OptionParams parameters)
			throws IllegalArgumentException {
		logger.info("initClassLoader execute", null);

		this.operationParams = parameters;

		boolean valid = fillInParameters();
		logger.info("Executing option: " + operation);
		logger.info("Parameter result: " + valid);
		logger.info("with parameters: " + moduleWrapper);
		
		
		if (valid) {


			Map<String, String> conditions = processCondition();

			if (conditions == null) {
				logger.warn("Conditions were set to null", null);
				return null;
			}

			Set<String> nodeIds = new HashSet<>();
			Set<Node> returnNodes = new HashSet<>();


			logger.info("initClassLoader Option execute", null);
			T result = this.getResult();

			logger.info("The option module returned the result: " + result, null);


			for (String condition : conditions.keySet()) {
				logger.info("Condition: " + condition, null);
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
							logger.debug("Match found adding node", null);
							nodeIds.add(conditions.get(condition));
						} else {
							logger.debug("Expression did not match not adding node", null);
						}
					}
				}

			}
			if (nodeIds.isEmpty()) {
				if (conditions.containsKey("default")) {
					logger.info("result does not match any of the configured options using default.", null);
					nodeIds.add(conditions.get("default"));
				} else {
					logger.warn("result does not match any of the configured options and no default is set.", null);
					return null;
				}
			}
			
			logger.debug("looping nodes list " + Arrays.toString(nodes.toArray()), null);
			for (Node cn : nodes) {


				logger.debug("checking if the list contains node " + cn.getModuleConfiguration().getName(), null);
				if (nodeIds.contains(cn.getModuleConfiguration().getName())) {
					logger.debug("node found adding to returns " + cn.getModuleConfiguration().getName(), null);
					returnNodes.add(cn);
				}
			}

			parameters.setSuccess(true);
			parameters.setSelectedNodes(returnNodes);
			if (parameters.getResultPath() == null) {
				parameters.setResultPath(parameters.getSourcePath());
			}
			return parameters;
		}
		parameters.setSuccess(false);
		return parameters;
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


	public static AbstractOption getOption(String op) throws EmptyOperationException {
		ModuleOperation operation = ModuleOperation.getModuleOperation(op);

		if (operation instanceof AbstractOption) {
			return (AbstractOption) operation;
		} else {
			logger.error("Incorrect operation, please check your configuration", null);
			throw new EmptyOperationException();
		}
	}
}
