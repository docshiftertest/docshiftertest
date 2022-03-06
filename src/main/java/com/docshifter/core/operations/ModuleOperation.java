package com.docshifter.core.operations;

import com.docshifter.core.asposehelper.utils.image.ImageUtils;
import com.docshifter.core.config.wrapper.ModuleWrapper;
import com.docshifter.core.exceptions.ConfigFileNotFoundException;
import com.docshifter.core.exceptions.EmptyOperationException;
import com.docshifter.core.task.Task;
import com.docshifter.core.operations.annotations.ModuleParam;
import com.docshifter.core.task.TaskStatus;
import com.docshifter.core.utils.ModuleClassLoader;
import com.google.common.base.Defaults;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.text.StringSubstitutor;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.awt.*;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by michiel.vandriessche on 15/03/17.
 */
@Log4j2
public abstract class ModuleOperation {

	private enum ParamType {
		BOOLEAN,
		BYTE,
		DOUBLE,
		FLOAT,
		INTEGER,
		LONG,
		SHORT,
		STRING,
		ENUM,
		XML_FILE,
		COLOR,
		UNKNOWN
	}

	public static final String MP_FILE_INDICATOR = "mp_file:";
	protected String operation = "Abstract Operation";
	public Task task;
	protected ModuleWrapper moduleWrapper;
	protected OperationParams operationParams;

	protected TaskStatus fillInParameters() {

		//WHEN FAULT => add fault to task.addMessage()
		Class<?> calledClass = this.getClass();

		Map<String, Object> valuesMap;
		
		// Get all the parameters into the valuesMap
		// First, by default, take all the Parameters set on the operationParams
		valuesMap = operationParams.getParameters();
		if (moduleWrapper != null) {
			// if we have a Module Wrapper then what's in its ParamMap gets added (or overridden) in the valuesMap 
			valuesMap.putAll( moduleWrapper.getParamMap());
		}

		//Match and fill in ModuleParam
		return Arrays.stream(calledClass.getDeclaredFields())
				.map(fld -> new ImmutablePair<>(fld, extractModuleParamAnnotation(fld.getDeclaredAnnotations())))
				.filter(entry -> entry.getRight() != null)
				.map(entry -> {
					//Select field + select fieldname to set
					Field field = entry.getLeft();
					ModuleParam modder = entry.getRight();
					log.trace("field value = {}", field);
					String fieldName = modder.value();
					log.trace("field name = {}", fieldName);
					if (fieldName.isEmpty()) {
						fieldName = field.getName();
						log.trace("fieldname is empty. Newname = {}", fieldName);
					}

					boolean required = modder.required();
					boolean supportPlaceholder = modder.supportPlaceholder();
					Object moduleValue = valuesMap.get(fieldName);

					log.trace("Field Type is: {}", field.getType().getName());
					ParamType paramType = parseParamType(field, modder);

					if (moduleValue == null || moduleValue.toString().isEmpty()) {
						if (StringUtils.isNotEmpty((modder.defaultValue()))) {
							moduleValue = modder.defaultValue();
							if (required) {
								log.info("Required Moduleparameter '{}' in {} was not set. The default value ({}) was used.",
										fieldName, calledClass, moduleValue);
							} else {
								log.debug("Moduleparameter '{}' in {} was not set. The default value ({}) was used.",
										fieldName, calledClass, moduleValue);
							}
						}
						else if (paramType == ParamType.BOOLEAN) {
							moduleValue = false;
						}
						else if (required) {
							log.error("Required Moduleparameter '{}' has not been set for usage in {}", fieldName, calledClass);
							return TaskStatus.BAD_CONFIG;
						} else {
							log.trace("Empty Moduleparameter '{}' in {} is not required and does not have a default value",
									fieldName,	calledClass);
						}
					}

					boolean access_old = field.isAccessible();
					field.setAccessible(true);
					TaskStatus status = setModuleValue(paramType, fieldName, field, moduleValue, supportPlaceholder);
					field.setAccessible(access_old);
					return status;
				})
				.filter(status -> !status.isSuccess())
				.findAny()
				.orElse(TaskStatus.SUCCESS);
	}

