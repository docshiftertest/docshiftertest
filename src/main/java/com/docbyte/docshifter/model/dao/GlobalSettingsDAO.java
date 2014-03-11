package com.docbyte.docshifter.model.dao;

import java.util.List;

import com.docbyte.docshifter.model.dao.inter.IGlobalSettingsDAO;
import com.docbyte.docshifter.model.util.HibernateTemplateProvider;
import com.docbyte.docshifter.model.vo.GlobalSettings;

public class GlobalSettingsDAO implements IGlobalSettingsDAO {

	private HibernateTemplateProvider hibernateTemplate;
	
	public GlobalSettingsDAO(){
		hibernateTemplate = HibernateTemplateProvider.getInstance();
	}
	
	@SuppressWarnings("unchecked")
	public GlobalSettings get() {
		List<GlobalSettings> list = hibernateTemplate.loadAll(GlobalSettings.class);
		
		if(list.size() >= 1){
			return list.get(0);
		}
		else{
			return null;
		}
	}

	public void save(GlobalSettings config) {
		hibernateTemplate.saveOrUpdate(config);
	}

		public HibernateTemplateProvider getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplateProvider hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}
	
}
