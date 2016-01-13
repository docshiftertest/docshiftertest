package com.docbyte.docshifter.model.util;

import com.docbyte.docshifter.model.vo.*;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import java.util.List;

public class HibernateTemplateProvider{
	
	private Configuration config;
	private SessionFactory factory;
	private StandardServiceRegistry serviceRegistry;

	public void setFactory(SessionFactory factory) {
		this.factory = factory;
	}

	private static HibernateTemplateProvider instance;
	
	public static HibernateTemplateProvider getInstance(){
		if(instance == null){
			instance = new HibernateTemplateProvider();
		}
		return instance;
	}
	
	private HibernateTemplateProvider(){
		config = new Configuration()
				.addPackage("com.docbyte.docshifter.model.vo")
				.addAnnotatedClass(Parameter.class)
				.addAnnotatedClass(ChainConfiguration.class)
				.addAnnotatedClass(Node.class)
				.addAnnotatedClass(Module.class)
				.addAnnotatedClass(ModuleConfiguration.class)
				.addAnnotatedClass(GlobalSettings.class);
		config.setProperties(System.getProperties());
		serviceRegistry = new StandardServiceRegistryBuilder().applySettings(
				config.getProperties()).build();
		factory = config.buildSessionFactory(serviceRegistry);
	}
	
	public void delete(Object object){
		Session session = factory.openSession();
		Transaction tx = session.beginTransaction();
		session.delete(object);
		tx.commit();
		session.close();
	}
	
	@SuppressWarnings("rawtypes")
	public Object get(Class c, int id){
		Session session = factory.openSession();
		Transaction tx = session.beginTransaction();
		Object o = session.get(c, id);
		tx.commit();
		session.close();
		
		return o;
	}
	
	@SuppressWarnings("rawtypes")
	public Object get(Class c, long id){
		Session session = factory.openSession();
		Transaction tx = session.beginTransaction();
		Object o = session.get(c, id);
		tx.commit();
		session.close();
		
		return o;
	}
	
	@SuppressWarnings({ "rawtypes" })
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
	
	@SuppressWarnings("rawtypes")
	public List loadAll(Class c){
		Session session = factory.openSession();
		Transaction tx = session.beginTransaction();
		List list = session.createCriteria(c).list();
		tx.commit();
		session.close();
		
		return list;
	}
	
	public void load () {
		instance.load();
	}

	public void merge(Object o) {
		Session session = factory.openSession();
		Transaction tx = session.beginTransaction();
		session.merge(o);
		tx.commit();
		session.close();
	}
}