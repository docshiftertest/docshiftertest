package com.docshifter.core.utils.dctm;

import com.docshifter.core.utils.Logger;
import com.documentum.fc.client.*;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfTime;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j
public class DataUtils {

	private static final String formatStrings[] = {
			"dd/MM/yyyy HH:mm:ss",
			"dd-MM-yyyy HH:mm:ss",
			"yyyy-MM-dd HH:mm:ss",
			"yyyy/MM/dd HH:mm:ss",
			"dd-MM-yyyy",
			"dd/MM/yyyy",
			"yyyy-MM-dd",
			"yyyy/MM/dd"};
	private static final String formatRegexes[] = {
			"(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[012])/(19|20)\\d\\d [0-6]\\d:[0-6]\\d:[0-6]\\d",
			"(0[1-9]|[12][0-9]|3[01])-(0[1-9]|1[012])-(19|20)\\d\\d [0-6]\\d:[0-6]\\d:[0-6]\\d",
			"(19|20)\\d\\d-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01]) [0-6]\\d:[0-6]\\d:[0-6]\\d",
			"(19|20)\\d\\d/(0[1-9]|1[012])/(0[1-9]|[12][0-9]|3[01]) [0-6]\\d:[0-6]\\d:[0-6]\\d",
			"(0[1-9]|[12][0-9]|3[01])-(0[1-9]|1[012])-(19|20)\\d\\d",
			"(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[012])/(19|20)\\d\\d",
			"(19|20)\\d\\d-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])",
			"(19|20)\\d\\d/(0[1-9]|1[012])/(0[1-9]|[12][0-9]|3[01])"};

	protected final static String SPLIT_PATTERN = "\\A([^|]*)(?:\\|([^(]*))?()?(?:\\(([^()]*)?\\))\\z";

	public static Map<String, Object> extractData(IDfSysObject sysobj, String[] fieldsArray) throws DfException {
		Map<String, Object> rawLoadData = new HashMap<>();


		Logger.debug("Starting data extraction", null);

		for (int i = 0; i < fieldsArray.length; i++) {
			String field = fieldsArray[i];

			Logger.debug("found field: " + field + " will try to parse into fieldDefinition, returnName and clause", null);
			
			FieldInfo fieldInfo = extractFieldInfo(field);
			String fieldDefinition = fieldInfo.getFieldDefinition();
			String returnName = fieldInfo.getReturnName();
			String clause = fieldInfo.getClause();

			Logger.debug(String.format("fieldDefinition: %s; returnName: %s; clause: %s", fieldDefinition, returnName, clause), null);

			Logger.debug("object has field?" + fieldDefinition + ": " + sysobj.hasAttr(field), null);
			if (fieldDefinition.equalsIgnoreCase("full_folder_path")) {
				IDfFolder folderObj = sysobj.getSession().getFolderBySpecification(sysobj.getFolderId(0).getId());
				if (folderObj != null) {
					rawLoadData.put(fieldDefinition, folderObj.getString("r_folder_path") + "/" + sysobj.getObjectName());
				}
			} else if (StringUtils.isNotBlank(clause)) {
				rawLoadData.put(returnName, getFieldWithQuery(sysobj, fieldDefinition, clause));
				Logger.debug("Field " + field + "is query and gave following result : " + Arrays.toString((Object[]) rawLoadData.get(returnName)), null);
			} else if (sysobj.hasAttr(fieldDefinition)) {
				Logger.debug("Field " + fieldDefinition + "is repeating? : " + sysobj.isAttrRepeating(fieldDefinition), null);
				if (sysobj.isAttrRepeating(fieldDefinition)) {
					rawLoadData.put(returnName, getRepeating(sysobj, fieldDefinition));
				} else {
					Logger.debug("Field " + fieldDefinition + " is of type : " + sysobj.getAttrDataType(fieldDefinition), null);
					switch (sysobj.getAttrDataType(fieldDefinition)) {
						case IDfType.DF_STRING:
							rawLoadData.put(returnName, sysobj.getString(fieldDefinition));
							break;
						case IDfType.DF_BOOLEAN:
							rawLoadData.put(returnName, sysobj.getBoolean(fieldDefinition));
							break;
						case IDfType.DF_DOUBLE:
							rawLoadData.put(returnName, sysobj.getDouble(fieldDefinition));
							break;
						case IDfType.DF_ID:
							rawLoadData.put(returnName, sysobj.getId(fieldDefinition));
							break;
						case IDfType.DF_INTEGER:
							rawLoadData.put(returnName, sysobj.getInt(fieldDefinition));
							break;
						case IDfType.DF_TIME:
							final Date date = sysobj.getTime(fieldDefinition).getDate();
							rawLoadData.put(returnName, date);
							break;
						default:
							break;
					}

				}
			}
		}
		return restructureData(rawLoadData);
	}

