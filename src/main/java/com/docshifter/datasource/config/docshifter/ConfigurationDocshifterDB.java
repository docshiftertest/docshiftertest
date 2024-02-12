package com.docshifter.datasource.config.docshifter;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.cfg.Environment;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableJpaRepositories(
        entityManagerFactoryRef = "dsEM",
        transactionManagerRef = "dsTransactionManager",
        basePackages = {
        "com.docshifter.core.config.repositories",
                "com.docshifter.core.monitoring.repositories"})
public class ConfigurationDocshifterDB {

    @Value( "${spring.datasource.url}" )
    private String url;
    @Value( "${spring.datasource.username}" )
    private String username;
    @Value( "${spring.datasource.password}" )
    private String password;
    @Value("${spring.jpa.database-platform}")
    private String dialect;
    @Value("${spring.jpa.properties.hibernate.default_schema}")
    private String defaultSchema;
    @Value("${spring.jpa.properties.hibernate.cache.use_second_level_cache}")
    private String secondLevelCache;
    @Value("${spring.jpa.properties.hibernate.cache.region.factory_class}")
    private String cacheRegionFactory;
    @Value("${spring.jpa.properties.jakarta.persistence.sharedCache.mode}")
    private String sharedCacheMode;
    @Value("${spring.jpa.properties.hibernate.cache.ehcache.missing_cache_strategy}")
    private String missingCacheStrat;
    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String schemaCreation;

    @Bean
    @Primary
    public DataSource docshifterDataSource(){
       return DataSourceBuilder.create()
               .url(url)
               .username(username)
               .password(password).build();
    }

    @Bean
    @Primary
    public PlatformTransactionManager dsTransactionManager() {
        return new JpaTransactionManager(dsEntityManagerFactory(docshifterDataSource()).getObject());
    }

    @Bean(name = "dsEM")
    @Primary
    public LocalContainerEntityManagerFactoryBean dsEntityManagerFactory(@Qualifier("docshifterDataSource") DataSource ds) {

        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setGenerateDdl(false);
        Properties properties = new Properties();

        properties.setProperty(Environment.DIALECT, dialect);
        properties.setProperty(Environment.DEFAULT_SCHEMA, defaultSchema);
        properties.put(Environment.PHYSICAL_NAMING_STRATEGY, CamelCaseToUnderscoresNamingStrategy.class.getName());
        properties.put(Environment.IMPLICIT_NAMING_STRATEGY, SpringImplicitNamingStrategy.class.getName());
        properties.put( Environment.USE_SECOND_LEVEL_CACHE, secondLevelCache);

        properties.put(Environment.CACHE_REGION_FACTORY, cacheRegionFactory);
        properties.put(Environment.JPA_SHARED_CACHE_MODE, sharedCacheMode);
        properties.put("hibernate.cache.ehcache.missing_cache_strategy", missingCacheStrat);
        properties.setProperty(Environment.HBM2DDL_AUTO, schemaCreation);

        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();

        factoryBean.setDataSource(docshifterDataSource());
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter);
        factoryBean.setPackagesToScan(
                "com.docshifter.core.config.entities",
                "com.docshifter.core.monitoring.entities");
        factoryBean.setPersistenceUnitName("DocShifterEM");

        factoryBean.setJpaProperties(properties);

        return factoryBean;
    }
}
