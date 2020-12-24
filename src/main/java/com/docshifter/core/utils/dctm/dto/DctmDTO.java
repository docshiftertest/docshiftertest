package com.docshifter.core.utils.dctm.dto;

import com.docshifter.core.utils.dctm.MetadataUtils;
import com.docshifter.core.utils.dctm.annotations.DctmAttribute;
import com.docshifter.core.utils.dctm.annotations.DctmId;
import com.docshifter.core.utils.dctm.annotations.DctmLocation;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfAttr;
import org.apache.commons.lang.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DctmDTO<T> {
	
	private final Class<T> clazz;
	
	public DctmDTO(Class<T> clazz) {
		this.clazz = clazz;
	}
	
	public T poToPOJO(IDfPersistentObject po) throws DfException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, ParseException {
		
		T pojo = clazz.newInstance();
		
		for (Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(DctmAttribute.class)) {
				String attributeName = field.getAnnotation(DctmAttribute.class).value();
				String fieldName = field.getName();
				int fieldType = po.getAttrDataType(attributeName);
				
				switch (fieldType) {
					case IDfAttr.DM_BOOLEAN:
						Boolean b = po.getBoolean(attributeName);
						if (field.isAccessible()) {
							field.setBoolean(pojo, b);
						} else {
							setValueUsingSetter(pojo, fieldName, Boolean.TYPE, b);
						}
						break;
					case IDfAttr.DM_INTEGER:
						Integer i = po.getInt(attributeName);
						if (field.isAccessible()) {
							field.setInt(pojo, i);
						} else {
							setValueUsingSetter(pojo, fieldName, Integer.TYPE, i);
						}
						break;
					case IDfAttr.DM_DOUBLE:
						Double d = po.getDouble(attributeName);
						if (field.isAccessible()) {
							field.setDouble(pojo, d);
						} else {
							setValueUsingSetter(pojo, fieldName, Double.TYPE, d);
						}
						break;
					case IDfAttr.DM_TIME:
						Date date = po.getTime(attributeName).getDate();
						if (field.isAccessible()) {
							field.set(pojo, date);
						} else {
							setValueUsingSetter(pojo, fieldName, Date.class, date);
						}
						break;
					case IDfAttr.DM_ID:
					case IDfAttr.DM_STRING:
					case IDfAttr.DM_UNDEFINED:
					default:
						String s = po.getString(attributeName);
						if (field.isAccessible()) {
							field.set(pojo, s);
						} else {
							setValueUsingSetter(pojo, fieldName, String.class, s);
						}
						break;
				}
				
			} else if (field.isAnnotationPresent(DctmId.class)) {
				String attributeName = field.getAnnotation(DctmId.class).value();
				String fieldName = field.getName();
				String s = po.getString(attributeName);
				if (field.isAccessible()) {
					field.set(pojo, s);
				} else {
					setValueUsingSetter(pojo, fieldName, String.class, s);
				}
			} else if (field.isAnnotationPresent(DctmLocation.class)) {
				Set<String> paths;
				
				if (po instanceof IDfSysObject) {
					IDfSysObject so = (IDfSysObject)po;
					paths = MetadataUtils.getPaths(so);
				} else {
					throw new ParseException("Failed to parse PO to POJO. Location specified but not a SysObject", 0);
				}
				
				String fieldName = field.getName();
				if (field.isAccessible()) {
					field.set(pojo, paths);
				} else {
					setValueUsingSetter(pojo, fieldName, Set.class, paths);
				}
				
				
				
			}
		}
		
		return pojo;
		
	}
	
	
	public Map<String, Object> getAttributeMap(T object) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		
		Map<String, Object> attributes = new HashMap<>();
		
		for (Field field : clazz.getDeclaredFields()) {
			String attributeName = null;
			if (field.isAnnotationPresent(DctmAttribute.class)) {
				attributeName = field.getAnnotation(DctmAttribute.class).value();
				
				String fieldName = field.getName();
				
				if (field.isAccessible()) {
					attributes.put(attributeName, field.get(object));
				} else {
					attributes.put(attributeName, getValueUsingGetter(object, fieldName, field.getDeclaringClass()));
				}
			}
			
		}
		
		return attributes;
	}
	
	
	public String getDctmAttribute(T object, String attr) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		for (Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(DctmAttribute.class)) {
				if (field.getAnnotation(DctmAttribute.class).value().equalsIgnoreCase(attr)) {
					if (field.isAccessible()) {
						return (String) field.get(object);
					} else {
						return getValueUsingGetter(object, field.getName(), String.class);
					}
				}
			}
		}
		return null;
	}
	
	public String getDctmId(T object) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		return getValueForAnnotatedField(object, DctmId.class, String.class);
	}
	
	
	public Set<String> getPaths(T object) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException  {
		return getValueForAnnotatedField(object, DctmLocation.class, Set.class);
	}
	
	public void setDctmId(T object, String id) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		setValueToAnnotatedField(object, DctmId.class, String.class, id);
	}
	
	//Annotation Utils
	
	public <V> V getValueForAnnotatedField(Object object, Class<? extends Annotation> annotation, Class<V> type) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		for (Field field : object.getClass().getDeclaredFields()) {
			if (field.isAnnotationPresent(annotation)) {
				if (field.isAccessible()) {
					return (V) field.get(object);
				} else {
					return getValueUsingGetter(object, field.getName(), type);
				}
			}
		}
		return null;
	}
	
	public <V> void setValueToAnnotatedField(Object object, Class<? extends Annotation> annotation, Class<V> type, V value) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		for (Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(annotation)) {
				if (field.isAccessible()) {
					field.set(object, value);
					return;
				} else {
					setValueUsingSetter(object, field.getName(), type, value);
					return;
				}
			}
		}
	}
	
	private <V> V getValueUsingGetter(Object object, String fieldName, Class<V> type) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		String getterName = String.format("get%s", StringUtils.capitalize(fieldName));
		Method method = clazz.getDeclaredMethod(getterName, null);
		return (V) method.invoke(object, null);
		
	}
	
	
	private <V> void setValueUsingSetter(Object pojo, String fieldName, Class<V> type, V value) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		String setterName = String.format("set%s", StringUtils.capitalize(fieldName));
		Method method = clazz.getDeclaredMethod(setterName, type);
		method.invoke(pojo, value);
	}
	
}