	/**
	 * Determine the ParamType as an Enum value for the field's type
	 * @param field
	 * @return ParamType the type of parameter e.g. STRING, INTEGER, LONG, BOOLEAN etc.
	 */
	private ParamType parseParamType(Field field, ModuleParam modder) {
		log.debug("Parsing the param type for field: {}", field);
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
			case "java.awt.color":
				return ParamType.COLOR;
			default:
				if (ModuleParam.ConfigFileType.JAXB_XML.equals(modder.configFile())) {
					return ParamType.XML_FILE;
				}
				if (field.getType().isEnum()) {
					return ParamType.ENUM;
				}
				log.warn("Returning ParamType of UNKNOWN for field: {}", field);
				return ParamType.UNKNOWN;
		}
	}

	/**
	 * Factored-out code to set the Module Value to the specified value
	 * @param paramType the type of field it is (STRING, INTEGER, BYTE ...)
	 * @param fieldName the name of the module parameter we're setting, for logging purposes
	 * @param field The field whose value we want to set
	 * @param moduleValue The value to set, as an Object
	 * @param supportPlaceholder boolean whether we support placeholders or not ${something}
	 * @return whether we were able to set the value successfully or if there was some kind of user or programming
	 * problem...
	 */
	private TaskStatus setModuleValue(ParamType paramType, String fieldName, Field field, Object moduleValue,
									  boolean supportPlaceholder) {
		String moduleValueStr = null;
		try {
			log.trace("modulevalue is === {}", moduleValue);
			if (moduleValue == null) {
				if (paramType == ParamType.BOOLEAN) {
					field.set(this, false);
				}
				else {
					field.set(this, Defaults.defaultValue(field.getType()));
				}
				return TaskStatus.SUCCESS;
			}
			moduleValueStr = moduleValue.toString();
			if (supportPlaceholder) {
				moduleValueStr = processPlaceHolders(task, moduleValueStr);
			}
			if (moduleValueStr.isEmpty()) {

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
				case COLOR:
					if (moduleValue instanceof Color) {
						field.set(this, moduleValue);
					} else {
						field.set(this, ImageUtils.getColor(moduleValueStr));
					}
				case XML_FILE:
					if (field.getType().isAssignableFrom(moduleValue.getClass())) {
						field.set(this, moduleValue);
					} else {
						File xmlConfigFile = new File(moduleValueStr);
						if (!xmlConfigFile.exists()) {
							throw new ConfigFileNotFoundException();
						}
						JAXBContext jaxbContext = JAXBContext.newInstance(field.getType());
						Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
						field.set(this, jaxbUnmarshaller.unmarshal(xmlConfigFile));
					}
					break;
				case ENUM:
					if (field.getType().isAssignableFrom(moduleValue.getClass())) {
						field.set(this, moduleValue);
					}
					else {
						field.set(this, Enum.valueOf(field.getType().asSubclass(Enum.class), moduleValueStr));
					}
					break;
				case UNKNOWN:
				default:
					log.warn("Got an UNKNOWN type: [{}] for field: {}. Attempting to set value to: {}",
							field.getType().getSimpleName(), field.getName(), moduleValue);
					field.set(this, moduleValue);
					break;
			}
		} catch (IllegalAccessException ex) {
			log.error("Illegal access of field: {}", field.getName(), ex);
			return TaskStatus.FAILURE;
		} catch (IllegalArgumentException ex) {
			log.error("Value '{}' of module parameter '{}' doesn't match the expected type: {}", moduleValue, fieldName,
					field.getType().getSimpleName().toLowerCase(), ex);
			return TaskStatus.BAD_CONFIG;
		} catch (ConfigFileNotFoundException ex) {
			log.error("The XML config file [pre-placeholder = " + moduleValue + "] " +
			"[post-placeholder = " + moduleValueStr + "] specified in the module parameter " + fieldName +
							" does not exist: is it accessible on the file system?", ex);
			return TaskStatus.BAD_CONFIG;
		} catch (JAXBException ex) {
			log.error("Unable to deserialize XML config file [pre-placeholder = " + moduleValue + "] " +
					"[post-placeholder = " + moduleValueStr + "] specified in the module parameter " + fieldName +
					": is it valid XML? See causing exception for more details.", ex);
			return TaskStatus.BAD_CONFIG;
		} catch (Exception ex) {
			log.error("Something ({}) went wrong while filling module parameter '{}' associated with field: {}",
					ex.getClass(), fieldName, field.getName(), ex);
			return TaskStatus.FAILURE;
		}
		return TaskStatus.SUCCESS;
	}

	/**
	 * Extracts a ModuleParam Annotation, if there is one, out of an Annotation array
	 * @param annotations An array of Annotations
	 * @return Optionally a ModuleParam Annotation, otherwise null
	 */
	private ModuleParam extractModuleParamAnnotation(Annotation[] annotations) {
		for (Annotation annie : annotations) {
			if (annie instanceof ModuleParam) {
				return (ModuleParam) annie;
			}
		}
		return null;
	}

	public static String processPlaceHolders(Task task, String text) {
		log.trace("Trying to process placeholders for: {}", text);
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
		log.trace("result after substitution: {}", text);
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
			log.error("Tried to access an invalid operation (it was NULL!)");
			throw new EmptyOperationException();
		}

		ModuleClassLoader classLoader;
		Object obj;
		try {
			classLoader = ModuleClassLoader.getInstance();
			log.debug("Got a class loader, now calling getSpringBean for opClassName: {}", opClassName);
			obj = classLoader.getSpringBean(opClassName);
			if (obj == null) {
			    obj = classLoader.getClassObject(opClassName);
			}
		} catch (InstantiationException insE) {
			log.warn("Failed to instantiate ModuleClassLoader. Probably running test. Trying to use standard " +
					"classloader for opClassName: {}", opClassName, insE);
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
			log.error("Incorrect operation, please check your configuration");
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
    		log.warn("Bad range specified: [{}]", param);
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
}
