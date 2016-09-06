package com.docbyte.docshifter;

import com.docbyte.docshifter.model.dao.ModuleConfigurationsDAO;
import com.docbyte.docshifter.model.dao.ModuleDAO;
import com.docbyte.docshifter.model.dao.NodeDAO;
import com.docbyte.docshifter.model.dao.ParameterDAO;
import com.docbyte.docshifter.model.vo.Module;
import com.docbyte.docshifter.model.vo.ModuleConfiguration;
import com.docbyte.docshifter.model.vo.Node;
import com.docbyte.docshifter.model.vo.Parameter;
import com.docbyte.docshifter.util.ParameterTypes;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestDB {

	
	@Test
	public void testInsertNode() {	
		List<Node> beforelist = nodedao.get();
		long before = beforelist.size();
		
		moduledao.insert(m4);
		parameterdao.save(p4);
		mcdao.insert(mc4);
		nodedao.insert(n4);
		
		List<Node> afterlist = nodedao.get();
		long after = afterlist.size();
		
		assertEquals(before + 1, after);
	}
	
	@Test
	public void testUpdateNode() {
		parameterdao.save(p1);
		parameterdao.save(p2);
		moduledao.insert(m2);
		mcdao.insert(mc2);
		nodedao.insert(n2);
		
		n2.getModuleConfiguration().getParameterValues().clear();
		
		mcdao.insert(mc12);
		
		List<Node> nodelist = nodedao.get();
		Node lastnode = nodelist.get(nodelist.size() - 1);
				
		lastnode.setModuleConfiguration(mc12);
				
		nodedao.update(lastnode);
				
		assertEquals("testsconf12" + millis, lastnode.getModuleConfiguration().getName());
	}
	
	@Test
	public void testGetNode() {
		moduledao.insert(m1);
		parameterdao.save(p3);
		mcdao.insert(mc1);
		nodedao.insert(n1);
		
		List<Node> fullist = nodedao.get();
		long id = fullist.get(fullist.size() - 1).getId();
		
		List<Node> nodelist = new ArrayList<Node>();
		Node lastnode = nodedao.get(id);
		nodelist.add(nodedao.get(id));
		
		assertEquals(1, nodelist.size());
		assertNotNull(lastnode);
	}
		
	@Test
	public void testDeleteNode() {		
		moduledao.insert(m7);
		parameterdao.save(p7);
		mcdao.insert(mc7);
		n7 = nodedao.insert(n7);
		nodedao.insert(n7);nodedao.insert(n7);

		System.out.print(n7.getId());
		
		List<Node> beforelist = nodedao.get();
		long before = beforelist.size();

		System.out.print(n7.getId());

		//nodedao.delete(n7);

		System.out.print(n7.getId());

		List<Node> afterlist = nodedao.get();
		long after = afterlist.size();
		
		assertEquals(before - 1, after);
	}
	
	@Test
	public void testInsertModule() {
		List<Module> beforelist = moduledao.getModules();
		long before = beforelist.size();
		
		moduledao.insert(m8);
		
		List<Module> afterlist = moduledao.getModules();
		long after = afterlist.size();
		
		assertEquals(before + 1, after);
	}
	
	@Test
	public void testUpdateModule() {
		moduledao.insert(m9);
				
		m9.setName("testingmodule9");
		
		moduledao.update(m9);
				
		assertEquals("testingmodule9", m9.getName());
		
		//data deletion after test
		moduledao.delete(m9);
	}
	
	@Test
	public void testGetModule() {		
		moduledao.insert(m10);
		
		List<Module> modulelist = moduledao.getModules();
		Module lastmodule = modulelist.get(modulelist.size() - 1);
		
		assertEquals("testmodule10" + millis, lastmodule.getName());
	}
	
	@Test
	public void testDeleteModule() {
		moduledao.insert(m11);
		
		List<Module> beforelist = moduledao.getModules();
		long before = beforelist.size();

		moduledao.delete(m11);

		List<Module> afterlist = moduledao.getModules();
		long after = afterlist.size();
		
		assertEquals(before - 1, after);
	}
	
	@Test
	public void testInsertParameter() {
		List<Parameter> beforelist = parameterdao.get();
		long before = beforelist.size();
		
		parameterdao.save(p8);
		
		List<Parameter> afterlist = parameterdao.get();
		long after = afterlist.size();
		
		assertEquals(before + 1, after);
	}
	
	@Test
	public void testUpdateParameter() {
		parameterdao.save(p9);
				
		p9.setName("testparameter9");
		
		parameterdao.update(p9);
				
		assertEquals("testparameter9", p9.getName());
	}
	
	@Test
	public void testGetParameter() {
		parameterdao.save(p10);
		
		List<Parameter> parameterlist = parameterdao.get();
		Parameter lastparam = parameterlist.get(parameterlist.size() - 1);
		
		assertEquals("parameter10" + millis, lastparam.getName());
	}
	
	@Test
	public void testDeleteParameter() {		
		parameterdao.save(p11);
		
		List<Parameter> beforelist = parameterdao.get();
		long before = beforelist.size();
				
		parameterdao.delete(p11);
		
		List<Parameter> afterlist = parameterdao.get();
		long after = afterlist.size();
		
		assertEquals(before - 1, after);
	}
	
	@Test
	public void testInsertModuleConfiguration() {		
		List<ModuleConfiguration> beforelist = mcdao.get();
		long before = beforelist.size();
		
		moduledao.insert(m12);
		parameterdao.save(p16);		
		mcdao.insert(mc8);
				
		List<ModuleConfiguration> afterlist = mcdao.get();
		long after = afterlist.size();
		
		assertEquals(before + 1, after);
	}
	
	@Test
	public void testUpdateModuleConfiguration() {		
		parameterdao.save(p13);
		mcdao.insert(mc9);
				
		mc9.setName("testingconfiguration9");
		
		mcdao.update(mc9);
				
		assertEquals("testingconfiguration9", mc9.getName());
	}
	
	@Test
	public void testGetModuleConfiguration() {
		parameterdao.save(p14);
		mcdao.insert(mc10);
		
		List<ModuleConfiguration> moduleconflist = mcdao.get();
		ModuleConfiguration lastconf = moduleconflist.get(moduleconflist.size() - 1);
		
		assertEquals("testsconf10" + millis, lastconf.getName());
	}
	
	@Test
	public void testDeleteModuleConfiguration() {
		parameterdao.save(p15);
		mcdao.insert(mc11);
		
		List<ModuleConfiguration> beforelist = mcdao.get();
		long before = beforelist.size();
				
		mcdao.delete(mc11);
		
		List<ModuleConfiguration> afterlist = mcdao.get();
		long after = afterlist.size();
				
		assertEquals(before - 1, after);
	}
}
