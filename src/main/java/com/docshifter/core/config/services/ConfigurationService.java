package com.docshifter.core.config.services;


import com.docshifter.core.config.wrapper.SenderConfigurationWrapper;

import lombok.extern.log4j.Log4j2;

import com.docshifter.core.config.Constants;
import com.docshifter.core.config.entities.ChainConfiguration;
import com.docshifter.core.config.repositories.ChainConfigurationRepository;
import com.docshifter.core.config.entities.Module;
import com.docshifter.core.config.entities.ModuleConfiguration;
import com.docshifter.core.config.repositories.ModuleConfigurationRepository;
import com.docshifter.core.config.repositories.ModuleRepository;
import com.docshifter.core.config.entities.Node;
import com.docshifter.core.config.repositories.NodeRepository;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Value object which represents the configuration server. Code to communicated
 * with the configuration server should be put in here.
 * 
 * @author $Author$
 * @version $Rev$ Last Modification Date: $Date$
 * 
 */
@Service
@Log4j2
public class ConfigurationService {

	private NodeRepository nodeRepository;
	private ChainConfigurationRepository chainConfigurationRepository;
	private ModuleRepository moduleRepository;
	private ModuleConfigurationRepository moduleConfigurationRepository;
	private GeneralConfigService generalConfigService;


	@Autowired
	public ConfigurationService(NodeRepository nodeRepository, ChainConfigurationRepository chainConfigurationRepository, ModuleRepository moduleRepository, ModuleConfigurationRepository moduleConfigurationRepository, GeneralConfigService generalConfigService) {
		this.nodeRepository = nodeRepository;
		this.chainConfigurationRepository = chainConfigurationRepository;
		this.moduleRepository = moduleRepository;
		this.moduleConfigurationRepository = moduleConfigurationRepository;
		this.generalConfigService = generalConfigService;
	}

	public GeneralConfigService getGeneralConfiguration() {
		return generalConfigService;
	}

	/**
	 * Method that provides a Set of SenderConfigurationWrapper objects
	 * representing all the enabled configurations for this ConfigurationServer.
	 * Typically each SenderConfigurationWrapper will correspond with 1 type of
	 * sender (1 input method).
	 */
	public Set<SenderConfigurationWrapper> getEnabledSenderConfigurations() {
		HashSet<SenderConfigurationWrapper> set = new HashSet<SenderConfigurationWrapper>();
//		List<Node> list = nodeDAO.getEnabledSenderConfigurations();
		//TODO is this working?
		List<Node> list = nodeRepository.getEnabledSenderConfigurations();



		for(Node n : list)
			set.add(new SenderConfigurationWrapper(n, chainConfigurationRepository));
		return set;
	}

	public SenderConfigurationWrapper getSenderConfigurationWS(
			String className, String paramValue) {
		SenderConfigurationWrapper sender = new SenderConfigurationWrapper(nodeRepository.getSenderByClassNameAndParamValue(className, paramValue), chainConfigurationRepository);
		return sender;
	}

	public ChainConfiguration getPrintserviceTransformationConfiguration(
			String queueName) {
		ChainConfiguration c = chainConfigurationRepository.findByQueueName(queueName);

		return c;
	}


	/**
	 * Method that returns the SenderConfigurationWrapper associated with the given
	 * Configuration UID.
	 * 
	 * Cached by uid parameter to avoid unnecessary database calls and instances unnecessary creation.
	 * 
	 * @param uid a long representing the UID of the requested
	 *        SenderConfiguration.
	 */
	@Cacheable(value = Constants.SENDER_CONFIGURATION_CACHE, key = "#uid")
	public Set<SenderConfigurationWrapper> getSenderConfiguration(long uid) {
		Optional<ChainConfiguration> cc = chainConfigurationRepository.findById(uid);
		if (cc.isPresent()) {
			return cc.get().getRootNodes().stream()
					.map(n -> new SenderConfigurationWrapper(n, chainConfigurationRepository))
					.collect(Collectors.toUnmodifiableSet());
		}
		else {
			log.error("Could not find ChainConfiguration (Workflow) using ID:{}. This may be because you have added/deleted Workflows and there are still messages on the Q referring to the old Workflow!", uid);
		}
		return null;
	}

	/**
	 * Method that returns the SenderConfigurationWrapper associated with the given
	 * class name
	 * 
	 * @param className
	 *            a string representing the class name of the
	 *            requested SenderConfiguration.
	 */
	public List<SenderConfigurationWrapper> getSenderConfiguration(
			String className) {
		List<Node> nodes = nodeRepository.getSendersByClassName(className);
		List<SenderConfigurationWrapper> senders = new ArrayList<SenderConfigurationWrapper>();
		for(Node n : nodes)
			senders.add(new SenderConfigurationWrapper(n, chainConfigurationRepository));
		
		return senders;
	}

	public ChainConfiguration getTransformationConfiguration(
			long uid) {
		return chainConfigurationRepository.findById(uid).get();
	}

	// TODO throw exception if the configuration is not found
	public List<ModuleConfiguration> getModuleConfiguration(Module module) {
		try {
			List<ModuleConfiguration> list = moduleConfigurationRepository.findByModule(module);
			return list;
		} catch (HibernateException e) {
			log.error("moduleconfiguration not found", e);
			return null;
		}
	}

	// TODO throw exception if the module is not found
	public Module getModule(String name) {
		try {
			Module module = moduleRepository.findOneByName(name);
			return module;
		} catch (HibernateException e) {
			log.error("module not found", e);
			return null;
		}
	}

	public GeneralConfigService getGeneralConfigService() {
		return generalConfigService;
	}
}
