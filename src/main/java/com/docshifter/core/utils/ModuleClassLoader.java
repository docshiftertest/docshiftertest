package com.docshifter.core.utils;

import com.docshifter.core.config.services.ApplicationContextProvider;
import com.docshifter.core.operations.AbstractOperation;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class ModuleClassLoader {

    private static final Logger logger = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

    private static ModuleClassLoader instance = null;
    private URLClassLoader loader;

    private String modulespath;
    private Map<Class, ApplicationContext> contextCache = new HashMap<>();

    public static ModuleClassLoader getInstance() throws Exception {
        if (instance == null) {
        	initClassLoader();
        	if (instance == null) {
        		throw new InstantiationException("ModuleClassLoader not initialized");
        	}
        }
        return instance;
    }

    public static ModuleClassLoader initClassLoader() throws Exception {
    	return initClassLoader(null);
    }

    public static ModuleClassLoader initClassLoader(String modulespath) throws Exception {
        if (instance == null) {
            instance = new ModuleClassLoader(modulespath);
        }
        return instance;
    }

    private ModuleClassLoader(String modulespath) throws Exception {

        this.modulespath = modulespath;

        //This method is for testing purposes
        //TODO: How to make this generic for testing?
        if (this.modulespath == null){
            this.modulespath = "modules";
        }

        File modulesDir=new File(this.modulespath);
        try {
            this.loader = URLClassLoader.newInstance(ClassHelper.findAllJars(modulesDir), ClassLoader.getSystemClassLoader());
        }
        catch (IllegalArgumentException ex){
            logger.error("Cannot locate the modules directory "+modulesDir.getAbsolutePath(),ex);
            throw new InstantiationException("[" + ex.getClass().getName() + "] " +
            		"Cannot locate the modules directory [" + 
            		modulesDir.getAbsolutePath() + "] Exception was: " +
            		ex.getMessage());
        }
        catch (IOException ex){
            logger.error("Error while accessing modules directory "+modulesDir.getAbsolutePath(),ex);
            throw new Exception("Error while accessing modules directory "+modulesDir.getAbsolutePath(),ex);
        }
    }


    public Object getClassObject(String name) {
        try{
            Class<?> typ=Class.forName(name,true,loader);
            Object obj=typ.getDeclaredConstructor().newInstance();
            return obj;
        } //errors
        catch(ExceptionInInitializerError exiie){
            logger.error("ExceptionInInitializerError while initializing requested operation", exiie);
        } catch(LinkageError lnkErr){
            logger.error("Could not load "+name+", make sure you have bought this module and installed it correctly", lnkErr);
        } //exceptions
        catch(IllegalAccessException ilax){
            logger.error("IllegalAccessException trying to get instance of class named: " + name, ilax);
        } catch(InstantiationException inse){
            logger.error("InstantiationException trying to get instance of class named: " + name, inse);
        } catch(SecurityException sex){
            logger.error("SecurityException trying to get instance of class named: " + name, sex);
        } catch(ClassNotFoundException cnfe){
            logger.error("ClassNotFoundException trying to load "+name+". Make sure you have bought this module and installed it correctly", cnfe);
        } catch (IllegalArgumentException illy) {
        	logger.error("IllegalArgumentException trying to get instance of class named: " + name, illy);
		} catch (InvocationTargetException invy) {
        	logger.error("InvocationTargetException trying to get instance of class named: " + name, invy);
		} catch (NoSuchMethodException nsme) {
			logger.error("NoSuchMethodException trying to get instance of class named: " + name, nsme);
		}
        return null;
    }

    public Object getSpringBean(String name) {
        logger.debug("getSpringBean(" + name + ")");
        Object bean = null;
        try {
            Class type = Class.forName(name, true, loader);
            ApplicationContext context = contextCache.get(type);
            if (context == null) {
                context = createModuleContext(type);
            }
            bean = getBean(context, type);
            if (bean != null && bean instanceof AbstractOperation
                    && ((AbstractOperation) bean).cacheContext()) {
                logger.debug(String.format("Caching Spring context for %s", name));
                contextCache.put(type, context);
            }
        } catch (Exception ex) {
            logger.error("Could not initialize Spring Bean " + name + ", make sure you have bought this module and installed it correctly", ex);
        }
        return bean;
    }

    private ApplicationContext createModuleContext(Class type) {
        logger.debug("Creating module context");
        String packageName = type.getPackage().getName();
        AnnotationConfigApplicationContext moduleContext = new AnnotationConfigApplicationContext();
        moduleContext.setClassLoader(type.getClassLoader());
        moduleContext.setParent(ApplicationContextProvider.getApplicationContext());
        logger.debug(String.format("Scanning package %s", packageName));
        moduleContext.scan(packageName);
        moduleContext.refresh();
        logger.debug("Created module context");
        return moduleContext;
    }

    private Object getBean(ApplicationContext context, Class type) {
        Object bean = null;
        try {
            bean = context.getBean(type);
        } catch (Exception ex) {
            logger.warn(String.format("Cannot find bean of type %s", type.getName()));
            logger.debug(ex);
        }
        return bean;
    }


    public String getModulespath() {
        return modulespath;
    }

    public void setModulespath(String modulespath) {
        this.modulespath = modulespath;
    }
}
