package com.docshifter.core.utils.dctm;


import com.documentum.fc.client.*;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfValue;
import lombok.extern.log4j.Log4j2;

import java.text.ParseException;
import java.util.*;

@Log4j2
public class MetadataUtils {
	
	private static final List<String> ignoreList = Arrays.asList("object_name", "acl_name", "acl_domain", "doc_id", "dossier_number", "dossier_type", "confidential");
	
	public static Object getValue(IDfTypedObject object, int type, boolean repeating, String name) throws DfException {
		if (!repeating) {
			return getValue(object, type, name);
		} else {
			return getRepeatingValue(object, type, name);
		}
	}
	
	public static Object getValue(IDfTypedObject object, int type, String name) throws DfException {
		switch (type) {
			case IDfAttr.DM_BOOLEAN:
				return object.getBoolean(name);
			case IDfAttr.DM_DOUBLE:
				return object.getDouble(name);
			case IDfAttr.DM_ID:
				return object.getId(name).getId();
			case IDfAttr.DM_INTEGER:
				return object.getInt(name);
			case IDfAttr.DM_STRING:
				return object.getString(name);
			case IDfAttr.DM_TIME:
				return object.getTime(name).getDate();
			default:
				return null;
		}
	}
	
	public static Object getRepeatingValue(IDfTypedObject object, int type, String name, int index) throws DfException {
		switch (type) {
			case IDfAttr.DM_BOOLEAN:
				return object.getRepeatingBoolean(name, index);
			case IDfAttr.DM_DOUBLE:
				return object.getRepeatingDouble(name, index);
			case IDfAttr.DM_ID:
				return object.getRepeatingId(name, index).getId();
			case IDfAttr.DM_INTEGER:
				return object.getRepeatingInt(name, index);
			case IDfAttr.DM_STRING:
				return object.getRepeatingString(name, index);
			case IDfAttr.DM_TIME:
				return object.getRepeatingTime(name, index).getDate();
			default:
				return null;
		}
	}
	
	
	public static Object getAttr(IDfTypedObject object, IDfAttr attr) throws DfException {
		if (attr.isRepeating()) {
			return MetadataUtils.getRepeatingValue(object, attr.getDataType(), attr.getName());
		} else {
			return MetadataUtils.getValue(object, attr.getDataType(), attr.getName());
		}
		
	}
	
	public static Object[] getRepeatingValue(IDfTypedObject object, int type, String name) throws DfException {
		
		int count = object.getValueCount(name);
		
		Object[] attributes = new Object[object.getValueCount(name)];
		
		for (int i = 0; i < count; i++) {
			attributes[i] = getRepeatingValue(object, type, name, i);
		}
		
		return attributes;
	}
	
	
	public static String[] getRepeatingStrings(IDfTypedObject object, String name) throws DfException {
		
		int count = object.getValueCount(name);
		
		String[] attributes = new String[object.getValueCount(name)];
		
		for (int i = 0; i < count; i++) {
			attributes[i] = object.getRepeatingString(name, i);
		}
		
		return attributes;
	}
	
	
	public static void setRepeatingValue(IDfTypedObject object, int type, String name, int i, Object value) throws DfException {
		if (value != null) {
			switch (type) {
				case IDfAttr.DM_BOOLEAN:
					try {
						object.setRepeatingBoolean(name, i, DataUtils.tryParseBoolean(value));
					} catch (ParseException e) {
						throw new IllegalArgumentException(String.format("Value for %s can not be parsed to boolean", name));
					}
					break;
				case IDfAttr.DM_DOUBLE:
					object.setRepeatingDouble(name, i, (Double) value);
					break;
				case IDfAttr.DM_INTEGER:
					object.setRepeatingInt(name, i, (Integer) value);
					break;
				case IDfAttr.DM_STRING:
					object.setRepeatingString(name, i, (String) value);
					break;
				case IDfAttr.DM_TIME:
					object.setRepeatingTime(name, i, new DfTime(DataUtils.tryParseDate((String) value)));
					break;
				default:
					object.setRepeatingString(name, i, (String) value);
					break;
			}
		} else {
			object.remove(name, i);
		}
	}
	
	public static void replaceRepeatingValue(IDfSysObject document, int type, String name, String oldValue, Object value) throws DfException {
		
		int count = document.getValueCount(name);
		
		for (int i = 0; i < count; i++) {
			IDfValue currentValue = document.getRepeatingValue(name, i);
			
			if (compareDfValue(currentValue, oldValue)) {
				setRepeatingValue(document, type, name, i, value);
				count = document.getValueCount(name);
			}
			
		}
		
		
	}
	
