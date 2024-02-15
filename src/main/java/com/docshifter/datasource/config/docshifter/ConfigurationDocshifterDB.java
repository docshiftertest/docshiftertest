package com.docshifter.datasource.config.docshifter;

import com.docshifter.datasource.config.DBUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        entityManagerFactoryRef = "dsEM",
        transactionManagerRef = "dsTransactionManager",
        basePackages = {
        "com.docshifter.core.config.repositories",
                "com.docshifter.core.monitoring.repositories"})
public class ConfigurationDocshifterDB {

    private final String url;
    private final String username;
    private final String password;


    public ConfigurationDocshifterDB(@Value("${spring.datasource.url}") String url, @Value("${spring.datasource.username}") String username, @Value("${spring.datasource.password}") String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

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

        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();

        factoryBean.setDataSource(ds);
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter);
        factoryBean.setPackagesToScan(
                "com.docshifter.core.config.entities",
                "com.docshifter.core.monitoring.entities");
        factoryBean.setPersistenceUnitName("DocShifterEM");

        factoryBean.setJpaProperties(DBUtils.defaultProperties("docshifter",true));

        return factoryBean;
    }
}
