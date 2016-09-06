package com.docbyte.docshifter.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.docbyte.docshifter.model.vo.ModuleConfiguration;
import com.docbyte.docshifter.model.vo.Parameter;

public class ModuleBean {

	protected Map<String, String> params;
	private String type;
	private String name;
	private String description;
	private String configName;
	
	public ModuleBean(ModuleConfiguration config){
		this.type = config.getModule().getType();
		this.configName = config.getName();
		this.name = config.getModule().getClassname();
		this.description = config.getDescription();
		
		params = new HashMap<String, String>();


		Iterator<Parameter> it = config.getParameterValues().keySet().iterator();
		while(it.hasNext()){
			Parameter key = it.next();
			String value = config.getParameterValues().get(key);
			params.put(key.getName(), value);
		}
	}
	
	public String toString(){
		return name;
	}
	/**
	 * @param name the name of the requested parameter
	 * @return the String value linked to the requested parameter
	 */
	public String getString(String name) {
		return params.get(name);
	}
	/**
	 * @param name the name of the requested parameter
	 * @return the int value linked to the requested parameter
	 */
	public int getInt(String name){
		return Integer.parseInt(params.get(name));
	}
	/**
	 * @param name the name of the requested parameter
	 * @return the boolean value linked to the requested parameter
	 */
	public boolean getBoolean(String name){
		return Boolean.parseBoolean(params.get(name));
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	public String getConfigName(){
		return configName;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 *
	 * Hardcoded filling in of the values, should actually get filled in with Hibernate info.
	 *
	 * @param int type			an int representing  the Module class (input/transformation/output).
	 * @param long fake		a long representing the Module type (DCTMScheduler/OdfTransformation/DCTMExport)
	 */
	/*
	public ModuleBean(long id){
		params = new HashMap<String, String>();
		
		ModuleConfigurationsDAO moduleConfigurationsDao = new ModuleConfigurationsDAO();
		ModuleConfiguration config = moduleConfigurationsDao.get(id);
		this.type = config.getModule().getType();
		
		Iterator<Parameter> it = config.getParameterValues().keySet().iterator();
		while(it.hasNext()){
			Parameter key = it.next();
			String value = config.getParameterValues().get(key);
			params.put(key.getName(), value);
		}
		
		/*
		if(type == ModuleTypes.INPUT.toString()){
			params.put("end_date", "22/11/2020 16:00");
			params.put("start_date","20/11/2006 16:00");
			params.put("frequency","36");
			this.type="input";
			if(fake==1){
				//	params.put("InputType","com.docbyte.docshifter.sender.dctm.scheduler.DCTMScheduler");
				params.put("dctm_repository","dev_doc");
				params.put("dctm_user","dmadmin");
				params.put("dctm_password","dmadmin");
				name="com.docbyte.docshifter.sender.dctm.scheduler.DCTMScheduler";
				description="dctm input module";
			}else if(fake==2){
				params.put("location_path","c:/test1/");
				params.put("deleteafterimport","false");
				name="com.docbyte.docshifter.sender.fs.scheduler.FSScheduler";
				description="filesystem input module";
			}
		}else if (type==ModuleTypes.TRANSFORMATION.toString()){
			this.type="transformation";
			if(fake==1){
				name="com.docbyte.docshifter.receiver.operations.pdf.PdfATransformation";
				description="PDF/A transformation";
			}else if(fake==2){
				name="com.docbyte.docshifter.receiver.operations.odf.OdfTransformation";
				description="ODF transformation";
			}else if(fake==3){
				name="com.docbyte.docshifter.receiver.operations.odf.OdfTransformation";
				description="ODF transformation";
			}
		}else if(type==ModuleTypes.RELEASE.toString()){
			this.type="release";
			if(fake==1){
				params.put("dctm_repository","dev_doc");
				params.put("dctm_user","dmadmin");
				params.put("dctm_password","dmadmin");
				name="com.docbyte.docshifter.receiver.operations.dctm.SaveOperation";
				description="dctm save module";
			}else if(fake==2){
				params.put("destinationfolder","C:/dsdestination/");
				params.put("deleteafterimport","false");
				name="com.docbyte.docshifter.receiver.operations.filesystem.FSExport";
				description="filesystem release module";
			}else if(fake==3){
				params.put("destinationfolder","C:/dsdestination2/");
				params.put("deleteafterimport","false");
				name="com.docbyte.docshifter.receiver.operations.filesystem.FSExport";
				description="filesystem release module";
			}else if(fake==4){
				params.put("dctm_repository","dev_doc");
				params.put("dctm_user","dmadmin");
				params.put("dctm_password","dmadmin");
				params.put("destination_folder", "/dmadmin/docShifterTest/import");
				params.put("object_type","dm_document");
				name="com.docbyte.docshifter.receiver.operations.dctm.DCTMExport";
				description="dctm export module";
			}else {
				System.out.println("wth "+fake);
			}
		}
	}
	*/
	
}
