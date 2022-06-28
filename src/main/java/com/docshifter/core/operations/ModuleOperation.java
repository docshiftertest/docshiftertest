package com.docshifter.core.operations;

import com.docshifter.core.config.wrapper.ModuleWrapper;
import com.docshifter.core.exceptions.EmptyOperationException;
import com.docshifter.core.task.Task;
import com.docshifter.core.operations.annotations.ModuleParam;
import com.docshifter.core.utils.ModuleClassLoader;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.log4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by michiel.vandriessche on 15/03/17.
 */
public abstract class ModuleOperation {

	private static final Logger logger = Logger.getLogger(ModuleOperation.class);

	private static enum ParamType {
		BOOLEAN,
		BYTE,
		DOUBLE,
		FLOAT,
		INTEGER,
		LONG,
		SHORT,
		STRING,
		UNKNOWN
	};
	public static final String MP_FILE_INDICATOR = "mp_file:";
	protected String operation = "Abstract Operation";
	public Task task;
	protected ModuleWrapper moduleWrapper;
	protected OperationParams operationParams;

	protected boolean fillInParameters() {

		//WHEN FAULT => add fault to task.addMessage()
		Class<?> calledClass = this.getClass();
		HashMap<String, Field> moduleParamFieldMap = new HashMap<>();

		Map<String, Object> valuesMap;
		
		// Get all the parameters into the valuesMap
		// First, by default, take all the Parameters set on the operationParams
		valuesMap = operationParams.getParameters();
		if (moduleWrapper != null) {
			// if we have a Module Wrapper then what's in its ParamMap gets added (or overridden) in the valuesMap 
			valuesMap.putAll( moduleWrapper.getParamMap());
		}

		boolean access_old;
		Field field;
		String fieldName;

		// Get the class (private and public) Fields and put them in the module param field map
		// if they have a module param annotation defined 
		for (Field fld : calledClass.getDeclaredFields()) {
			Optional<ModuleParam> modder = extractModuleParamAnnotation(fld.getDeclaredAnnotations());
			if (modder.isPresent()) {
				moduleParamFieldMap.put(fld.getName(), fld);
			}
		}

		//Match and fill in ModuleParam
		for (Map.Entry<String, Field> entry : moduleParamFieldMap.entrySet()) {

			//Select field + select fieldname to set
			field = entry.getValue();
			logger.trace("field value = " + field);
			fieldName = ((ModuleParam) (field.getDeclaredAnnotations()[0])).value();
			logger.trace("field name = " + fieldName);
			if (fieldName.isEmpty()) {
				fieldName = entry.getKey();
				logger.trace("fieldname is empty. Newname = " + fieldName);
			}

			ModuleParam modder = extractModuleParamAnnotation(field.getDeclaredAnnotations()).orElseThrow(NullPointerException::new);

			boolean empty = false;
			boolean required = modder.required();
			boolean supportPlaceholder = modder.supportPlaceholder();
			Object moduleValue = valuesMap.get(fieldName);

			logger.trace("Field Type is: " + field.getType().getName());
			ParamType paramType = parseParamType(field);
			try {
				if (StringUtils.isBlank(String.valueOf(moduleValue))) {
					logger.trace("Setting empty to true... field.getType(): " + field.getType() + " and moduleValue: " + String.valueOf(moduleValue));
					empty = true;
				}
			}
			catch (ClassCastException cce) {
				logger.error("Field with name: " + fieldName + " is marked as Type String"
						+ " but trying to cast the value to a String gave a ClassCastException." 
						+ " Check your ModuleParams for type consistency, particularly if"
						+ " using chained parameters!");
			}

			if ((moduleValue == null || empty) && required) {
				if (StringUtils.isNotBlank((modder.defaultValue()))) {
					moduleValue = modder.defaultValue();
					logger.info("Required Moduleparameter '" + fieldName + "' in " + calledClass + " was not set. The default value (" + moduleValue + ") was used.");
				}
				else if (paramType == ParamType.BOOLEAN) {
					moduleValue = false;
				}
				else {
					logger.error("Required Moduleparameter '" + fieldName + "' has not been set for usage in " + calledClass);
					return false;
				}
			}

			access_old = field.isAccessible();
			field.setAccessible(true);
			String defaultValue = modder.defaultValue();
			if (moduleValue == null && StringUtils.isNotBlank(defaultValue)) {
				moduleValue = defaultValue;
			}
			setModuleValue(paramType, field, moduleValue, supportPlaceholder);
			field.setAccessible(access_old);
		}

		return true;
	}

