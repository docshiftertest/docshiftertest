package com.docbyte.docshifter.config;

import java.util.HashMap;
import java.util.Map;

import com.docbyte.docshifter.model.dao.GlobalSettingsDAO;
import com.docbyte.docshifter.model.vo.GlobalSettings;

/**
 * Value object which represents a GeneralConfiguration. Code to communicated with the configuration server should be put in here.
 * 
 * @author $Author$
 * @version $Rev$
 * Last Modification Date: $Date$
 *
 */
public class GeneralConfigurationBean {
	protected Map<String, String> params;
	private GlobalSettingsDAO globalSettingsDAO = new GlobalSettingsDAO();

	
	/**
	 *
	 * Hardcoded filling in of the values, should actually get filled in with Hibernate info.
	 *
	 * @param long uid			a long representing the UID of the requested SenderConfiguration.
	 */
	public GeneralConfigurationBean(){
		params = new HashMap<String, String>();
		
		GlobalSettings config = globalSettingsDAO.get();

		if(config != null){
			params.put("jms_system", config.getJmsSystem());
			params.put("jms_url", config.getJmsURL());
			params.put("jms_queue", config.getJmsQueue());
			params.put("jms_user", config.getJmsUser());
			params.put("jms_password", config.getJmsUserPassword());
			params.put("openoffice_host", config.getDefaultOpenOfficeHost());
			params.put("openoffice_port", config.getDefaultOpenOfficePort());
			params.put("tempfolder", config.getDefaultTempFolder());
		}
		
		/*
		params.put("jms_url","tcp://localhost:61616");
		params.put("jms_queue","docShifter.queue");
		params.put("jms_user","");
		params.put("jms_password","");
		params.put("openoffice_host","localhost");
		params.put("openoffice_port", "8101");
		params.put("tempfolder","c:/dstemp/");
		*/
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
