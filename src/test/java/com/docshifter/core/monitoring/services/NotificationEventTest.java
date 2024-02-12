package com.docshifter.core.monitoring.services;

import com.docshifter.core.AbstractSpringTest;
import com.docshifter.core.dctm.DctmMetaDataConsts;
import com.docshifter.core.events.NotificationEvent;
import com.docshifter.core.messaging.message.DocshifterMessage;
import com.docshifter.core.monitoring.dtos.ConfigurationDto;
import com.docshifter.core.monitoring.dtos.MailConfigurationItemDto;
import com.docshifter.core.monitoring.dtos.NotificationDto;
import com.docshifter.core.monitoring.enums.NotificationLevels;
import com.docshifter.core.monitoring.utils.EmailPlaceHolderConsts;
import com.docshifter.core.task.Task;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.apache.commons.mail.util.MimeMessageParser;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import jakarta.mail.Address;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class NotificationEventTest extends AbstractSpringTest {

	private static final Logger logger = Logger.getLogger(NotificationEventTest.class);

	private ConfigurationDto sampleConfiguration1 = new ConfigurationDto();

	private MailConfigurationItemDto mailConfigurationItem;

	private CountDownLatch cdl = new CountDownLatch(1);

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private ApplicationEventPublisher publisher;

	@Autowired
	private EventListener listener;

	@Autowired
	private NotificationService notificationService;

	private Task task;

	private ConfigurationDto configuration;

	@RegisterExtension
	static final GreenMailExtension greenMail = new GreenMailExtension(new ServerSetup[]{new ServerSetup(0, null, ServerSetup.PROTOCOL_SMTP), new ServerSetup(0, null, ServerSetup.PROTOCOL_IMAP)})
			.withConfiguration(GreenMailConfiguration.aConfig().withDisabledAuthentication());

	@Test
	public void sendEmailWithPlaceHolders() throws Exception {
		logger.info("Running sendEmailWithPlaceHolders()");
		
		List<String> expected = Arrays.asList("last_checked_in_user@email.com", "rendition_requested_by@email.com",
				"dynamic1@eml.com", "dynamic2@eml.com", "dynamic3@eml.com");

		NotificationDto notification = new NotificationDto();
		notification.setLevel(NotificationLevels.WARN);
		notification.setTaskId(task.getId());
		notificationService.sendNotification(configuration.getId(), notification);

		// Giving extra time to process the notification.
		cdl.await(10000, TimeUnit.MILLISECONDS);

		MimeMessage[] emails = greenMail.getReceivedMessages();

		for (int i = 0; i < emails.length; i++) {
			
			MimeMessage mimeMessage = emails[i];
			
			Address address = mimeMessage.getAllRecipients()[0];

			//Checking expected e-mail Address
			assertTrue(expected.contains(address.toString()));
			
			//Checking content and subject from each e-mail.
			String content = getHtmlContent(mimeMessage);
			// TODO: Uncomment once org.apache.commons.commons-email 2.0 is available and bumped in the POM!
			//assertThat(greenMail.getReceivedMessages()[0].getSubject()).isEqualTo("UnitTest");
		
			//Checking replaced body place holder message.
			// TODO: Uncomment once org.apache.commons.commons-email 2.0 is available and bumped in the POM!
			//assertThat(content).isEqualToIgnoringWhitespace(
			//		"Body:" + DctmMetaDataConsts.GENERIC_EMAIL_ADDRESS_NOT_FOUND_MSG + ", level: WARN");
		}
		
	}

	@Test
	public void eventListenerTest() {
		logger.info("Running eventListenerTest()");
		assertTrue(listener.getMapOfTasks().containsValue(task));
		assertTrue(listener.getMapOfTasks().containsKey(task.getId()));
		assertFalse(listener.getMapOfTasks().isEmpty());
	}

	@BeforeEach
	public void beforeTest() {
		
		logger.info("Running setup beforeTest()");

		// Starting up Green mail.
		greenMail.start();
		Session smtpSession = greenMail.getSmtp().createSession();

		// Filling DTO mail configuration
		mailConfigurationItem = new MailConfigurationItemDto();
		mailConfigurationItem.setToAddresses(EmailPlaceHolderConsts.DCTM_LAST_CHECKED_IN_USER + ","
				+ EmailPlaceHolderConsts.DCTM_RENDITION_REQUESTED_BY + "," + EmailPlaceHolderConsts.DYNAMIC_EMAIL);
		mailConfigurationItem.setHost(smtpSession.getProperty("mail.smtp.host"));
		mailConfigurationItem.setPort(Integer.parseInt(smtpSession.getProperty("mail.smtp.port")));
		mailConfigurationItem.setFromAddress("juan@localhost");
		mailConfigurationItem.setSsl(false);

		mailConfigurationItem.setTemplateBody("Body: {{email_not_found}}, level: {{level}}");
		mailConfigurationItem.setTemplateTitle("UnitTest");
		mailConfigurationItem.setNotificationLevels(Arrays.asList(NotificationLevels.ERROR, NotificationLevels.WARN,
				NotificationLevels.INFO, NotificationLevels.DEBUG));
		mailConfigurationItem.setPassword("randomly");
		sampleConfiguration1.setName("test");
		sampleConfiguration1.setId(1L);
		sampleConfiguration1.setConfigurationItems(Arrays.asList(mailConfigurationItem));

		// Inserting configuration in database.
		configuration = configurationService.addConfiguration(sampleConfiguration1);

		// Creating new task object to send by event.
		task = new Task();
		task.setId("1");

		// Adding list of emails to simulate WS calls with dynamic emails
		List<String> dynamicEmails = new ArrayList<String>();
		dynamicEmails.add("dynamic1@eml.com");
		dynamicEmails.add("dynamic2@eml.com");
		dynamicEmails.add("dynamic3@eml.com");

		// Filling task data....
		task.addData(EmailPlaceHolderConsts.DCTM_LAST_CHECKED_IN_USER, "last_checked_in_user@email.com");
		task.addData(EmailPlaceHolderConsts.DCTM_RENDITION_REQUESTED_BY, "rendition_requested_by@email.com");
		task.addData(EmailPlaceHolderConsts.DYNAMIC_EMAIL, dynamicEmails);
		task.addData(EmailPlaceHolderConsts.EMAIL_ADDRESS_NOT_FOUND,
				DctmMetaDataConsts.GENERIC_EMAIL_ADDRESS_NOT_FOUND_MSG);

		// Publishing event
		publisher.publishEvent(new NotificationEvent(new DocshifterMessage(null, task, null)));
	}

	@AfterEach
	public void tearDown() {
		logger.info("Running teardown tearDown()");
		greenMail.stop();
		List<ConfigurationDto> configs = configurationService.getAll();
		configs.forEach(config -> configurationService.deleteConfiguration(config.getId()));
	}

	private String getHtmlContent(MimeMessage mimeMessage) throws Exception {
		return null;
		// TODO: Uncomment once org.apache.commons.commons-email 2.0 is available and bumped in the POM!
//		MimeMessageParser parser = new MimeMessageParser(mimeMessage);
//		parser.parse();
//		return parser.getHtmlContent();
	}
}
