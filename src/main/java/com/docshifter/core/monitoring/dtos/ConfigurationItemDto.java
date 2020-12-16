package com.docshifter.core.monitoring.dtos;

import com.docshifter.core.monitoring.dtos.AbstractConfigurationItemDto;
import com.docshifter.core.monitoring.enums.ConfigurationTypes;

import java.util.stream.Collectors;

public class ConfigurationItemDto extends AbstractConfigurationItemDto {
    private int no;
    private ConfigurationTypes type;
    private String levels;

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public String getNotificationLevelStr() {
        if (getNotificationLevels() == null) {
            return "";
        }
        return getNotificationLevels()
                .stream()
                .map(nl -> nl.toString())
                .collect(Collectors.joining(", "));
    }

    @Override
    public ConfigurationTypes getType() {
        return type;
    }

    public void setType(ConfigurationTypes type) {
        this.type = type;
    }

    public String getLevels() {
        return levels;
    }

    public void setLevels(String levels) {
        this.levels = levels;
    }
}
