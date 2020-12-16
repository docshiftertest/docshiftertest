package com.docshifter.core.utils;

import java.util.IllegalFormatException;

public class Logger {

    public static final org.apache.log4j.Logger log= org.apache.log4j.LogManager.getLogger(Logger.class.getName());

    public static void fatal(String message, Throwable t, Object... params) {
        try {
        	log.fatal(String.format(message, params), t);
        }
        catch (IllegalFormatException iffy) {
        	log.fatal(message, t);
        }
    }

    public static void error(String message, Throwable t, Object... params) {
        try {
        	log.error(String.format(message, params), t);
        }
        catch (IllegalFormatException iffy) {
        	log.error(message, t);
        }
    }

    public static void warn(String message, Throwable t, Object... params) {
        try {
        	log.warn(String.format(message, params), t);
        }
        catch (IllegalFormatException iffy) {
        	log.warn(message, t);
        }
    }

    public static void info(String message, Throwable t, Object... params) {
        try {
        	log.info(String.format(message, params), t);
        }
        catch (IllegalFormatException iffy) {
        	log.info(message, t);
        }
    }

    public static void debug(String message, Throwable t, Object... params) {
        try {
        	log.debug(String.format(message, params), t);
        }
        catch (IllegalFormatException iffy) {
        	log.debug(message, t);
        }
    }

    public static void trace(String message, Throwable t, Object... params) {
        try {
        	log.trace(String.format(message, params), t);
        }
        catch (IllegalFormatException iffy) {
        	log.trace(message, t);
        }
    }
}
