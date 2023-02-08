package com.docshifter.datasource.config.publicDB;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        entityManagerFactoryRef = "metricsEntityManagerFactory",
        transactionManagerRef = "metricsTransactionManager",
        basePackages = "com.docshifter.core.metrics.repositories")
public class ConfigurationPublicDB {

}