	public static boolean compareDfValue(IDfValue value, String stringValue) {
		if (value == null && stringValue == null) {
			return true;
		}
		
		try {
			switch (value.getDataType()) {
				case IDfAttr.DM_BOOLEAN:
					return (value.asBoolean() == DataUtils.tryParseBoolean(stringValue));
				
				case IDfAttr.DM_DOUBLE:
					return (value.asDouble() == Double.parseDouble(stringValue));
				case IDfAttr.DM_INTEGER:
					return (value.asInteger() == Integer.parseInt(stringValue));
				case IDfAttr.DM_STRING:
					return (value.asString().equals(stringValue));
				case IDfAttr.DM_TIME:
					return (value.asTime().getDate().equals(DataUtils.tryParseDate(stringValue)));
				default:
					return (value.asString().equals(stringValue));
			}
		} catch (ParseException | NumberFormatException e) {
			log.warn("unable to parse string to correct type");
		}
		return false;
		
	}
	
	public static void setRepeatingValues(IDfTypedObject object, int type, String name, Iterable values) throws DfException {
		Iterator it = values.iterator();
		object.truncate(name, 0);
		int i = 0;
		
		while (it.hasNext()) {
			setRepeatingValue(object, type, name, i, it.next());
			i++;
		}
		
	}
	
	public static void setRepeatingValues(IDfTypedObject object, int type, String name, Object[] values) throws DfException {
		
		object.truncate(name, 0);
		
		for (int i = 0; i <values.length; i++) {
			setRepeatingValue(object, type, name, i, values[i]);
		}
		
	}
	
	public static void setAttributes(IDfTypedObject document, Map<String, Object> attributes) throws DfException, IllegalArgumentException {
		setAttributes(document, attributes, false);
	}
	
	public static void setAttributes(IDfTypedObject document, Map<String, Object> attributes, boolean continueWhenUnknown) throws DfException, IllegalArgumentException {
		if (attributes != null) {
			for (Map.Entry<String, Object> attr : attributes.entrySet()) {
				String attrName = attr.getKey();
				Object attrValue = attr.getValue();
				
				//TODO: Calling a method that returns a boolean and ignoring the result?
				setAttribute(document, attrName, attrValue, continueWhenUnknown);
				
			}
		}
	}
	
	
	public static boolean setAttribute(IDfTypedObject document, String attrName, Object attrValue, boolean continueWhenUnknown) throws DfException {
		if (attrName.matches("[a-zA-Z0-9_\\-]+\\[]")) {
			attrName = attrName.substring(0, attrName.indexOf("["));
			
			if (!validateAttribute(document, continueWhenUnknown, attrName)) return false;
			
			int i = document.getValueCount(attrName);
			//no need to increment count = latest index
			
			if (attrValue instanceof Iterable) {
				Iterator array = ((Iterable) attrValue).iterator();
				while (array.hasNext()) {
					setRepeatingValue(document, document.getAttrDataType(attrName), attrName, i, array.next());
					i++;
				}
			} else {
				setRepeatingValue(document, document.getAttrDataType(attrName), attrName, i, attrValue);
			}
		} else if (attrName.matches("[a-zA-Z0-9_\\-]+\\[[0-9]+]")) {
			String index = attrName.substring(attrName.indexOf("[") + 1, attrName.indexOf("]"));
			int i = Integer.parseInt(index);
			
			attrName = attrName.substring(0, attrName.indexOf("["));
			if (!validateAttribute(document, continueWhenUnknown, attrName)) return false;
			
			setRepeatingValue(document, document.getAttrDataType(attrName), attrName, i, attrValue);
			
		} else if (attrName.matches("[a-zA-Z0-9_\\-]+\\[[\\x20-\\x7E]+]")) {
			String oldValue = attrName.substring(attrName.indexOf("[") + 1, attrName.lastIndexOf("]"));
			
			attrName = attrName.substring(0, attrName.indexOf("["));
			if (!validateAttribute(document, continueWhenUnknown, attrName)) return false;
			
			replaceRepeatingValue((IDfSysObject) document, document.getAttrDataType(attrName), attrName, oldValue, attrValue);
			
			
		} else {
			
			if (!validateAttribute(document, continueWhenUnknown, attrName)) return false;
			
			if (attrValue == null) {
				setNull(document, document.getAttrDataType(attrName), attrName);
			} else if (attrValue instanceof Iterable) {
				setRepeatingValues(document, document.getAttrDataType(attrName), attrName, (Iterable) attrValue);
				
			} else if (attrValue.getClass().isArray()) {
				
				setRepeatingValues(document, document.getAttrDataType(attrName), attrName, (Object[])attrValue);
				
			} else {
				setValue(document, document.getAttrDataType(attrName), attrName, attrValue);
			}
		}
		return true;
	}
	
	
	private static boolean validateAttribute(IDfTypedObject document, boolean continueWhenUnknown, String attrName) throws DfException {
		if (!document.hasAttr(attrName)) {
			if (continueWhenUnknown) {
				return true;
			} else {
				throw new IllegalArgumentException(String.format("Attribute %s does not exist on type %s ", attrName, document.getString(MetadataConsts.OBJECT_TYPE)));
			}
			
		}
		return true;
	}
	
	
	public static void setValue(IDfTypedObject object, String name, Object value) throws DfException, IllegalArgumentException {
		setValue(object, object.getAttrDataType(name), name, value);
	}
	
