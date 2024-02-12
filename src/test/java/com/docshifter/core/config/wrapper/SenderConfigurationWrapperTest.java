package com.docshifter.core.config.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.reset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import javax.naming.ConfigurationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.docshifter.core.config.entities.ChainConfiguration;
import com.docshifter.core.config.repositories.ChainConfigurationRepository;
import com.docshifter.core.config.entities.Module;
import com.docshifter.core.config.entities.ModuleConfiguration;
import com.docshifter.core.config.entities.Node;
import com.docshifter.core.config.entities.Parameter;
import com.docshifter.core.config.entities.ParameterTypes;

@ExtendWith(MockitoExtension.class)
public class SenderConfigurationWrapperTest {

	private static final String DCTM_USER = "dctm_user";
	private static final String DCTM_MIN_POLL = "dctm_minPoll";
	private static final String DCTM_MAX_POLL = "dctm_maxPoll";
	private static final String DOCSHIFTER = "docshifter";

	@Mock
	private ChainConfigurationRepository chainConfigRepo;

	@Mock
	private ChainConfiguration chainConfig;

	/**
	 * Test for empty String in an Integer param, previously this was throwing a NumberFormatException
	 * @throws ConfigurationException
	 */
	@Test
	public void testEmptyIntegerParam() throws ConfigurationException {
		Map<Parameter, String> paramMap = new HashMap<Parameter, String>();
		paramMap.put(new Parameter(DCTM_MIN_POLL, ParameterTypes.INTEGER), "");
		SenderConfigurationWrapper config = setupForConfigTest(paramMap);
		assertTrue(config.getInt(DCTM_MIN_POLL) == 0, "Min poll should be defaulted to 0");
		assertTrue(config.getInt(DCTM_MIN_POLL, 300) == 300, "Min poll should be defaulted to 300");
		reset();
	}

	/**
	 * Test for bad String in an Integer param, previously this may have thrown a NumberFormatException
	 * @throws ConfigurationException
	 */
	@Test
	public void testBadIntegerParam() throws ConfigurationException {
		Map<Parameter, String> paramMap = new HashMap<Parameter, String>();
		paramMap.put(new Parameter(DCTM_MAX_POLL, ParameterTypes.INTEGER), "QQWA");
		SenderConfigurationWrapper config = setupForConfigTest(paramMap);
		assertTrue(config.getInt(DCTM_MAX_POLL) == 0, "Max poll should be defaulted to 0");
		assertTrue(config.getInt(DCTM_MAX_POLL, 500) == 500, "Max poll should be defaulted to 500");
		reset();
	}

	/**
	 * Test for NULL in an Integer param
	 * @throws ConfigurationException
	 */
	@Test
	public void testNullIntegerParam() throws ConfigurationException {
		Map<Parameter, String> paramMap = new HashMap<Parameter, String>();
		paramMap.put(new Parameter(DCTM_MIN_POLL, ParameterTypes.INTEGER), null);
		SenderConfigurationWrapper config = setupForConfigTest(paramMap);
		assertTrue(config.getInt(DCTM_MIN_POLL) == 0, "Min poll should be defaulted to 0");
		assertTrue(config.getInt(DCTM_MIN_POLL, 300) == 300, "Min poll should be defaulted to 300");
		reset();
	}

	/**
	 * Test for missing Integer params
	 * @throws ConfigurationException
	 */
	@Test
	public void testMissingIntegerParams() throws ConfigurationException {
		Map<Parameter, String> paramMap = new HashMap<Parameter, String>();
		paramMap.put(new Parameter(DCTM_USER, ParameterTypes.STRING), DOCSHIFTER);
		SenderConfigurationWrapper config = setupForConfigTest(paramMap);
		assertTrue(config.getInt(DCTM_MIN_POLL) == 0, "Min poll should be defaulted to 0");
		assertTrue(config.getInt(DCTM_MAX_POLL) == 0, "Max poll should be defaulted to 0");
		assertTrue(config.getInt(DCTM_MIN_POLL, 300) == 300, "Min poll should be defaulted to 300");
		assertTrue(config.getInt(DCTM_MAX_POLL, 500) == 500, "Max poll should be defaulted to 500");
		assertTrue(config.getString(DCTM_USER).equals(DOCSHIFTER), "DCTM User should be set to docshifter");
		assertTrue(config.getString(DCTM_USER, "some other default").equals(DOCSHIFTER), "DCTM User should still be set to docshifter");
		reset();
	}

	/**
	 * Test for empty String in a String param
	 * @throws ConfigurationException
	 */
	@Test
	public void testEmptyStringParam() throws ConfigurationException {
		Map<Parameter, String> paramMap = new HashMap<Parameter, String>();
		paramMap.put(new Parameter(DCTM_USER, ParameterTypes.STRING), "");
		SenderConfigurationWrapper config = setupForConfigTest(paramMap);
		// TODO: Is this correct behaviour?
		assertTrue(config.getString(DCTM_USER).equals(""), "DCTM User should be set to empty String");
		assertTrue(config.getString(DCTM_USER, "some other default").equals(""), "DCTM User should still be set to empty String");
		reset();
	}

