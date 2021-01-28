package com.docshifter.core.monitoring.services;

import com.docshifter.core.monitoring.utils.EmailPlaceHolderConsts;
import com.docshifter.core.monitoring.dtos.*;
import com.docshifter.core.monitoring.enums.NotificationLevels;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by blazejm on 12.05.2017.
 */
@Service
public class NotificationServiceImpl implements NotificationService {
    private static final Logger log = Logger.getLogger(com.docshifter.core.monitoring.services.NotificationServiceImpl.class.getName());

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private WebhookNotificationService webhookNotificationService;

    @Autowired
    private SnmpNotificationService snmpNotificationService;

    @Autowired
    private DbNotificationService dbNotificationService;

    @Autowired
    private ExecutorService executorService;
    
    @Autowired
    private EventListener listener;

	@Override
    public void sendNotification(long configurationId, NotificationLevels level, String taskId, String message) {
        log.debug("No attachments, sendNotification(configId=" + configurationId + ", level=" + level.toString() + ", taskId=" + taskId + ", message=" + message + ")");
        sendNotification(configurationId, level, taskId, message, new File[0]);
    }

    @Override
    public void sendNotification(long configurationId, NotificationLevels level, String taskId, String message, File... attachments) {
    	log.debug("With attachments, sendNotification(configId=" + configurationId + ", level=" + level.toString() + ", taskId=" + taskId + ", message=" + message + ")");
    	sendNotification(configurationId, level, taskId, message, null, new File[0]);
    }

    @Override
    public void sendNotification(long configurationId, NotificationDto notification) {
    	log.debug("sendNotification() with configurationId: " + configurationId + " and a NotificationDto object");
    	if (notification != null) {		
    		log.debug("Notification taskId: " + notification.getTaskId()
    			+ "Notification message: " + notification.getMessage());
    	    if (notification.getLevel() != null) {
    	    	log.debug("Notification level: " + notification.getLevel().toString());
    	    }
    	    else {
    	    	log.debug("Notification level is NULL!");
    	    }
    	}
    	else {
    		log.debug("Notification is NULL!");
    	}
        ConfigurationDto configuration = configurationService.getById(configurationId);
        if (configuration == null) {
        	if (notification.getLevel() == NotificationLevels.ERROR || notification.getLevel() == NotificationLevels.WARN) {
        		log.warn("Could not find Configuration by Id: " + configurationId + " to send a Notification with level: " + notification.getLevel());
        	}
        	else {
        		log.debug("Could not find Configuration by Id: " + configurationId + " to send a notification. Level was: " + notification.getLevel());
        	}
        }
        executorService.submit(() -> sendNotificationsInternal(configuration, notification));
    }

    private void sendNotificationsInternal(ConfigurationDto configuration, NotificationDto notification) {
        if (configuration == null) {
            throw new IllegalArgumentException("Configuration not found. See previous warning.");
        }
        if (configuration.getConfigurationItems() != null) {
            configuration.getConfigurationItems()
                    .forEach(item ->
                            sendNotificationInternal(item, notification));
        }
    }

    private void sendNotificationInternal(AbstractConfigurationItemDto item, NotificationDto notification) {
        if (item instanceof MailConfigurationItemDto) {
        	log.debug("Sending email notification(s)... item Id: " + item.getId() + " item type: " + item.getType());
            sendEmails((MailConfigurationItemDto)item, notification);
        }
        if (item instanceof WebhookConfigurationItemDto) {
        	log.debug("Sending webhook notification(s)... item Id: " + item.getId() + " item type: " + item.getType());
        	sendWebhookNotifications((WebhookConfigurationItemDto)item, notification);
        }
        if (item instanceof SnmpConfigurationItemDto) {
        	log.debug("Sending snmp notification(s)... item Id: " + item.getId() + " item type: " + item.getType());
            sendSnmpNotifications((SnmpConfigurationItemDto)item, notification);
        }
        if (item instanceof DbConfigurationItemDto) {
        	log.debug("Sending db notification(s)... item Id: " + item.getId() + " item type: " + item.getType());
        	sendDbNotifications((DbConfigurationItemDto)item, notification);
        }
    }

    private void sendEmails(MailConfigurationItemDto item, NotificationDto notification) {
        if (item.getToAddressList() == null) {
        	log.debug("NOT sending mail... toAddressList is NULL!");
            return;
        }
        if (!matchesNotificationLevel(
                item.getNotificationLevels(), notification.getLevel())) {
        	log.debug("NOT sending mail... no match on Notification Level");
        	return;
        }
        
        //Method to handle WS calls
        handleDynamicEmail(item, notification);
        
		String taskId = notification.getTaskId();
        
        //Sending listener to TemplateUtils to handle email not found placeholder
		item.getToAddressList().stream()
		//Filtering and replace placeholder before sending email
		.filter(f -> !replacePlaceHolder(f, taskId).equalsIgnoreCase(StringUtils.EMPTY))
		//Sending e-mails...
		.forEach(email -> emailService.sendEmail(item, replacePlaceHolder(email, taskId), notification, listener));

	}
    
