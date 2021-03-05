package com.docshifter.core.metric.model;

//import org.hibernate.jpa.boot.spi.EntityManagerFactoryBuilder;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
//        entityManagerFactoryRef = "EntityManagerFactoryMetrics",
//        transactionManagerRef = "TransactionManagerMetrics",
        basePackages = "com.docshifter.core.metric.model"
)
@EntityScan({"com.docshifter.core.metric.model"})
public class ConfigurationMetricsDb {

    @Bean(name = "metricsDatabase")
    public DataSource getDataSource(){
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("org.hsqldb.jdbcDriver");
        dataSourceBuilder.url("jdbc:hsqldb:hsql://localhost:9001/metrics");
        dataSourceBuilder.username("admin");
        dataSourceBuilder.password("admin");
        return dataSourceBuilder.build();
    }


//    @Bean(name = "EntityManagerFactoryMetrics")
//    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder,
//                                                                       @Qualifier("metricsDatabase") DataSource dataSource) {
//
//        return builder
//                .dataSource(dataSource)
//                .packages("com.docshifter.core.metric.model")
//                .persistenceUnit("metrics")
//                .build();
//    }
//
////    @Bean(name = "TransactionManagerMetrics")
//    public PlatformTransactionManager transactionManager(@Qualifier("EntityManagerFactoryMetrics") EntityManagerFactory entityManagerFactory) {
//        return new JpaTransactionManager(entityManagerFactory);
//    }
}

