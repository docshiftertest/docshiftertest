package com.docbyte.docshifter;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.TopicConnection;
import javax.naming.ConfigurationException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.docbyte.docshifter.config.ConfigurationServer;
import com.docbyte.docshifter.config.Constants;
import com.docbyte.docshifter.config.GeneralConfigurationBean;
import com.docbyte.docshifter.config.Task;
import com.docbyte.docshifter.messaging.factory.IConnectionFactory;
import com.docbyte.docshifter.messaging.factory.MessagingConnectionFactory;
import com.docbyte.docshifter.messaging.queue.information.Information;
import com.docbyte.docshifter.messaging.queue.sender.IMessageSender;
import com.docbyte.docshifter.messaging.queue.sender.JMSSender;
import com.docbyte.docshifter.model.dao.ChainConfigurationDAO;
import com.docbyte.docshifter.model.dao.ModuleConfigurationsDAO;
import com.docbyte.docshifter.model.dao.ModuleDAO;
import com.docbyte.docshifter.model.dao.NodeDAO;
import com.docbyte.docshifter.model.dao.ParameterDAO;
import com.docbyte.docshifter.model.vo.ChainConfiguration;
import com.docbyte.docshifter.model.vo.Module;
import com.docbyte.docshifter.model.vo.ModuleConfiguration;
import com.docbyte.docshifter.model.vo.Node;
import com.docbyte.docshifter.model.vo.Parameter;
import com.docbyte.docshifter.util.ParameterTypes;
import com.docbyte.docshifter.work.WorkFolder;
import com.docbyte.docshifter.work.WorkFolderManager;

public class TestJMS {
	private GeneralConfigurationBean config;
	private TopicConnection connection;
	private IConnectionFactory connectionfactory;
	private IMessageSender sender;
	private WorkFolderManager manager;
	private WorkFolder workfolder;
	private ChainConfigurationDAO ccdao;
	private NodeDAO nodedao;
	private ModuleConfigurationsDAO mcdao;
	private ModuleDAO moduledao;
	private ParameterDAO parameterdao;
	private ChainConfiguration cc;
	
	/* jms server used: glassfish
	 * to start jms server, run following command in Documents folder: glassfish4/bin/asadmin start-domain 
	 * config server: http://localhost:4848 */
	
	@Before
	public void before() {
		config = ConfigurationServer.getGeneralConfiguration();
		String user = config.getString(Constants.JMS_USER);
		String password = config.getString(Constants.JMS_PASSWORD);
		String url = config.getString(Constants.JMS_URL);
		ccdao = new ChainConfigurationDAO();
		nodedao = new NodeDAO();
		mcdao = new ModuleConfigurationsDAO();
		moduledao = new ModuleDAO();
		parameterdao = new ParameterDAO();
		String millis = "" + System.currentTimeMillis();
		
		Module m = new Module("module" + millis, "module" + millis, "module" + millis, "input", null, null);
		Parameter p = new Parameter("parameter" + millis, "parameter" + millis, ParameterTypes.STRING);
		Map<Parameter, String> map = new HashMap<Parameter, String>();
		map.put(p, "parameter" + millis);
		ModuleConfiguration mc = new ModuleConfiguration(m, "moduleconf" + millis, "moduleconf" + millis, map);
		Node n= new Node(null, mc);
		cc = new ChainConfiguration("chainconftest" + millis, "chainconftest" + millis, true, n, null, config.getString(Constants.JMS_QUEUE));
		try {
			moduledao.insert(m);
			parameterdao.save(p);
			mcdao.insert(mc);
			nodedao.insert(n);
			ccdao.save(cc);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		try {
			connectionfactory = MessagingConnectionFactory.getConnectionFactory(user, password, url);		
			sender = JMSSender.getInstance(config.getString(Constants.JMS_QUEUE));
			manager = WorkFolderManager.getInstance();
			workfolder = manager.getNewWorkfolder("work");
			
			connection = connectionfactory.createTopicConnection();
			connection.start();			
		} catch (JMSException | ConfigurationException | IOException e) {
			e.printStackTrace();
		}
	}
	
	/* Test fails because there are no messages on the queue */
	@Test
	public void testSendMessage() {
		String queuename = config.getString(Constants.JMS_QUEUE);
		File file = new File(workfolder.toString() + File.separator + "test.txt");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				Assert.fail("Exception: " + e);
			}
		}
		
		Task task = new Task(file.getAbsolutePath(), workfolder);
		
		try {
			sender.run();
			sender.sendTask(queuename, task);
		} catch (JMSException e) {
			e.printStackTrace();
			Assert.fail("Exception: " + e);
		}
		
		Information info = new Information();
		int nrOfMessages = info.getNumberOfMessages();
		assertEquals(1, nrOfMessages);
	}
	
	@After
	public void after() {
		try {
			connection.stop();
			connection.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
		File folder = new File(workfolder.toString());
		if (folder.exists()) {
			manager.deleteWorkfolder(workfolder);
		}
		ccdao.delete(cc);
	}
}
