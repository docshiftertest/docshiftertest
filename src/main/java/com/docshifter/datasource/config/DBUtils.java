package com.docshifter.datasource.config;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;

import java.util.Properties;

public class DBUtils {

    private DBUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static Properties defaultProperties(String defaultSchema, boolean enableCaching) {
        Properties properties = new Properties();

        properties.setProperty(AvailableSettings.DEFAULT_SCHEMA, defaultSchema);
        properties.put(AvailableSettings.PHYSICAL_NAMING_STRATEGY, CamelCaseToUnderscoresNamingStrategy.class.getName());
        properties.put(AvailableSettings.IMPLICIT_NAMING_STRATEGY, SpringImplicitNamingStrategy.class.getName());
        properties.put(AvailableSettings.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
        properties.setProperty(AvailableSettings.HBM2DDL_AUTO, "none");
        if (enableCaching) {
            properties.put(AvailableSettings.USE_SECOND_LEVEL_CACHE, true);
            properties.put(AvailableSettings.JAKARTA_SHARED_CACHE_MODE, "ENABLE_SELECTIVE");
            properties.put(AvailableSettings.CACHE_REGION_FACTORY, "org.hibernate.cache.jcache.JCacheRegionFactory");
        }

        return properties;
    }
}