    /**
     * 
     * Method to handle WS calls,it can handle null task data , arrayList with emails or single email.
     * 
     * @param item {@link MailConfigurationItemDto} get the To addresses.
     * @param notification {@link NotificationDto} to get the exactly task from the listener.
     */
	@SuppressWarnings("unchecked")
	private void handleDynamicEmail(MailConfigurationItemDto item, NotificationDto notification) {

		if (item.getToAddresses().contains(EmailPlaceHolderConsts.DYNAMIC_EMAIL) && listener.getMapOfTasks()
				.get(notification.getTaskId()).getData().get(EmailPlaceHolderConsts.DYNAMIC_EMAIL) != null) {
			
			StringBuilder sb = new StringBuilder();

			String emails = item.getToAddresses();

			emails = item.getToAddresses().replace(EmailPlaceHolderConsts.DYNAMIC_EMAIL, StringUtils.EMPTY);

			sb.append(emails);

			//Retriving message from taskData
			Object dynamicEmail = listener.getMapOfTasks().get(notification.getTaskId()).getData()
					.get(EmailPlaceHolderConsts.DYNAMIC_EMAIL);

			//Check if an arrayList or a single string value
			if (dynamicEmail instanceof ArrayList<?>) {

				ArrayList<String> arrayEmail = (ArrayList<String>) dynamicEmail;

				arrayEmail.forEach(placeHolder -> sb.append("," + placeHolder));
			} else {
				sb.append("," + (String) dynamicEmail);
			}

			item.setToAddresses(sb.toString());
		}
	}
    /**
     * Method to replace place holders email address.
     * 
     * @param placeHolder the placeholder from console or email address
     * @param taskId {@code NotificationDto.getTaskId()} to get the exactly task from the listener.
     * @return the exactly email address or empty string.
     */
    private String replacePlaceHolder(String placeHolder,String taskId) {
    	
		switch (placeHolder) {
		case EmailPlaceHolderConsts.DCTM_RENDITION_REQUESTED_BY:
			
			if(listener.getMapOfTasks().get(taskId).getData().get(EmailPlaceHolderConsts.DCTM_RENDITION_REQUESTED_BY) != null) {
				 placeHolder = placeHolder.replace(placeHolder,
						 (String)listener.getMapOfTasks().get(taskId).getData().get(EmailPlaceHolderConsts.DCTM_RENDITION_REQUESTED_BY));
			}
			else {
				log.info("E-mail not found to placeholder:" + placeHolder);
				placeHolder = placeHolder.replace(placeHolder,StringUtils.EMPTY);
			}
		  
			break;
		case EmailPlaceHolderConsts.DCTM_LAST_CHECKED_IN_USER:
			
			if(listener.getMapOfTasks().get(taskId).getData().get(EmailPlaceHolderConsts.DCTM_LAST_CHECKED_IN_USER) != null) {
				placeHolder = placeHolder.replace(placeHolder,
						(String)listener.getMapOfTasks().get(taskId).getData().get(EmailPlaceHolderConsts.DCTM_LAST_CHECKED_IN_USER));
			}
			else {
				log.info("E-mail not found to placeholder :" + placeHolder);
				placeHolder = placeHolder.replace(placeHolder,StringUtils.EMPTY);
			}
			break;
		}
		return placeHolder;
    }

    private void sendWebhookNotifications(WebhookConfigurationItemDto item, NotificationDto notification) {
        if (matchesNotificationLevel(item.getNotificationLevels(), notification.getLevel())) {
            webhookNotificationService.sendNotification(item, notification);
        }
    }

    private void sendSnmpNotifications(SnmpConfigurationItemDto item, NotificationDto notification) {
        if (matchesNotificationLevel(item.getNotificationLevels(), notification.getLevel())) {
            snmpNotificationService.sendNotification(item, notification);
        }
    }

    private void sendDbNotifications(DbConfigurationItemDto item, NotificationDto notification) {
        if (matchesNotificationLevel(item.getNotificationLevels(), notification.getLevel())) {
            try {
                dbNotificationService.sendNotification(item, notification);
            } catch (SQLException exc) {
                log.error("sendDbNotifications error", exc);
            }
        }
    }

    private boolean matchesNotificationLevel(List<NotificationLevels> levelList, NotificationLevels level) {
        return levelList != null
                && levelList.contains(level);
    }

	@Override
	public void sendNotification(long configurationId, NotificationLevels level, String taskId, String message,
			String sourceFilePath) {
		log.debug("No attachments, sendNotification(configId=" + configurationId + ", level=" + level.toString() + ", taskId=" + taskId + ", message=" + message +
				", sourceFilePath=" + sourceFilePath + ")");
		sendNotification(configurationId, level, taskId, message, sourceFilePath, new File[0]);
	}

	@Override
	public void sendNotification(long configurationId, NotificationLevels level, String taskId, String message,
			String sourceFilePath, File... attachments) {
		log.debug("With attachments, sendNotification(configId=" + configurationId + ", level=" + level.toString() + ", taskId=" + taskId + ", message=" + message +
				", sourceFilePath=" + sourceFilePath + ")");
            NotificationDto notification = new NotificationDto();
            notification.setLevel(level);
            notification.setTaskId(taskId);
            notification.setMessage(message);
            notification.setSourceFilePath(sourceFilePath);
            notification.setAttachments(attachments);
            sendNotification(configurationId, notification);
	}
}
