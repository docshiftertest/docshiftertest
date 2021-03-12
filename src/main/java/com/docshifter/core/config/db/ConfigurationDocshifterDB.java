package com.docshifter.core.config.db;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
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
        properties.setProperty("hibernate.dialect", dialect);
        properties.setProperty("hibernate.default_schema", defaultSchema);
        properties.put("hibernate.physical_naming_strategy", SpringPhysicalNamingStrategy.class.getName());
        properties.put("hibernate.implicit_naming_strategy", SpringImplicitNamingStrategy.class.getName());
//        properties.setProperty("hibernate.hbm2ddl.auto", "update");

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
