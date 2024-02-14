package com.docshifter.datasource.config.metrics;

import com.docshifter.datasource.config.DBUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.cfg.AvailableSettings;
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

    public ConfigurationMetricsDB(@Value("${spring.datasource.metrics.url}") String url,
                                  @Value("${spring.datasource.metrics.password:}") String password) {
        this.url = url;
        // Backwards compatibility with older installations: the metrics password used to be hardcoded
        this.password = StringUtils.isEmpty(password) ? "mb282wu7nvDkbQRkfXvA" : password;
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
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();

        factoryBean.setDataSource(metricsDataSource());
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter);
        factoryBean.setPackagesToScan(
                "com.docshifter.core.metrics.entities");
        factoryBean.setPersistenceUnitName("metrics");

        factoryBean.setJpaProperties(DBUtils.defaultProperties("metrics",false));

        return factoryBean;
    }
}