	public static void setValue(IDfTypedObject object, int type, String name, Object value) throws DfException, IllegalArgumentException {
		if (value != null) {
			
			switch (type) {
				case IDfAttr.DM_BOOLEAN:
					try {
						object.setBoolean(name, DataUtils.tryParseBoolean(value));
					} catch (ParseException e) {
						throw new IllegalArgumentException(String.format("Value for %s can not be parsed to boolean", name));
					}
				case IDfAttr.DM_DOUBLE:
					if (value instanceof Double) {
						object.setDouble(name, (Double) value);
					} else if (value instanceof Integer) {
						object.setDouble(name, (Integer) value);
					} else if (value instanceof String && ((String) value).matches("[0-9.,]*")) {
						object.setDouble(name, (Double.parseDouble((String) value)));
					} else {
						throw new IllegalArgumentException(String.format("Failed to parse value %s for %s to double", value.toString(), name));
					}
					break;
				case IDfAttr.DM_INTEGER:
					if (value instanceof Integer) {
						object.setInt(name, (Integer) value);
					} else if (value instanceof String && ((String) value).matches("[0-9]*")) {
						object.setInt(name, (Integer.parseInt((String) value)));
					} else {
						throw new IllegalArgumentException(String.format("Failed to parse value %s for %s to integer", value.toString(), name));
					}
					break;
				case IDfAttr.DM_STRING:
					object.setString(name, (String) value);
					break;
				case IDfAttr.DM_TIME:
					Date date = DataUtils.tryParseDate((String) value);
					if (date != null)
						object.setTime(name, new DfTime(date));
					break;
				default:
					object.setString(name, (String) value);
					break;
			}
		} else {
			setNull(object, type, name);
		}
	}
	
	public static void setNull(IDfTypedObject object, int type, String name) throws DfException {
		if (object.isAttrRepeating(name)) {
			object.truncate(name, 0);
		} else {
			
			switch (type) {
				case IDfAttr.DM_BOOLEAN:
					object.setBoolean(name, false);
					break;
				case IDfAttr.DM_DOUBLE:
					object.setDouble(name, 0);
					break;
				case IDfAttr.DM_INTEGER:
					object.setInt(name, 0);
					break;
				case IDfAttr.DM_TIME:
					object.setTime(name, null);
					break;
				case IDfAttr.DM_STRING:
				default:
					object.setString(name, null);
					break;
			}
		}
		
	}
	
	
	public static Map<String, Object> getAttributeMap(IDfTypedObject object) throws DfException {
		int attrCount = object.getAttrCount();
		Map<String, Object> attributes = new HashMap<String, Object>(attrCount);
		
		for (int i = 0; i < attrCount; i++) {
			
			String name = object.getAttr(i).getName();
			
			if (!name.startsWith("a_") && !name.startsWith("r_") && !name.startsWith("i_") && !ignoreList.contains(name)) {
				
				attributes.put(name, getAttr(object, object.getAttr(i)));
			}
		}
		
		return attributes;
	}
	
	public static Object getValueIfExists(IDfTypedObject object, String attr) throws DfException {
		if (object.hasAttr(attr)) {
			return getValue(object, object.getAttrDataType(attr), attr);
		}
		return null;
	}
	
	public static Set<String> collectionToStringSet(IDfCollection coll, String attribute) throws DfException {
		Set<String> locations;
		locations = new HashSet<>();
		
		try {
			
			while (coll.next()) {
				locations.add(coll.getString(attribute));
			}
			
		} finally {
			if (coll != null) {
				coll.close();
			}
		}
		return locations;
	}
	
	
	public static Set<String> getPaths(IDfSysObject object)
			throws DfException {
		
		Set<String> paths = new HashSet<>();
		if (object instanceof IDfFolder) {
			IDfFolder folder = (IDfFolder) object;
			int pathCount = folder.getFolderPathCount();
			for (int i = 0; i < pathCount; i++)
				paths.add(folder.getFolderPath(i));
		} else {
			IDfSession session = object.getSession();
			int folderCount = object.getFolderIdCount();
			for (int i = 0; i < folderCount; i++) {
				paths.addAll(getPaths((IDfFolder) session.getObject(object.getFolderId(i))));
				
			}
		}
		
		return paths;
	}
	
}