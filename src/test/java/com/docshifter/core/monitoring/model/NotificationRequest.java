package com.docshifter.core.monitoring.model;

import com.docshifter.core.monitoring.enums.NotificationLevels;

/**
 * Created by blazejm on 12.05.2017.
 */
public class NotificationRequest {
    private Long configurationId;
    private NotificationLevels level;
    private String taskId;
    private String message;

    public Long getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(Long configurationId) {
        this.configurationId = configurationId;
    }

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
}
