package com.docbyte.docshifter.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;

import com.docbyte.docshifter.model.dao.ChainConfigurationDAO;
import com.docbyte.docshifter.model.dao.ModuleConfigurationsDAO;
import com.docbyte.docshifter.model.dao.ModuleDAO;
import com.docbyte.docshifter.model.dao.NodeDAO;
import com.docbyte.docshifter.model.vo.ChainConfiguration;
import com.docbyte.docshifter.model.vo.Module;
import com.docbyte.docshifter.model.vo.ModuleConfiguration;
import com.docbyte.docshifter.model.vo.Node;
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
	private static NodeDAO nodeDAO = new NodeDAO();
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
		List<Node> list = nodeDAO.getEnabledSenderConfigurations();
		for(Node n : list)
			set.add(new SenderConfigurationBean(n));
		return set;
	}

	public static SenderConfigurationBean getSenderConfigurationWS(
			String className, String paramValue) {
		SenderConfigurationBean sender = new SenderConfigurationBean(nodeDAO.getSenderByClassNameAndParamValue(className, paramValue));
		return sender;
	}

	public static ChainConfiguration getPrintserviceTransformationConfiguration(
			String queueName) {
		ChainConfiguration c = chainConfigurationDAO.getPrintserviceTransformation(queueName);

		return c;
	}

	/**
	 * Method that returns the GeneralConfigurationBean associated with this
	 * ConfigurationServer.
	 */
	public static GeneralConfigurationBean getGeneralConfiguration() {
		return new GeneralConfigurationBean();
	}

	/**
	 * Method that returns the SenderConfigurationBean associated with the given
	 * Configuration UID.
	 * 
	 * @param long uid a long representing the UID of the requested
	 *        SenderConfiguration.
	 */
	public static SenderConfigurationBean getSenderConfiguration(long uid) {
		// return new SenderConfigurationBean(senderConfigurationDAO.get((int)
		// uid));
		return new SenderConfigurationBean(nodeDAO.get(uid));
	}

	/**
	 * Method that returns the SenderConfigurationBean associated with the given
	 * class name
	 * 
	 * @param String
	 *            className a string representing the class name of the
	 *            requested SenderConfiguration.
	 */
	public static List<SenderConfigurationBean> getSenderConfiguration(
			String className) {
		List<Node> nodes = nodeDAO.getSendersByClassName(className);
		List<SenderConfigurationBean> senders = new ArrayList<SenderConfigurationBean>();
		for(Node n : nodes)
			senders.add(new SenderConfigurationBean(n));
		
		return senders;
	}

	public static ChainConfiguration getTransformationConfiguration(
			long uid) {
		return chainConfigurationDAO.get((int) uid);
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