	private static Map<String, Object> restructureData(Map<String, Object> rawLoadData) {
		Map<String, Object> restructuredData = new HashMap<>();

		for (Map.Entry<String, Object> entry : rawLoadData.entrySet()) {
			Logger.info("data has been loaded from dctm. restructuring the data", null);

			String key = entry.getKey();
			Logger.debug("processing entry: " + key, null);
			Object object = entry.getValue();
			if (object == null) {
				Logger.warn("Cannot process: object (entry value) was NULL, for entry key: " + entry.getKey(), null);
				continue;
			}
			Logger.debug("value object.getClass(): " + object.getClass(), null);
			Logger.debug("value object.getClass().getCanonicalName(): " + object.getClass().getCanonicalName(), null);
			Logger.debug("value object.getClass().isArray(): " + object.getClass().isArray(), null);
			Logger.debug("value object instanceof IDfTime: " + (object instanceof IDfTime), null);

			if (!object.getClass().isArray()) {
				Logger.info("object for key '" + key + "' needs no further processing", null);

				restructuredData.put(key, object);
			} else {
				int arraySize = ((Object[]) object).length;
				Logger.info("object for key '" + key + "' needs further processing, is array with size: " + arraySize, null);

				String mapKey = key;
				if (key.contains(".")) {
					mapKey = key.substring(0, key.indexOf("."));
				}

				List<Object> childList;
				if (restructuredData.containsKey(mapKey)) {
					childList = (List) restructuredData.get(mapKey);
				}
				else {
					childList = new ArrayList<>();
				}
				for (int j = 0; j < arraySize; j++) {
					childList.add(((Object[]) object)[j]);
				}
				restructuredData.put(mapKey, childList);
			}
		}

		Logger.info("data restructuring completed", null);
		return restructuredData;
	}

	public static Object[] getFieldWithQuery(IDfSysObject sysobj, String fieldDef, String clause) throws DfException {

		IDfSession session = sysobj.getSession();

		String objecttype = getObjectTypeFromFieldDef(fieldDef);
		String objectfield = getFieldFromFieldDef(fieldDef);

		String clauseField1 = clause.split("=")[0];
		String clauseField2 = clause.split("=")[1];


		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("select ")
				.append(fieldDef)
				.append(" from ")
				.append(getObjectTypeFromFieldDef(clauseField1))
				.append(", ")
				.append(getObjectTypeFromFieldDef(clauseField2))
				.append(" where ")
				.append(clause)
				.append(" and ")
				.append(getObjectTypeFromFieldDef(clauseField1))
				.append(".r_object_id ='")
				.append(sysobj.getObjectId().getId())
				.append("' order by ")
				.append(clauseField1)
				.append(" ASC ")
				.append(" ENABLE(ROW_BASED)");

		DfQuery query = new DfQuery(queryBuilder.toString());


		IDfType type = session.getType(objecttype);
		int dataType = type.getTypeAttrDataType(objectfield);


		IDfCollection coll = null;
		try {
			coll = query.execute(session, IDfQuery.DF_READ_QUERY);

			List<Object> values = new ArrayList<>();

			while (coll.next()) {
				switch (dataType) {
					case IDfType.DF_STRING:
						values.add(coll.getString(objectfield));
						break;
					case IDfType.DF_BOOLEAN:
						values.add(coll.getBoolean(objectfield));
						break;
					case IDfType.DF_DOUBLE:
						values.add(coll.getDouble(objectfield));
						break;
					case IDfType.DF_ID:
						values.add(coll.getId(objectfield).getId());
						break;
					case IDfType.DF_INTEGER:
						values.add(coll.getInt(objectfield));
						break;
					case IDfType.DF_TIME:
						values.add(coll.getTime(objectfield));
						break;
					default:
						break;
				}
			}
			return values.toArray();

		} finally {
			if (coll != null) {
				coll.close();
			}
		}
	}

	protected static String getFieldFromFieldDef(String fieldDef) {
		return fieldDef.substring(fieldDef.indexOf('.') + 1, fieldDef.length());
	}

	public static String getObjectTypeFromFieldDef(String fieldDef) {
		return fieldDef.substring(0, fieldDef.indexOf('.'));
	}

