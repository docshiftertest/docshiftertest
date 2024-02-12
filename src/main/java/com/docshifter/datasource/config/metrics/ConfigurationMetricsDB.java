package com.docshifter.datasource.config.metrics;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.cfg.Environment;
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
import java.util.Properties;

@Configuration
@EnableJpaRepositories(
        entityManagerFactoryRef = "metricsEntityManagerFactory",
        transactionManagerRef = "metricsTransactionManager",
        basePackages = "com.docshifter.core.metrics.repositories"
)
public class ConfigurationMetricsDB {
    private final String url;
    private final String password;
    private final String dialect;
    private final String schemaCreation;

    public ConfigurationMetricsDB(@Value("${spring.datasource.metrics.url}") String url,
                                  @Value("${spring.datasource.metrics.password:}") String password,
                                  @Value("${spring.jpa.database-platform}") String dialect,
                                  @Value("${spring.jpa.hibernate.ddl-auto}") String schemaCreation) {
        this.url = url;
        // Backwards compatibility with older installations: the metrics password used to be hardcoded
        this.password = StringUtils.isEmpty(password) ? "mb282wu7nvDkbQRkfXvA" : password;
        this.dialect = dialect;
        this.schemaCreation = schemaCreation;
    }

    @Bean
    public DataSource metricsDataSource(){
       return DataSourceBuilder.create()
        .url(url)
        .username("metrics_system")
        .password(password)
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
        properties.put(Environment.PHYSICAL_NAMING_STRATEGY, CamelCaseToUnderscoresNamingStrategy.class.getName());
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

