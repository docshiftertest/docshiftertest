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
	private Module m1;
	private Module m2;
	private Module m4;
	private Module m7;
	private Module m8;
	private Module m9;
	private Module m10;
	private Module m11;
	private Module m12;
	private Module m13;
	private Module m14;
	private Module m15;
	
	private Parameter p1;
	private Parameter p2;
	private Parameter p3;
	private Parameter p4;
	private Parameter p7;
	private Parameter p8;
	private Parameter p9;
	private Parameter p10;
	private Parameter p11;
	private Parameter p13;
	private Parameter p14;
	private Parameter p15;
	private Parameter p16;
	
	private ModuleConfiguration mc1;
	private ModuleConfiguration mc2;
	private ModuleConfiguration mc4;
	private ModuleConfiguration mc7;
	private ModuleConfiguration mc8;
	private ModuleConfiguration mc9;
	private ModuleConfiguration mc10;
	private ModuleConfiguration mc11;
	private ModuleConfiguration mc12;
	
	private Node n1;
	private Node n2;
	private Node n4;
	private Node n7;
	
	private ModuleDAO moduledao;
	private ParameterDAO parameterdao;
	private ModuleConfigurationsDAO mcdao;
	private NodeDAO nodedao;
	
	private String millis;
	
	@Before
	public void before() {
		moduledao = new ModuleDAO();
		parameterdao = new ParameterDAO();
		mcdao = new ModuleConfigurationsDAO();
		nodedao = new NodeDAO();
		
		millis = "" + System.currentTimeMillis();
		
		m1 = new Module("testmodule1" + millis, "testmodule1" + millis, "testsmodule1" + millis, "input", null, null);
		m2 = new Module("testmodule2" + millis, "testmodule2" + millis, "testsmodule2" + millis, "input", null, null);
		m4 = new Module("testmodule4" + millis, "testmodule4" + millis, "testsmodule4" + millis, "input", null, null);
		m7 = new Module("testmodule7" + millis, "testmodule7" + millis, "testsmodule7" + millis, "input", null, null);
		m8 = new Module("testmodule8" + millis, "testmodule8" + millis, "testsmodule8" + millis, "input", null, null);
		m9 = new Module("testmodule9" + millis, "testmodule9" + millis, "testsmodule9" + millis, "input", null, null);
		m10 = new Module("testmodule10" + millis, "testmodule10" + millis, "testsmodule10" + millis, "input", null, null);
		m11 = new Module("testmodule11" + millis, "testmodule11" + millis, "testsmodule11" + millis, "input", null, null);
		m12 = new Module("testmodule12" + millis, "testmodule12" + millis, "testsmodule12" + millis, "input", null, null);
		
		p1 = new Parameter("parameter1" + millis, "parameter1" + millis, ParameterTypes.STRING);
		p2 = new Parameter("parameter2" + millis, "parameter2" + millis, ParameterTypes.STRING);
		p3 = new Parameter("parameter3" + millis, "parameter3" + millis, ParameterTypes.STRING);
		p4 = new Parameter("parameter4" + millis, "parameter4" + millis, ParameterTypes.STRING);
		p7 = new Parameter("parameter7" + millis, "parameter7" + millis, ParameterTypes.STRING);
		p8 = new Parameter("parameter8" + millis, "parameter8" + millis, ParameterTypes.STRING);
		p9 = new Parameter("parameter9" + millis, "parameter9" + millis, ParameterTypes.STRING);
		p10 = new Parameter("parameter10" + millis, "parameter10" + millis, ParameterTypes.STRING);
		p11 = new Parameter("parameter11" + millis, "parameter11" + millis, ParameterTypes.STRING);
		p13 = new Parameter("parameter13" + millis, "parameter13" + millis, ParameterTypes.STRING);
		p14 = new Parameter("parameter14" + millis, "parameter14" + millis, ParameterTypes.STRING);
		p15 = new Parameter("parameter15" + millis, "parameter15" + millis, ParameterTypes.STRING);
		p16 = new Parameter("parameter16" + millis, "parameter16" + millis, ParameterTypes.STRING);
		
		Map<Parameter, String> map1 = new HashMap<Parameter, String>();
		map1.put(p1, "parameter1");
		Map<Parameter, String> map2 = new HashMap<Parameter, String>();
		map2.put(p2, "parameter2");
		Map<Parameter, String> map3 = new HashMap<Parameter, String>();
		map3.put(p3, "parameter3");
		Map<Parameter, String> map4 = new HashMap<Parameter, String>();
		map4.put(p4, "parameter4");
		Map<Parameter, String> map7 = new HashMap<Parameter, String>();
		map7.put(p7, "parameter7");
		Map<Parameter, String> map13 = new HashMap<Parameter, String>();
		map13.put(p13, "parameter13");
		Map<Parameter, String> map14 = new HashMap<Parameter, String>();
		map14.put(p14, "parameter14");
		Map<Parameter, String> map15 = new HashMap<Parameter, String>();
		map15.put(p15, "parameter15");
		Map<Parameter, String> map16 = new HashMap<Parameter, String>();
		map16.put(p16, "parameter16");
		
		mc1 = new ModuleConfiguration(m1, "testsconf1" + millis, "testconfiguration1" + millis, map3);
		mc2 = new ModuleConfiguration(m2, "testsconf2" + millis, "testconfiguration2" + millis, map1);
		mc4 = new ModuleConfiguration(m4, "testsconf4" + millis, "testconfiguration4" + millis, map4);
		mc7 = new ModuleConfiguration(m7, "testsconf7" + millis, "testconfiguration7" + millis, map7);
		mc8 = new ModuleConfiguration(m12, "testsconf8" + millis, "testconfiguration8" + millis, map16);
		mc9 = new ModuleConfiguration(m13, "testsconf9" + millis, "testconfiguration9" + millis, map13);
		mc10 = new ModuleConfiguration(m14, "testsconf10" + millis, "testconfiguration10" + millis, map14);
		mc11 = new ModuleConfiguration(m15, "testsconf11" + millis, "testconfiguration11" + millis, map15);
		mc12 = new ModuleConfiguration(m2, "testsconf12" + millis, "testconfiguration12" + millis, map2);
		
		n1 = new Node(null, mc1);
		n2 = new Node(null, mc2);
		n4 = new Node(null, mc4);
		n7 = new Node(null, mc7);
	}
	
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
