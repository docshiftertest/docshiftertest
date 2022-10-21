package com.docshifter.datasource.config.audit;

import org.hibernate.cfg.Environment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableJpaRepositories(
        entityManagerFactoryRef = "auditEntityManagerFactory",
        transactionManagerRef = "auditTransactionManager",
        basePackages = "com.docshifter.core.audit.repositories")
public class ConfigurationAuditDB {

    @Value("${spring.datasource.audit.url}")
    private String url;
    @Value("${spring.jpa.database-platform}")
    private String dialect;
    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String schemaCreation;

    @Bean
    public DataSource auditDataSource() {
        return DataSourceBuilder.create()
                .url(url)
                .username("ds_audit")
                .password("dc9daec4c16279d7792a133c2d638c36")
                .build();
    }

    @Bean
    public PlatformTransactionManager auditTransactionManager() {
        return new JpaTransactionManager(auditEntityManagerFactory().getObject());
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean auditEntityManagerFactory() {

        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setGenerateDdl(false);

        Properties properties = new Properties();
        properties.setProperty(Environment.DIALECT, dialect);
        properties.setProperty(Environment.DEFAULT_SCHEMA, "audit");
        properties.put(Environment.PHYSICAL_NAMING_STRATEGY, SpringPhysicalNamingStrategy.class.getName());
        properties.put(Environment.IMPLICIT_NAMING_STRATEGY, SpringImplicitNamingStrategy.class.getName());
        properties.setProperty(Environment.HBM2DDL_AUTO, schemaCreation);

        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();

        factoryBean.setDataSource(auditDataSource());
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter);
        factoryBean.setPackagesToScan(
                "com.docshifter.core.audit.entities");
        factoryBean.setPersistenceUnitName("audit");

        factoryBean.setJpaProperties(properties);

        return factoryBean;
    }
}