	/**
	 * Determine the ParamType as an Enum value for the field's type
	 * @param field
	 * @return ParamType the type of parameter e.g. STRING, INTEGER, LONG, BOOLEAN etc.
	 */
	private ParamType parseParamType(Field field) {
		logger.debug("Parsing the param type for field: " + field.toString());
		switch (field.getType().getSimpleName().toLowerCase()) {
			case "int":
			case "integer":
			case "java.lang.integer":
			case "java.lang.int":
				return ParamType.INTEGER;
			case "java.lang.boolean":
			case "boolean":
				return ParamType.BOOLEAN;
			case "java.lang.long":
			case "long":
				return ParamType.LONG;
			case "java.lang.double":
			case "double":
				return ParamType.DOUBLE;
			case "java.lang.float":
			case "float":
				return ParamType.FLOAT;
			case "java.lang.byte":
			case "byte":
				return ParamType.BYTE;
			case "java.lang.short":
			case "short":
				return ParamType.SHORT;
			case "java.lang.string":
			case "string":
				return ParamType.STRING;
			default:
				logger.warn("Returning ParamType of UNKNOWN!");
				return ParamType.UNKNOWN;
		}
	}

	/**
	 * Factored-out code to set the Module Value to the specified value
	 * @param paramType the type of field it is (STRING, INTEGER, BYTE ...)
	 * @param field The field whose value we want to set
	 * @param moduleValue The value to set, as an Object
	 * @param supportPlaceholder boolean whether we support placeholders or not ${something}
	 * @return true we set a value, false we hit a problem or had no value to set
	 */
	private boolean setModuleValue(ParamType paramType, Field field, Object moduleValue, boolean supportPlaceholder) {
		try {
			logger.trace("modulevalue is === " + moduleValue);
			if (moduleValue == null) {
				if (paramType == ParamType.BOOLEAN) {
					field.set(this, false);
				}
				else {
					field.set(this, null);
				}
				return false;
			}
			String moduleValueStr = moduleValue.toString();
			if (supportPlaceholder) {
				moduleValueStr = processPlaceHolders(task, moduleValueStr);
			}
			switch (paramType) {
				case INTEGER:
					if (moduleValue instanceof Integer) {
						field.set(this, moduleValue);
					} 
					else if (StringUtils.isNotBlank(moduleValueStr)) {
						field.set(this, Integer.parseInt(moduleValueStr));
					}
					break;
				case BOOLEAN:
					if (moduleValue instanceof Boolean) {
						field.set(this, moduleValue);
					} 
					else if (StringUtils.isNotBlank(moduleValueStr)) {
						field.set(this, Boolean.parseBoolean(moduleValueStr));
					}
					break;
				case LONG:
					if (moduleValue instanceof Long) {
						field.set(this, moduleValue);
					} 
					else if (StringUtils.isNotBlank(moduleValueStr)) {
						field.set(this, Long.parseLong(moduleValueStr));
					}
					break;
				case DOUBLE:
					if (moduleValue instanceof Double) {
						field.set(this, moduleValue);
					} 
					else if (StringUtils.isNotBlank(moduleValueStr)) {
						field.set(this, Double.parseDouble(moduleValueStr));
					}
					break;
				case FLOAT:
					if (moduleValue instanceof Float) {
						field.set(this, moduleValue);
					} 
					else if (StringUtils.isNotBlank(moduleValueStr)) {
						field.set(this, Float.parseFloat(moduleValueStr));
					}
					break;
				case BYTE:
					if (moduleValue instanceof Byte) {
						field.set(this, moduleValue);
					} 
					else if (StringUtils.isNotBlank(moduleValueStr)) {
						field.set(this, Byte.parseByte(moduleValueStr));
					}
					break;
				case SHORT:
					if (moduleValue instanceof Short) {
						field.set(this, moduleValue);
					} 
					else if (StringUtils.isNotBlank(moduleValueStr)) {
						field.set(this, Short.parseShort(moduleValueStr));
					}
					break;
				case STRING:
					field.set(this, moduleValueStr);
					break;
				case UNKNOWN:
				default:
					logger.warn("Got an UNKNOWN type: [" + field.getType().getSimpleName() + "] for field: " + field + ". Setting value to: " + moduleValue);
					field.set(this, moduleValue);
					break;
			}
		} catch (IllegalAccessException ex) {
			logger.error("Illegal access of field", ex);
			return false;
		} catch (IllegalArgumentException ex) {
			logger.error("Module parameter doesn't match the type of fieldname '" + field.getName() + "' type " + field.getType().getSimpleName().toLowerCase(), ex);
			return false;
		} catch (Exception ex) {
			logger.error("Something went wrong while filling Moduleparameters", ex);
			return false;
		}
		return true;
	}

