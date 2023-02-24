package com.docshifter.datasource.config.metrics;

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
        entityManagerFactoryRef = "metricsEntityManagerFactory",
        transactionManagerRef = "metricsTransactionManager",
        basePackages = "com.docshifter.core.metrics.repositories"
)
public class ConfigurationMetricsDB {

    @Value( "${spring.datasource.metrics.url}" )
    private String url;
    @Value("${spring.jpa.database-platform}")
    private String dialect;
    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String schemaCreation;

    @Bean
    public DataSource metricsDataSource(){
       return DataSourceBuilder.create()
        .url(url)
        .username("metrics_system")
        .password("mb282wu7nvDkbQRkfXvA") // make it at least slightly less obvious that this is the password
               //make a new string from a byte array
               //.password(somestring);
        .build();
    }

    @Bean
    public PlatformTransactionManager metricsTransactionManager() {
        return new JpaTransactionManager(metricsEntityManagerFactory().getObject());
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean metricsEntityManagerFactory() {

        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setGenerateDdl(false);

        Properties properties = new Properties();
        properties.setProperty(Environment.DIALECT, dialect);
        properties.setProperty(Environment.DEFAULT_SCHEMA, "metrics");
        properties.put(Environment.PHYSICAL_NAMING_STRATEGY, SpringPhysicalNamingStrategy.class.getName());
        properties.put(Environment.IMPLICIT_NAMING_STRATEGY, SpringImplicitNamingStrategy.class.getName());
        properties.setProperty(Environment.HBM2DDL_AUTO, schemaCreation);

        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();

        factoryBean.setDataSource(metricsDataSource());
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter);
        factoryBean.setPackagesToScan(
                "com.docshifter.core.metrics.entities");
        factoryBean.setPersistenceUnitName("metrics");

        factoryBean.setJpaProperties(properties);

        return factoryBean;
    }
}

