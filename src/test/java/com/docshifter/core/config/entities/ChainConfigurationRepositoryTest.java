package com.docshifter.core.config.entities;

import com.docshifter.core.AbstractSpringTest;
import com.docshifter.core.config.repositories.ChainConfigurationRepository;
import com.docshifter.core.config.repositories.ModuleConfigurationRepository;
import com.docshifter.core.config.repositories.ModuleRepository;
import com.docshifter.core.config.repositories.NodeRepository;
import com.docshifter.core.config.repositories.ParameterRepository;
import com.docshifter.core.operations.FailureLevel;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

/**
 * Created by michiel.vandriessche@docbyte.com on 9/6/16.
 */
public class ChainConfigurationRepositoryTest extends AbstractSpringTest {

	@Autowired
	ModuleRepository moduleRepository;
	@Autowired
	ParameterRepository parameterRepository;
	@Autowired
	ModuleConfigurationRepository moduleConfigurationRepository;
	@Autowired
	NodeRepository nodeRepository;
	@Autowired
	ChainConfigurationRepository chainConfigurationRepository;

	private static final Logger logger = Logger.getLogger(ChainConfigurationRepositoryTest.class);

	Module m1;
	Module m2;
	Module m4;
	Module m7;
	Module m8;
	Module m9;
	Module m10;
	Module m11;
	Module m12;
	Module m13;
	Module m14;
	Module m15;

	Parameter p1;
	Parameter p2;
	Parameter p3;
	Parameter p4;
	Parameter p7;
	Parameter p8;
	Parameter p9;
	Parameter p10;
	Parameter p11;
	Parameter p13;
	Parameter p14;
	Parameter p15;
	Parameter p16;

	ModuleConfiguration mc1;
	ModuleConfiguration mc2;
	ModuleConfiguration mc3;
	ModuleConfiguration mc4;
	ModuleConfiguration mc7;
	ModuleConfiguration mc8;
	ModuleConfiguration mc9;
	ModuleConfiguration mc10;
	ModuleConfiguration mc11;
	ModuleConfiguration mc12;

	Node n1;
	Node n2;
	Node n3;
	Node n4;
	Node n7;

	ChainConfiguration chc1;

	private String millis;


	@Before
	public void before() {

		millis = "" + System.currentTimeMillis();


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

		m1 = new Module("testmodule1" + millis, "testmodule1" + millis, "testsmodule1" + millis, "input", null,  new HashSet(Arrays.asList(p1)));
		m2 = new Module("testmodule2" + millis, "testmodule2" + millis, "testsmodule2" + millis, "input", null, null);
		m4 = new Module("testmodule4" + millis, "testmodule4" + millis, "testsmodule4" + millis, "input", null, null);
		m7 = new Module("testmodule7" + millis, "testmodule7" + millis, "testsmodule7" + millis, "input", null, null);
		m8 = new Module("testmodule8" + millis, "testmodule8" + millis, "testsmodule8" + millis, "input", null, null);
		m9 = new Module("testmodule9" + millis, "testmodule9" + millis, "testsmodule9" + millis, "input", null, null);
		m10 = new Module("testmodule10" + millis, "testmodule10" + millis, "testsmodule10" + millis, "input", null, null);
		m11 = new Module("testmodule11" + millis, "testmodule11" + millis, "testsmodule11" + millis, "input", null, null);
		m12 = new Module("testmodule12" + millis, "testmodule12" + millis, "testsmodule12" + millis, "input", null, null);


		Map<Parameter, String> map0 = new HashMap<Parameter, String>();
		map0.put(p1, "parameter0");
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
		mc3 = new ModuleConfiguration(m2, "testsconf3" + millis, "testconfiguration3" + millis, map0);
		mc4 = new ModuleConfiguration(m4, "testsconf4" + millis, "testconfiguration4" + millis, map4);
		mc7 = new ModuleConfiguration(m7, "testsconf7" + millis, "testconfiguration7" + millis, map7);
		mc8 = new ModuleConfiguration(m12, "testsconf8" + millis, "testconfiguration8" + millis, map16);
		mc9 = new ModuleConfiguration(m13, "testsconf9" + millis, "testconfiguration9" + millis, map13);
		mc10 = new ModuleConfiguration(m14, "testsconf10" + millis, "testconfiguration10" + millis, map14);
		mc11 = new ModuleConfiguration(m15, "testsconf11" + millis, "testconfiguration11" + millis, map15);
		mc12 = new ModuleConfiguration(m2, "testsconf12" + millis, "testconfiguration12" + millis, map2);



		m1 = moduleRepository.save(m1);
		m2 = moduleRepository.save(m2);
		m4 = moduleRepository.save(m4);
		m7 = moduleRepository.save(m7);
		m8 = moduleRepository.save(m8);
		m9 = moduleRepository.save(m9);
		m10 = moduleRepository.save(m10);
		m11 = moduleRepository.save(m11);
		m12 = moduleRepository.save(m12);


		p1 = parameterRepository.save(p1);
		p2 = parameterRepository.save(p2);
		p3 = parameterRepository.save(p3);
		p4 = parameterRepository.save(p4);
		p7 = parameterRepository.save(p7);
		p8 = parameterRepository.save(p8);
		p9 = parameterRepository.save(p9);
		p10 = parameterRepository.save(p10);
		p11 = parameterRepository.save(p11);
		p13 = parameterRepository.save(p13);
		p14 = parameterRepository.save(p14);
		p15 = parameterRepository.save(p15);
		p16 = parameterRepository.save(p16);


		moduleConfigurationRepository.save(mc1);
		moduleConfigurationRepository.save(mc2);
		moduleConfigurationRepository.save(mc3);
		moduleConfigurationRepository.save(mc4);
		moduleConfigurationRepository.save(mc7);
		moduleConfigurationRepository.save(mc8);
		moduleConfigurationRepository.save(mc9);
		moduleConfigurationRepository.save(mc10);
		moduleConfigurationRepository.save(mc11);
		moduleConfigurationRepository.save(mc12);

		n1 = new Node(null, mc1);
		n2 = new Node(n1, mc2);
		n3 = new Node(n2, mc3);
		n4 = new Node(null, mc4);
		n7 = new Node(null, mc7);


		chc1 = new ChainConfiguration("testchain", "desc", true, n1, null, null, 60, 2, FailureLevel.FILE, UUID.randomUUID());

		chainConfigurationRepository.save(chc1);

		nodeRepository.save(n1);
		nodeRepository.save(n2);
		nodeRepository.save(n3);
		nodeRepository.save(n4);
		nodeRepository.save(n7);

	}

	@Test
	public void findRootNodesByEnabled() throws Exception {
		logger.info(chainConfigurationRepository.findByEnabled(true));
	}

	@Test
	public void findByRootNode() throws Exception {

	}

	@Test
	public void findByQueueName() throws Exception {

	}

}