	/**
	 * Extracts a ModuleParam Annotation, if there is one, out of an Annotation array
	 * @param annotations An array of Annotations
	 * @return Optionally a ModuleParam Annotation, otherwise empty
	 */
	private Optional<ModuleParam> extractModuleParamAnnotation(Annotation[] annotations) {
		for (Annotation annie : annotations) {
			if (annie instanceof ModuleParam) {
				return Optional.of((ModuleParam) annie);
			}
		}
		return Optional.empty();
	}

	public static String processPlaceHolders(Task task, String text) {
		logger.trace("Trying to process placeholders for: " + text);
		Map<String, Object> substituteData = new HashMap<>();
		if (task.getData() != null) {
			for (Map.Entry<String, Object> entry : task.getData().entrySet()) {
				if (entry.getValue() instanceof Object[]) {
					String value = Arrays.toString((Object[]) entry.getValue()).replaceAll("\\[", "").replaceAll("\\]", "");
					substituteData.put(entry.getKey(), value);
				}
				else if (entry.getValue() instanceof List) {
					Map<String, List<Object>> arrayified = new HashMap<>();
					for (Object obj : ((List) entry.getValue())) {
						if (obj instanceof Map) {
							final Map<String, Object> objectMap = (Map<String, Object>) obj;
							for (Map.Entry<String, Object> subEntry : objectMap.entrySet()) {
								arrayified.putIfAbsent(subEntry.getKey(), new ArrayList<>());
								arrayified.get(subEntry.getKey()).add(subEntry.getValue());
							}
						}
						else {
							arrayified.putIfAbsent(entry.getKey(), ((List<Object>) entry.getValue()));
						}
					}
					for (Map.Entry<String, List<Object>> arrayifiedEntry : arrayified.entrySet()) {
						String value = Arrays.toString(arrayifiedEntry.getValue().toArray()).replaceAll("\\[", "").replaceAll("\\]", "");
						substituteData.put(arrayifiedEntry.getKey(), value);
					}
				}
				else {
					substituteData.put(entry.getKey(), entry.getValue());
				}
			}
		}
		StringSubstitutor sub = new StringSubstitutor(substituteData);
		text = sub.replace(text);

		// Replace all placeholders that were not found
		text = text.replaceAll("\\$\\{.+}", "");
		logger.trace("result after substitution: " + text);
		if (text.toLowerCase().startsWith(MP_FILE_INDICATOR)) {
			// If the value starts 'mp_file:' indicates we should use a file from the multipart request
			text = text.substring(MP_FILE_INDICATOR.length());
			if (CollectionUtils.isNotEmpty(task.getConfigFilesList())) {
				final String finalText = text;
				text = task.getConfigFilesList().stream().filter(file -> file.endsWith(finalText))
						.findFirst().orElse("");
			}
		}
		return text;
	}

	protected static ModuleOperation getModuleOperation(String opClassName) throws EmptyOperationException {
		ModuleOperation operation = null;
		if (opClassName == null) {
			logger.error("Tried to access an invalid operation");
			throw new EmptyOperationException();
		}

		ModuleClassLoader classLoader;
		Object obj;
		try {
			classLoader = ModuleClassLoader.getInstance();
			logger.debug("Got a class loader, now calling getSpringBean for opClassName: " + opClassName);
			obj = classLoader.getSpringBean(opClassName);
			if (obj == null) {
			    obj = classLoader.getClassObject(opClassName);
			}
		} catch (InstantiationException insE) {
			logger.warn("Failed to instantiate ModuleClassLoader. Probably running test. Trying to use standard classloader for opClassName: " + opClassName, null);
			try {
				Class<?> opClass=Class.forName(opClassName, true, ModuleOperation.class.getClassLoader());
				obj=opClass.getDeclaredConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | 
					ClassNotFoundException | IllegalArgumentException |
					InvocationTargetException | NoSuchMethodException |
					SecurityException exc1) {
				throw new EmptyOperationException(exc1);
			}
		} catch (Exception exc) {
			throw new EmptyOperationException(exc);
		}

		if (obj == null) {
			throw new EmptyOperationException("Module Loader returned null");
		} else if (obj instanceof ModuleOperation) {
			operation = (ModuleOperation) obj;
		} else {
			logger.error("Incorrect operation, please check your configuration");
			throw new EmptyOperationException();
		}

		return operation;
	}