	/**
	 * Test for NULL in a String param
	 * @throws ConfigurationException
	 */
	@Test
	public void testNullStringParam() throws ConfigurationException {
		Map<Parameter, String> paramMap = new HashMap<Parameter, String>();
		paramMap.put(new Parameter(DCTM_USER, ParameterTypes.STRING), null);
		SenderConfigurationWrapper config = setupForConfigTest(paramMap);
		// TODO: Is this correct behaviour?
		assertTrue(config.getString(DCTM_USER) == null, "DCTM User should be null");
		assertTrue(config.getString(DCTM_USER, "some other default") == null, "DCTM User should still be null");
		reset();
	}

	/**
	 * Test for missing String param
	 * @throws ConfigurationException
	 */
	@Test
	public void testMissingStringParam() throws ConfigurationException {
		Map<Parameter, String> paramMap = new HashMap<Parameter, String>();
		paramMap.put(new Parameter(DCTM_MIN_POLL, ParameterTypes.INTEGER), "12");
		paramMap.put(new Parameter(DCTM_MAX_POLL, ParameterTypes.INTEGER), "23");
		SenderConfigurationWrapper config = setupForConfigTest(paramMap);
		assertTrue(config.getInt(DCTM_MIN_POLL) == 12, "Min poll should be set to 12");
		assertTrue(config.getInt(DCTM_MAX_POLL) == 23, "Max poll should be set to 23");
		assertTrue(config.getInt(DCTM_MIN_POLL, 300) == 12, "Min poll should still be set to 12");
		assertTrue(config.getInt(DCTM_MAX_POLL, 500) == 23, "Max poll should still be set to 23");
		// TODO: Is this correct behaviour?
		assertTrue(config.getString(DCTM_USER) == null, "DCTM User should be NULL");
		assertTrue(config.getString(DCTM_USER, "some other default").equals("some other default"), "DCTM User should be set to some other default");
		reset();
	}

	/**
	 * Test for NULL in a String param
	 * @throws ConfigurationException
	 */
	@Test
	public void testGetParameterOrDefaultForNullInput() throws ConfigurationException {
		Map<Parameter, String> paramMap = new HashMap<>();
		paramMap.put(new Parameter(DOCSHIFTER, ParameterTypes.STRING), null);
		SenderConfigurationWrapper config = setupForConfigTest(paramMap);
		assertNull(config.getString(DOCSHIFTER), "docshifter should be null");
		assertEquals("docshifter", config.getStringParameterOrDefault(DOCSHIFTER, "docshifter"), "docshifter Should get default parameter");
		reset();
	}

	/**
	 * Test for an empty String in a String param
	 * @throws ConfigurationException
	 */
	@Test
	public void testGetParameterOrDefaultForEmptyStringInput() throws ConfigurationException {
		Map<Parameter, String> paramMap = new HashMap<>();
		paramMap.put(new Parameter(DOCSHIFTER, ParameterTypes.STRING), "");
		SenderConfigurationWrapper config = setupForConfigTest(paramMap);
		assertEquals("", config.getString(DOCSHIFTER), "docshifter should be empty");
		assertEquals("docshifter", config.getStringParameterOrDefault(DOCSHIFTER, "docshifter"), "docshifter Should get default parameter");
		reset();
	}

	/**
	 * Test for some String in a String param
	 * @throws ConfigurationException
	 */
	@Test
	public void testGetParameterOrDefaultForSomeStringInput() throws ConfigurationException {
		Map<Parameter, String> paramMap = new HashMap<>();
		paramMap.put(new Parameter(DOCSHIFTER, ParameterTypes.STRING), "someString");
		SenderConfigurationWrapper config = setupForConfigTest(paramMap);
		assertEquals("someString", config.getString(DOCSHIFTER), "docshifter should be someString");
		assertEquals("someString", config.getStringParameterOrDefault(DOCSHIFTER, "docshifter"), "docshifter Should get the input String parameter");
		reset();
	}

	private SenderConfigurationWrapper setupForConfigTest(Map<Parameter, String> paramMap) throws ConfigurationException {
		// Manually build the ModuleConfiguration with a custom ParamMap
		ModuleConfiguration moduleConfiguration = new ModuleConfiguration(new Module(), "Some Module", "Indescribable", UUID.randomUUID(), paramMap);
		// Since 8.0 we store the x and y positions of a config in a Workflow...
		Node node = new Node(new HashSet<>(), moduleConfiguration, 0d, 0d, false, 1);
		when(chainConfigRepo.findByRootNodes(any(Node.class))).thenReturn(chainConfig);
		SenderConfigurationWrapper config = new SenderConfigurationWrapper(node, chainConfigRepo);
		return config;
	}
}
