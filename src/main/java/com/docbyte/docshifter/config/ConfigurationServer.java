package com.docbyte.docshifter.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;

import com.docbyte.docshifter.model.dao.ChainConfigurationDAO;
import com.docbyte.docshifter.model.dao.ModuleConfigurationsDAO;
import com.docbyte.docshifter.model.dao.ModuleDAO;
import com.docbyte.docshifter.model.dao.SenderConfigurationDAO;
import com.docbyte.docshifter.model.vo.ChainConfiguration;
import com.docbyte.docshifter.model.vo.Module;
import com.docbyte.docshifter.model.vo.ModuleConfiguration;
import com.docbyte.docshifter.model.vo.SenderConfiguration;
import com.docbyte.docshifter.util.Logger;

/**
 * Value object which represents the configuration server. Code to communicated
 * with the configuration server should be put in here.
 * 
 * @author $Author$
 * @version $Rev$ Last Modification Date: $Date$
 * 
 */
public class ConfigurationServer {
	private static SenderConfigurationDAO senderConfigurationDAO = new SenderConfigurationDAO();
	private static ChainConfigurationDAO chainConfigurationDAO = new ChainConfigurationDAO();
	private static ModuleConfigurationsDAO moduleConfigurationDAO = new ModuleConfigurationsDAO();
	private static ModuleDAO moduleDAO = new ModuleDAO();

	/**
	 * Method that provides a Set of SenderConfigurationBean objects
	 * representing all the enabled configurations for this ConfigurationServer.
	 * Typically each SenderConfigurationBean will correspond with 1 type of
	 * sender (1 input method).
	 */
	public static Set<SenderConfigurationBean> getEnabledSenderConfigurations() {
		HashSet<SenderConfigurationBean> set = new HashSet<SenderConfigurationBean>();
		List<SenderConfiguration> list = senderConfigurationDAO.getEnabled();

		for (SenderConfiguration c : list) {
			set.add(new SenderConfigurationBean(c));
		}

		return set;
	}

	public static SenderConfigurationBean getSenderConfigurationWS(
			String className, String paramValue) {
		SenderConfiguration sender = senderConfigurationDAO
				.getByClassNameAndParamValue(className, paramValue);

		if (sender != null) {
			return new SenderConfigurationBean(sender);
		} else {
			return null;
		}
	}

	public static TransformationConfigurationBean getPrintserviceTransformationConfiguration(
			String queueName) {
		ChainConfiguration c = chainConfigurationDAO
				.getPrintserviceTransformation(queueName);

		return new TransformationConfigurationBean(c);
	}

	/**
	 * Method that returns the GeneralConfigurationBean assosiated with this
	 * ConfigurationServer.
	 */
	public static GeneralConfigurationBean getGeneralConfiguration() {
		return new GeneralConfigurationBean();
	}

	/**
	 * Method that returns the SenderConfigurationBean assosiated with the given
	 * Configuration UID.
	 * 
	 * @param long uid a long representing the UID of the requested
	 *        SenderConfiguration.
	 */
	public static SenderConfigurationBean getSenderConfiguration(long uid) {
		// return new SenderConfigurationBean(senderConfigurationDAO.get((int)
		// uid));
		return new SenderConfigurationBean(senderConfigurationDAO.get(uid));
	}

	/**
	 * Method that returns the SenderConfigurationBean assosiated with the given
	 * class name
	 * 
	 * @param String
	 *            className a string representing the class name of the
	 *            requested SenderConfiguration.
	 */
	public static List<SenderConfigurationBean> getSenderConfiguration(
			String className) {
		List<SenderConfiguration> senders = senderConfigurationDAO
				.getByClassName(className);
		List<SenderConfigurationBean> beans = new ArrayList<SenderConfigurationBean>();

		for (SenderConfiguration sc : senders) {
			beans.add(new SenderConfigurationBean(sc));
		}

		return beans;
	}

	public static TransformationConfigurationBean getTransformationConfiguration(
			long uid) {
		return new TransformationConfigurationBean(
				chainConfigurationDAO.get((int) uid));
	}

	// TODO throw exception if the configuration is not found
	public static List<ModuleConfiguration> getModuleConfiguration(Module module) {
		try {
			List<ModuleConfiguration> list = moduleConfigurationDAO.get(module);
			return list;
		} catch (HibernateException e) {
			Logger.error("moduleconfiguration not found", e);
			e.printStackTrace();
			return null;
		}
	}

	// TODO throw exception if the module is not found
	public static Module getModule(String name) {
		try {
			Module module = moduleDAO.get(name);
			return module;
		} catch (HibernateException e) {
			Logger.error("module not found", e);
			e.printStackTrace();
			return null;
		}
	}
}
