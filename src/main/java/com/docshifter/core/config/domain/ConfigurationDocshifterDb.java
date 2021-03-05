package com.docshifter.core.config.domain;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.sql.DataSource;

@Configuration
//@PropertySource({"classpath:application.properties"})
@EnableJpaRepositories(
//        entityManagerFactoryRef = "docshifterEntityManager",
//        transactionManagerRef = "docshifterTransactionManager",
        basePackages = {
        "com.docshifter.core.config.domain",
        "com.docshifter.core.monitoring.repo"})
@EntityScan({"com.docshifter.core.config", "com.docshifter.core.monitoring.entities"})
public class ConfigurationDocshifterDb {

    @Primary
    @Bean(name="docshifterDatabaseSource")
    public DataSource getDataSource(){
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("org.hsqldb.jdbcDriver");
        dataSourceBuilder.url("jdbc:hsqldb:hsql://localhost:9001/docshifter");
        dataSourceBuilder.username("DS_USER");
        dataSourceBuilder.password("DS_USER");
        return dataSourceBuilder.build();
    }
}
