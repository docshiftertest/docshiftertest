package com.docshifter.core.config.db;

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

    @Value( "${spring.datasource.url}" )
    private String url;
    @Value("${spring.jpa.database-platform}")
    private String dialect;

    @Bean
    public DataSource metricsDataSource(){
       return DataSourceBuilder.create()
        .url(url)
        .username("METRICS_SYSTEM")
        .password("docshifterpassword")
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
        properties.setProperty("hibernate.dialect", dialect);
        properties.setProperty("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.physical_naming_strategy", SpringPhysicalNamingStrategy.class.getName());
        properties.put("hibernate.implicit_naming_strategy", SpringImplicitNamingStrategy.class.getName());

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

