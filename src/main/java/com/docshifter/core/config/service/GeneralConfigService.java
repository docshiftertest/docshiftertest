package com.docshifter.core.config.service;


import com.docshifter.core.config.Constants;
import com.docshifter.core.config.domain.GlobalSettings;
import com.docshifter.core.config.domain.GlobalSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Value object which represents a GeneralConfiguration. Code to communicated with the configuration server should be put in here.
 * 
 * @author $Author$
 * @version $Rev$
 * Last Modification Date: $Date$
 *
 */
@Service
public class GeneralConfigService {
	protected Map<String, String> params;
	private GlobalSettingsRepository globalSettingsRepository;

	
	/**
	 *
	 * Hardcoded filling in of the values, should actually get filled in with Hibernate info.
	 *
	 * @param globalSettingsRepository
	 */
	@Autowired
	public GeneralConfigService(GlobalSettingsRepository globalSettingsRepository){
		this.globalSettingsRepository = globalSettingsRepository;
		params = new HashMap<String, String>();
		
		GlobalSettings config = this.globalSettingsRepository.findOne(1l);

		if(config != null){
			params.put(Constants.MQ_SYSTEM, config.getMqSystem());
			params.put(Constants.MQ_URL, config.getMqURL());
			params.put(Constants.MQ_QUEUE, config.getMqQueue());
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
		return (String) params.get(name);
	}
/**
	 * @param name the name of the requested parameter
	 * @return the int value linked to the requested parameter
	 */
	public int getInt(String name){
		return Integer.parseInt((String)params.get(name));
	}
	
	public Map<String, String> getParams() {
		return params;
	}
}
