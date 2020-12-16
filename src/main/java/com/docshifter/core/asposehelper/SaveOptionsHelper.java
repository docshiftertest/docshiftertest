package com.docshifter.core.asposehelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Convenience class to dump the aspose Save Options
 * Each get of an attribute is surrounded by a try catch so this won't fail in
 * the case of a NSME (for e.g.)
 * Mainly useful for debugging 
 * @author jules
 *
 */
public class SaveOptionsHelper {
	public static final String NEWLINE = System.getProperty("line.separator");

	public static String dumpSaveOptions(com.aspose.cells.SaveOptions options) {
		return genericToString(options);
	}

	public static String dumpSaveOptions(com.aspose.diagram.SaveOptions options) {
		return genericToString(options);
	}

	public static String dumpSaveOptions(com.aspose.email.SaveOptions options) {
		return genericToString(options);
	}

	public static String dumpSaveOptions(com.aspose.email.AppointmentSaveOptions options) {
		return genericToString(options);
	}

	public static String dumpSaveOptions(com.aspose.email.ContactSaveOptions options) {
		return genericToString(options);
	}

	public static String dumpSaveOptions(com.aspose.pdf.SaveOptions options) {
		return genericToString(options);
	}

	public static String dumpSaveOptions(com.aspose.tasks.SaveOptions options) {
		return genericToString(options);
	}

	public static String dumpSaveOptions(com.aspose.tasks.MPPSaveOptions options) {
		return genericToString(options);
	}

	public static String dumpSaveOptions(com.aspose.tasks.SaveTemplateOptions options) {
		return genericToString(options);
	}

	public static String dumpSaveOptions(com.aspose.words.SaveOptions options) {
		return genericToString(options);
	}

	public static String genericToString(Object anObj) {
		return genericToString(anObj, null);
	}

	public static String genericToString(Object anObj, String[] excludedMethods) {
   		StringBuffer sBuf = new StringBuffer();
   		String methodName;
   		sBuf.append(anObj.getClass().getName());
   		sBuf.append(" [");
		Method[] meths = anObj.getClass().getMethods();
   		methods:
		for (Method meth : meths) {
   			methodName = meth.getName();
   			if (excludedMethods != null) {
   				for (String excludeMethod : excludedMethods) {
   					if (methodName.equals(excludeMethod)) {
   						continue methods;
   					}
   				}
   			}
   			if (!methodName.equals("getClass") && methodName.startsWith("get")) {
   				sBuf.append(methodName);
   				sBuf.append("=\"");
   				try {
   					sBuf.append(invokeCommand(anObj, methodName));
   				}
   				catch (Throwable thr) {
   					sBuf.append("** Throwable: " + thr + " getting value **");
   				}
   				sBuf.append("\", ");
   			}
   		}
   		sBuf.append("]");
   		return sBuf.toString();
	}

	private static Object invokeCommand(Object anObj, String methodName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Class<?> cls = anObj.getClass();
		Class<?> parTypes[] = new Class[0];
		Method meth = cls.getMethod(methodName, parTypes);
		Object argList[] = new Object[0];
		Object retObj = meth.invoke(anObj, argList);
		return retObj;
	}
}
