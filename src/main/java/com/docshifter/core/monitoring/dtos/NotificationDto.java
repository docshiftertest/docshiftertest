package com.docshifter.core.monitoring.dtos;

import com.docshifter.core.monitoring.enums.NotificationLevels;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;

/**
 * Created by blazejm on 16.05.2017.
 */
public class NotificationDto {
    private NotificationLevels level;
    private String taskId;
    private String message;
    private String sourceFilePath = "";
    private File[] attachments;

    public NotificationLevels getLevel() {
        return level;
    }

    public void setLevel(NotificationLevels level) {
        this.level = level;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public File[] getAttachments() {
        return attachments;
    }

    public void setAttachments(File[] attachments) {
        this.attachments = attachments;
    }

    public String getSourceFilePath() {
        return sourceFilePath;
    }

    public void setSourceFilePath(Path sourceFilePath) {
        this.sourceFilePath = ObjectUtils.toString(sourceFilePath);
    }

    public void setSourceFilePath(String sourceFilePath) {
        this.sourceFilePath = StringUtils.defaultString(sourceFilePath);
    }

    /**
     * @return the hostname and ip from running machine.
     */
	public String getHostname() {
		try {
			return InetAddress.getLocalHost().getHostName() + "-" + InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			return StringUtils.EMPTY;
		}
	}
}
