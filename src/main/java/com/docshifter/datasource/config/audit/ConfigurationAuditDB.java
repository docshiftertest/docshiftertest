package com.docshifter.datasource.config.audit;

import com.docshifter.datasource.config.DBUtils;
import org.apache.commons.lang3.StringUtils;
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
    private final String url;
    private final String password;

    public ConfigurationAuditDB(@Value("${spring.datasource.audit.url}") String url,
                                @Value("${spring.datasource.audit.password:}") String password) {
        this.url = url;
        // Backwards compatibility with older installations: the audit password used to be hardcoded
        this.password = StringUtils.isEmpty(password) ? "aae4d1b46100a43119b6c43eacff8f74" : password;
    }

    @Bean
    public DataSource auditDataSource() {
        return DataSourceBuilder.create()
                .url(url)
                .username("ds_audit")
                .password(password)
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

        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();

        factoryBean.setDataSource(auditDataSource());
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter);
        factoryBean.setPackagesToScan(
                "com.docshifter.core.audit.entities");
        factoryBean.setPersistenceUnitName("audit");

        factoryBean.setJpaProperties(DBUtils.defaultProperties("audit",false));

        return factoryBean;
    }
}
