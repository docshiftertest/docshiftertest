package com.docshifter.core.config.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
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

    @Autowired
    Environment environment;

    @Primary
    @Bean(name="docshifterDatabaseSource")
    public DataSource getDataSource(){
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("org.hsqldb.jdbcDriver");
        // TODO: Get the data from the application.properties
        dataSourceBuilder.url("jdbc:hsqldb:hsql://localhost:9001/docshifter"); // running it normally, DB gets deleted
//        dataSourceBuilder.url("jdbc:hsqldb:mem:docshifter"); //Running it in-memory, more tests fail but DB doesn't get deleted
        dataSourceBuilder.username("DS_USER");
//        dataSourceBuilder.url(environment.getProperty("spring.datasource.url"));
//        dataSourceBuilder.username(environment.getProperty("spring.datasource.username"));
//        dataSourceBuilder.password(environment.getProperty("spring.datasource.password"));
        dataSourceBuilder.password("DS_USER");
        return dataSourceBuilder.build();
    }
}
