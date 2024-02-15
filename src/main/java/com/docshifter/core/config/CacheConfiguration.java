package com.docshifter.core.config;

import com.docshifter.core.config.entities.ChainConfiguration;
import com.docshifter.core.config.entities.ModuleConfiguration;
import com.docshifter.core.config.entities.Node;
import com.docshifter.core.config.entities.Parameter;
import com.docshifter.core.config.entities.ParameterDependency;
import com.docshifter.core.monitoring.entities.AbstractConfigurationItem;
import com.docshifter.core.monitoring.entities.MonitoringFilter;
import lombok.extern.log4j.Log4j2;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.util.stream.Stream;

/**
 * Central cache configuration responsible for configuring caches used throughout the application.
 * <p>
 * This includes caches for Hibernate and predefined application caches.
 */
@Configuration
@EnableCaching
@Log4j2
public class CacheConfiguration {

    private static final Duration CACHE_EXPIRY_DURATION = Duration.ETERNAL;
    private static final long HIBERNATE_CACHE_SIZE_MB = 1;

    private static final String[] PREDEFINED_CACHE_NAMES = Stream.concat(
            Stream.of(
                    AbstractConfigurationItem.class,
                    Parameter.class,
                    Node.class,
                    MonitoringFilter.class,
                    Module.class,
                    ChainConfiguration.class,
                    ModuleConfiguration.class,
                    com.docshifter.core.monitoring.entities.Configuration.class,
                    ParameterDependency.class
            ).map(Class::getCanonicalName),
            Stream.of(
                    "com.docshifter.core.config.entities.Node.childNodes",
                    "com.docshifter.core.monitoring.entities.AbstractConfigurationItem.notificationLevels",
                    "com.docshifter.core.config.entities.Module.parameters",
                    "com.docshifter.core.config.entities.ChainConfiguration.rootNodes",
                    "com.docshifter.core.config.entities.ModuleConfiguration.parameterValues",
                    "com.docshifter.core.config.entities.Node.parentNodes"
            )
    ).toArray(String[]::new);

    @Bean
    public CacheManager ehCacheManager() {
        var cacheManager = Caching.getCachingProvider().getCacheManager();

        configureHibernateCaches(cacheManager);
        configurePredefinedCaches(cacheManager);

        return cacheManager;
    }

    /**
     * Configures caches used by Hibernate.
     *
     * @param cacheManager The cache manager to use for creating caches.
     */
    private void configureHibernateCaches(CacheManager cacheManager) {
        configureCache(cacheManager, "default-update-timestamps-region", HIBERNATE_CACHE_SIZE_MB);
        configureCache(cacheManager, "default-query-results-region", HIBERNATE_CACHE_SIZE_MB);
    }

    /**
     * Configures predefined caches used by the application.
     *
     * @param cacheManager The cache manager to use for creating caches.
     */
    private void configurePredefinedCaches(CacheManager cacheManager) {
        var config = new MutableConfiguration<>()
                .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(CACHE_EXPIRY_DURATION))
                .setStoreByValue(false)
                .setStatisticsEnabled(false);

        for (String cacheName : PREDEFINED_CACHE_NAMES) {
            cacheManager.createCache(cacheName, config);
        }
    }

    /**
     * Configures a single cache with a specified size.
     *
     * @param cacheManager The cache manager to use for creating the cache.
     * @param cacheName    The name of the cache to configure.
     * @param sizeMb       The size of the cache in megabytes.
     */
    private void configureCache(CacheManager cacheManager, String cacheName, long sizeMb) {
        var builder = CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Object.class, Object.class,
                        ResourcePoolsBuilder.newResourcePoolsBuilder().offheap(sizeMb, MemoryUnit.MB));

        var cache = Eh107Configuration.fromEhcacheCacheConfiguration(builder);
        cacheManager.createCache(cacheName, cache);
    }
}