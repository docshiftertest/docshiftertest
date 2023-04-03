package com.docshifter.core.config.services;

import com.docshifter.core.config.entities.ChainConfiguration;
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

    /**
     * A more "friendly" representation of the output returned by {@link #getBrokenRules()}, as that simply fetches
     * and fills the raw text as stored in the database into this sample. This method actually parses the raw text
     * into a {@link Set} of {@link WorkflowRule}s, analogous to how it's implemented on the real
     * {@link com.docshifter.core.config.entities.ChainConfiguration} entity
     * (see {@link ChainConfiguration#getBrokenRules()}).
     */
    default Set<WorkflowRule> getFriendlyBrokenRules() {
        if (StringUtils.isEmpty(getBrokenRules())) {
            return EnumSet.noneOf(WorkflowRule.class);
        }
        return Collections.unmodifiableSet(
                Arrays.stream(getBrokenRules().split(","))
                        .map(WorkflowRule::valueOf)
                        .collect(Collectors.toCollection(() -> EnumSet.noneOf(WorkflowRule.class)))
        );
    }
}