	private static Object[] getRepeating(IDfSysObject sysobj, String field) throws DfException {

		Logger.debug("Field " + field + " count is : " + sysobj.getValueCount(field), null);
		Logger.debug("Field " + field + " is of type : " + sysobj.getAttrDataType(field), null);

		int count = sysobj.getValueCount(field);

		switch (sysobj.getAttrDataType(field)) {
			case IDfType.DF_STRING:
				List<String> stringValues = new ArrayList<>();
				for (int j = 0; j < count; j++) {
					Logger.debug("Field " + field + " adding value : " + sysobj.getRepeatingString(field, j), null);
					String val = sysobj.getRepeatingString(field, j);
					if (StringUtils.isNotBlank(val)) {
						stringValues.add(val);
					}
					Collections.sort(stringValues, new Comparator<String>() {
						@Override
						public int compare(String s1, String s2) {
							return s1.compareToIgnoreCase(s2);
						}
					});
				}
				return stringValues.toArray();
			case IDfType.DF_BOOLEAN:
				Boolean[] boolValues = new Boolean[count];
				for (int j = 0; j < count; j++) {
					Logger.debug("Field " + field + " adding value : " + sysobj.getRepeatingBoolean(field, j), null);
					boolValues[j] = sysobj.getRepeatingBoolean(field, j);
				}
				return boolValues;
			case IDfType.DF_DOUBLE:
				Double[] doubleValues = new Double[count];
				for (int j = 0; j < count; j++) {
					Logger.debug("Field " + field + " adding value : " + sysobj.getRepeatingDouble(field, j), null);
					doubleValues[j] = sysobj.getRepeatingDouble(field, j);
				}
				return doubleValues;
			case IDfType.DF_ID:
				String[] idValues = new String[count];
				for (int j = 0; j < count; j++) {
					Logger.debug("Field " + field + " adding value : " + sysobj.getRepeatingId(field, j), null);
					idValues[j] = sysobj.getRepeatingId(field, j).getId();
				}
				return idValues;
			case IDfType.DF_INTEGER:
				Integer[] intValues = new Integer[count];
				for (int j = 0; j < count; j++) {
					Logger.debug("Field " + field + " adding value : " + sysobj.getRepeatingInt(field, j), null);
					intValues[j] = sysobj.getRepeatingInt(field, j);
				}
				return intValues;
			case IDfType.DF_TIME:
				Date[] timeValues = new Date[count];
				for (int j = 0; j < count; j++) {
					Logger.debug("Field " + field + " adding value : " + sysobj.getRepeatingTime(field, j), null);
					timeValues[j] = sysobj.getRepeatingTime(field, j).getDate();
				}
				return timeValues;
			default:
				return null;
		}
	}

	protected static Boolean tryParseBoolean(Object value) throws ParseException {
		if (value instanceof Boolean) {
			return (Boolean) value;
		} else if (value instanceof String) {
			return Boolean.parseBoolean((String) value);
		} else {
			throw new ParseException("Value can not be parsed to boolean", 0);
		}
	}

	protected static Date tryParseDate(String dateString)
	{
		for (int i = 0; i < formatStrings.length; i++) {
			String regex = formatRegexes[i];
			String formatString = formatStrings[i];

			if(dateString.matches(regex)) {
				try {
					return new SimpleDateFormat(formatString).parse(dateString);
				} catch (ParseException e) {
					log.warn("Something strange happened: string matches regex but fails to format");
				}
			}
		}

		log.debug("Could not parse " + dateString + " to a valid date. Supported formats are:\n");
		for(String f : formatStrings)
		{
			log.debug(f);
		}
		return null;
	}

	public static String escapeForDql(String name) {
		name = name.replaceAll("'", "''");

		return name;
	}



	public static FieldInfo extractFieldInfo(String field) {
		Pattern splitPattern = Pattern.compile(SPLIT_PATTERN);
		Matcher splitMatcher = splitPattern.matcher(field);
		
		String returnName = "";
		String fieldDefinition = "";
		String clause = "";
		if (splitMatcher.find()) {
				fieldDefinition = splitMatcher.group(1);
				returnName = splitMatcher.group(2);
				clause = splitMatcher.group(4);
		} else {
			fieldDefinition =field;
		}
		
		if (StringUtils.isBlank(returnName)) {
			Logger.debug("no return name specified, setting to field definition", null);
			returnName = fieldDefinition;
		}
		return new FieldInfo(fieldDefinition, clause, returnName);
	}
}
