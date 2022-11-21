package com.docshifter.datasource.config.audit;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Objects;
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

    @Bean
    public DataSource auditDataSource() {
        return DataSourceBuilder.create()
                .url(url)
                .username("ds_audit")
                .password("aae4d1b46100a43119b6c43eacff8f74")
                .build();
    }

    @Bean
    public PlatformTransactionManager auditTransactionManager() {
        return new JpaTransactionManager(Objects.requireNonNull(auditEntityManagerFactory().getObject()));
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean auditEntityManagerFactory() {

        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setGenerateDdl(false);

        Properties properties = new Properties();
        properties.setProperty(AvailableSettings.DIALECT, dialect);
        properties.setProperty(AvailableSettings.DEFAULT_SCHEMA, "audit");
        properties.put(AvailableSettings.PHYSICAL_NAMING_STRATEGY, CamelCaseToUnderscoresNamingStrategy.class.getName());
        properties.put(AvailableSettings.IMPLICIT_NAMING_STRATEGY, SpringImplicitNamingStrategy.class.getName());
        properties.setProperty(AvailableSettings.HBM2DDL_AUTO, "none");

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
