package com.docshifter.core.config.services;

import com.docshifter.core.config.entities.WorkflowRule;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Created by Juan Marques on 20/07/2021
 * ChainConfiguration sample class used to query only specific columns
 */
public interface ChainConfigurationSample {

    Long getId();

    String getName();

    String getDescription();

    Boolean getEnabled();

    Integer getPriority();

    Long getTimeout();

    UUID getUuid();

    LocalDateTime getLastModifiedDate();

    String getBrokenRules();

    default Set<WorkflowRule> getFriendlyBrokenRules() {
        if (StringUtils.isEmpty(getBrokenRules())) {
            return EnumSet.noneOf(WorkflowRule.class);
        }
        return Arrays.stream(getBrokenRules().split(","))
                .map(WorkflowRule::valueOf)
                .collect(Collectors.toCollection(() -> Collections.unmodifiableSet(EnumSet.noneOf(WorkflowRule.class))));
    }
}
