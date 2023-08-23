package com.docshifter.core.utils;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utility class that provides a method for determining whether caching should be performed
 * based on the application name.
 * <p>
 * This class encapsulates the logic for caching decisions based on the application name.
 * </p>
 * <p>
 * Example usage with Spring's caching annotation:
 * <pre>{@code
 *   @Cacheable(value = "isConsumptionBasedLicense", condition = "@cachingDecisionByAppName.shouldCache()")
 *   public boolean isConsumptionBasedLicense() {
 *       // Existing method logic
 *   }
 * }</pre>
 * </p>
 *
 * @author Juan Marques
 * @created 21/08/2023
 */
@Component("cachingDecisionByAppName")
@Log4j2
public class CachingDecision {

    /**
     * The name of the application retrieved from the configuration.
     */
    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * Determines whether caching should be performed based on the application name.
     *
     * @return {@code true} if caching should be performed, {@code false} otherwise.
     */
    public boolean shouldCache() {
        return !"console".equals(applicationName);
    }
}