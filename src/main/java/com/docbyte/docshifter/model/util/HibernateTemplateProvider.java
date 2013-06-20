package com.docbyte.docshifter.model.util;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import com.docbyte.docshifter.model.vo.ChainConfiguration;
import com.docbyte.docshifter.model.vo.GlobalSettings;
import com.docbyte.docshifter.model.vo.Module;
import com.docbyte.docshifter.model.vo.ModuleConfiguration;
import com.docbyte.docshifter.model.vo.Parameter;
import com.docbyte.docshifter.model.vo.ReceiverConfiguration;
import com.docbyte.docshifter.model.vo.SenderConfiguration;

public class HibernateTemplateProvider{
	
	private Configuration config;
	private SessionFactory factory;
	
	public static HibernateTemplateProvider instance;
	
	public static HibernateTemplateProvider getInstance(){
		if(instance == null){
			instance = new HibernateTemplateProvider();
		}
		return instance;
	}
	
	private HibernateTemplateProvider(){
		config = new Configuration();
		config.addClass(ChainConfiguration.class);
		config.addClass(GlobalSettings.class);
		config.addClass(Module.class);
		config.addClass(ModuleConfiguration.class);
		config.addClass(Parameter.class);
		config.addClass(ReceiverConfiguration.class);
		config.addClass(SenderConfiguration.class);
		config.setProperties(System.getProperties());
		factory = config.configure().buildSessionFactory();
	}
	
	public void delete(Object object){
		Session session = factory.openSession();
		Transaction tx = session.beginTransaction();
		session.delete(object);
		tx.commit();
		session.close();
	}
	
	@SuppressWarnings("unchecked")
	public Object get(Class c, long id){
		Session session = factory.openSession();
		Transaction tx = session.beginTransaction();
		Object o = session.get(c, id);
		tx.commit();
		session.close();
		
		return o;
	}
	
	@SuppressWarnings("unchecked")
	public Object get(Class c, int id){
		Session session = factory.openSession();
		Transaction tx = session.beginTransaction();
		Object o = session.get(c, id);
		tx.commit();
		session.close();
		
		return o;
	}
	
	@SuppressWarnings("unchecked")
	public List find(String query){
		Session session = factory.openSession();
		Transaction tx = session.beginTransaction();
		
		Query q = session.createQuery(query);
		List list = q.list();
		
		tx.commit();
		session.close();
		
		return list;
	}
	
	public void saveOrUpdate(Object o){
		Session session = factory.openSession();
		Transaction tx = session.beginTransaction();
		session.saveOrUpdate(o);
		tx.commit();
		session.close();
	}
	
	@SuppressWarnings("unchecked")
	public List loadAll(Class c){
		Session session = factory.openSession();
		Transaction tx = session.beginTransaction();
		List list = session.createCriteria(c).list();
		tx.commit();
		session.close();
		
		return list;
	}

	public void merge(Object o) {
		Session session = factory.openSession();
		Transaction tx = session.beginTransaction();
		session.merge(o);
		tx.commit();
		session.close();
	}
}