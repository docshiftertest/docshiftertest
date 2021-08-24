package com.docshifter.core.config.services;

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
}