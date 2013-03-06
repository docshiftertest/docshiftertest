package com.docbyte.docshifter.util;



/**
 * Simple wrapper around the Log4JLogger class
 * 
 * @author $Author$
 * @version $Rev$
 * Last Modification Date: $Date$
 *
 */
public class Logger {
	
	public static final org.apache.log4j.Logger log= org.apache.log4j.Logger.getLogger(Logger.class.getName());
	public static void fatal(String message, Throwable t) {
	//	if (log.isFatalEnabled()){
			log.fatal(message,  t);
			/*System.err.print("*** FATAL: message: "+message);
			if(t!=null){
				System.err.print("\n*** ");
				t.printStackTrace();
			}else {
				System.err.println();
			}*/
	//	}
	}

	public static void error(String message, Throwable t) {
		//if (log.isErrorEnabled()){
			log.error(message, t);
			/*System.err.print("*** ERROR: message: "+message);
			if(t!=null){
				System.err.print("\n*** ");
				t.printStackTrace();
			}else {
				System.err.println();
			}*/
		//}
	}

	public static void warn(String message, Throwable t) {
	//	if (log.isWarnEnabled()){
		/*	System.err.print("*** WARN: message: "+message);
			if(t!=null){
				System.err.print("\n*** ");
				t.printStackTrace();
			}else {
				System.err.println();
			}*/
			log.warn(message, t);
	//	}		
	}

	public static void info(String message, Throwable t) {
		
	//	if (log.isInfoEnabled()){
			/*System.err.print("*** INFO: message: "+message);
			if(t!=null){
				System.err.print("\n*** ");
				t.printStackTrace();
			}else {
				System.err.println();
			}*/
			log.info(message, t);
	//	}		
	}

	public static void debug(String message, Throwable t) {
	//	if (log.isDebugEnabled()){
			/*System.err.print("*** DEBUG: message: "+message);
			if(t!=null){
				System.err.print("\n*** ");
				t.printStackTrace();
			}else {
				System.err.println();
			}*/
			log.debug(message, t);
		//}
	}

	public static void trace(String message, Throwable t) {
	//	if (log.isTraceEnabled()){
			/*System.err.print("*** TRACE: message: "+message);
			if(t!=null){
				System.err.print("\n*** ");
				t.printStackTrace();
			}else {
				System.err.println();
			}*/
			log.trace(message, t);
	//	}
	}
}