    /**
     * Makes sure a(n) (list of) int range(s) is(are) valid
     * @param param String like 4,5-10,12,13,19 (valid)
     *                     or 5*6;9;i  (invalid)
     * @return Boolean true if valid
     */
	public static Boolean isValidIntRangeInput(String param) {
        Pattern reValid = Pattern.compile(
            "# Validate comma separated integers/integer ranges.\n" +
            "^             # Anchor to start of string.         \n" +
            "[0-9]+        # Integer of 1st value (required).   \n" +
            "(?:           # Range for 1st value (optional).    \n" +
            "  -           # Dash separates range integer.      \n" +
            "  [0-9]+      # Range integer of 1st value.        \n" +
            ")?            # Range for 1st value (optional).    \n" +
            "(?:           # Zero or more additional values.    \n" +
            "  ,           # Comma separates additional values. \n" +
            "  [0-9]+      # Integer of extra value (required). \n" +
            "  (?:         # Range for extra value (optional).  \n" +
            "    -         # Dash separates range integer.      \n" +
            "    [0-9]+    # Range integer of extra value.      \n" +
            "  )?          # Range for extra value (optional).  \n" +
            ")*            # Zero or more additional values.    \n" +
            "$             # Anchor to end of string.           ", 
            Pattern.COMMENTS);
        Matcher mat = reValid.matcher(param);
        return mat.matches();
    }

    /**
     * Parses a String of (pages), (page) ranges (could be any list of ranges, lines for e.g.)
     * like 1,3-4,10,11-20 and converts to a List of int arrays
     * of the start and end of each comma-separated range..so in this case:
     * [1,1], [3, 4], [10, 10], [11, 20] 
     * @param param String representing a list of (page) ranges or single (page)s
     * @return List of int[]s
     */
    public static List<int[]> parseIntRanges(String param) {
    	List<int[]> result = new ArrayList<>();
    	if (!isValidIntRangeInput(param)) {
    		logger.warn("Bad range specified: [" + param + "]");
    	}
    	else {
    		Pattern reNextVal = Pattern.compile(
    			"# extract next integers/integer range value.    \n" +
    			"([0-9]+)      # $1: 1st integer (Base).         \n" +
    			"(?:           # Range for value (optional).     \n" +
    			"  -           # Dash separates range integer.   \n" +
    			"  ([0-9]+)    # $2: 2nd integer (Range)         \n" +
    			")?            # Range for value (optional). \n" +
    			"(?:,|$)       # End on comma or string end.", 
    			Pattern.COMMENTS);
    		Matcher mat = reNextVal.matcher(param);
    		while (mat.find()) {
    			int[] startAndEnd = new int[2];
    			startAndEnd[0] = Integer.parseInt(mat.group(1));
    			startAndEnd[1] = startAndEnd[0];
    			if (mat.group(2) != null) {
    				startAndEnd[1] = Integer.parseInt(mat.group(2));
    			}
    			result.add(startAndEnd);
    		}
    	}
    	return result;
    }

	/**
	 * The {@link DirectoryHandling} treatment this operation expects to receive. Defaults to
	 * {@link DirectoryHandling#PARALLEL_FOREACH}, but can be overridden by operations to indicate they'd like to do
	 * something different.
	 * @return
	 */
	public DirectoryHandling getDirectoryHandling() {
		return DirectoryHandling.PARALLEL_FOREACH;
	}

	private final Set<AutoCloseable> closeables = new LinkedHashSet<>();

	/**
	 * Add a {@link java.io.Closeable} to track. Any ones tracked here will be automatically closed and cleaned up
	 * after the execution of this operation ends (whether it succeeded or failed). It is still recommended to use
	 * try-with-resources and to close resources as early as possible, but this mechanism can be used if you're sure (or
	 * unsure) that a specific resource needs to stay open for pretty much the rest of the operation's execution
	 * lifetime.
	 * @param closeable A {@link java.io.Closeable} to track.
	 * @return The provided {@link java.io.Closeable}. Useful if you're creating it and want to immediately assign it
	 * to a variable for further use.
	 * @param <T> The specific type of the {@link java.io.Closeable}.
	 */
	protected final <T extends AutoCloseable> T trackCloseable(T closeable) {
		closeables.add(closeable);
		return closeable;
	}

	protected final void cleanup() {
		for (Iterator<AutoCloseable> it = closeables.iterator(); it.hasNext();) {
			AutoCloseable closeable = it.next();
			try {
				closeable.close();
			} catch (Exception ex) {
				logger.warn("Could not properly close object of class " + closeable.getClass() + " during cleanup", ex);
			} finally {
				it.remove();
			}
		}
	}

	/**
	 * Override and add any code here to run if this operation receives an interrupt/timeout. It will be processed in a
	 * different thread, so you can use this method to forcibly close I/O streams or set cancellation tokens for
	 * example in order to force a long-running invocation to come to an abrupt stop.
	 */
	public void onInterrupt() {
	}
}
