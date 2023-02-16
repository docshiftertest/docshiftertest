package com.docshifter.core.config.services;


import com.docshifter.core.config.Constants;
import com.docshifter.core.config.entities.GlobalSettings;
import com.docshifter.core.config.repositories.GlobalSettingsRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Value object which represents a GeneralConfiguration. Code to communicated with the configuration server should be put in here.
 * 
 * @author $Author$
 * @version $Rev$
 * Last Modification Date: $Date$
 *
 */
@Service
@Log4j2
public class GeneralConfigService {

	protected Map<String, String> params;

	/**
	 *
	 * Hardcoded filling in of the values, should actually get filled in with Hibernate info.
	 *
	 * @param globalSettingsRepository
	 */
	@Autowired
	public GeneralConfigService(GlobalSettingsRepository globalSettingsRepository) {
		params = new HashMap<>();

		log.info("GlobalSettingsRepository.count(): {}", globalSettingsRepository.count());
		Optional<GlobalSettings> optionalConfig = globalSettingsRepository.findById(1L);

		if(optionalConfig.isPresent()){
			GlobalSettings config = optionalConfig.get();
			params.put(Constants.MQ_SYSTEM, config.getMqSystem());
			params.put(Constants.MQ_URL, config.getMqURL());
			params.put(Constants.MQ_QUEUE, config.getMqQueue());
			params.put(Constants.MQ_METRICS_QUEUE, config.getMqMetricsQueue());
			params.put(Constants.MQ_ONGOING_TASK_QUEUE, config.getMqOngoingTaskQueue());
			params.put(Constants.MQ_USER, config.getMqUser());
			params.put(Constants.MQ_PASSWORD, config.getMqUserPassword());
			params.put(Constants.TEMPFOLDER, config.getDefaultTempFolder());
			params.put(Constants.ERRORFOLDER, config.getDefaultErrorFolder());
		}
	}

	/**
	 * @param name the name of the requested parameter
	 * @return the String value linked to the requested parameter
	 */
	public String getString(String name){
		return params.get(name);
	}

	/**
	 * @param name the name of the requested parameter
	 * @return the int value linked to the requested parameter
	 */
	public int getInt(String name){
		return Integer.parseInt(params.get(name));
	}
	
	public Map<String, String> getParams() {
		return params;
	}
}
