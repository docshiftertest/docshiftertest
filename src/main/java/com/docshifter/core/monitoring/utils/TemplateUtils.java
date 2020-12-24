package com.docshifter.core.monitoring.utils;

import com.docshifter.core.monitoring.dtos.NotificationDto;
import com.docshifter.core.monitoring.services.EventListener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by blazejm on 26.05.2017.
 */
public abstract class TemplateUtils {

	private static final Logger logger = Logger.getLogger(com.docshifter.core.monitoring.utils.TemplateUtils.class);

    public static String getTextFromTemplate(String template,
                                             NotificationDto notification,EventListener listener) {
        if (template == null) {
            return "";
        }

        List<String> attachmentNames = getAttachmentNames(notification);

        template = template.replace(EmailPlaceHolderConsts.MESSAGE, notification.getMessage() != null
                ? notification.getMessage()
                : "");
        template = template.replace(EmailPlaceHolderConsts.TASK_ID, notification.getTaskId() != null
                ? notification.getTaskId()
                : "");
        template = template.replace(EmailPlaceHolderConsts.LEVEL, notification.getLevel() != null
                ? notification.getLevel().toString()
                : "");
        
		if (listener != null) {
			//Safe check , because if the task doesn't contains a data we never publish the message and we get a NPE trying to retrieve the task with taskId.
			if(!listener.getMapOfTasks().isEmpty() && listener.getMapOfTasks().get(notification.getTaskId()).getData() != null) {
				
				Map<String, Object> mapOfTasks = listener.getMapOfTasks().get(notification.getTaskId()).getData();
				template = template.replace(EmailPlaceHolderConsts.EMAIL_ADDRESS_NOT_FOUND,
						mapOfTasks.containsKey(EmailPlaceHolderConsts.EMAIL_ADDRESS_NOT_FOUND) ? 
								(String) mapOfTasks.get(EmailPlaceHolderConsts.EMAIL_ADDRESS_NOT_FOUND)	: StringUtils.EMPTY);
			}
			//Even if there's not task and the user added the placeholder EMAIL_ADDRESS_NOT_FOUND we replace it to "" avoiding sending the placeholder.
			else if(template.contains(EmailPlaceHolderConsts.EMAIL_ADDRESS_NOT_FOUND)){
				
				template = template.replace(EmailPlaceHolderConsts.EMAIL_ADDRESS_NOT_FOUND,StringUtils.EMPTY);
			}
		
		}
        
		template = template.replace(EmailPlaceHolderConsts.HOSTNAME, notification.getHostname());
		
        template = template.replace(EmailPlaceHolderConsts.SOURCE_FILE_PATH, notification.getSourceFilePath() != null
                ? notification.getSourceFilePath()
                : StringUtils.EMPTY);
        
        template = template.replace(EmailPlaceHolderConsts.ATTACHMENTS, attachmentNames != null
                ? attachmentNames
                    .stream()
                    .collect(Collectors.joining(", "))
                : "");
        template = template.replace(EmailPlaceHolderConsts.ATTACHMENTS_JSON, attachmentNames != null
                ? "[\"" + attachmentNames
                    .stream()
                    .collect(Collectors.joining("\", \"")) + "\"]"
                : "null");
        String result;
        try {
        	// Do some json escaping
        	result = new ObjectMapper().writeValueAsString(template);
        	// If our result is the same as the template was, except for the
        	// double-quotes around, then strip them off
        	if (template.equals(result.substring(1, result.length() - 1))) {
        		result = result.substring(1);
        		result = result.substring(0, result.length() -1);
        	}
        }
        catch (JsonProcessingException jippy) {
        	logger.error("Caught JsonProcessingException: " + jippy + " trying to get text from the Notification Template!");
        	// Just set the result to the template text and hope for the best?
        	result = template;
        }
        return result;
    }

    public static List<String> getAttachmentNames(NotificationDto notification) {
        return notification.getAttachments() != null
                ? Stream.of(notification.getAttachments())
                .map(a -> a.getName())
                .collect(Collectors.toList())
                : null;
    }
}
