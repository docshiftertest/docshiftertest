package com.docshifter.core.config;


import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.MappingStyle;
import org.javers.core.diff.ListCompareAlgorithm;
import org.javers.repository.sql.ConnectionProvider;
import org.javers.repository.sql.DialectName;
import org.javers.repository.sql.JaversSqlRepository;
import org.javers.repository.sql.SqlRepositoryBuilder;
import org.javers.spring.auditable.AuthorProvider;
import org.jgroups.annotations.Component;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceUtils;

@Configuration
@ComponentScan(basePackages = {"com.docshifter.core", "com.docshifter.core.monitoring"})
public class JaversConfiguration {

    /**
     * Bean to configure the author of the changes
     *
     * @return the author of the changes
     */
    @Bean
    public AuthorProvider authorProvider() {
        return new SpringSecurityAuthorProvider();
    }

    /**
     * Bean to configure the javers Object
     *
     * @return the javers bean
     */
    @Component
    public Javers javers() {
        ConnectionProvider connectionProvider = () -> DataSourceUtils.getConnection(DataSourceBuilder.create().url("jdbc:postgresql://localhost:5432/audit").password("DS_AUDIT").username("ds_audit").build());

        JaversSqlRepository sqlRepository = SqlRepositoryBuilder
                .sqlRepository()
                .withSchema("audit") //optionally, provide the schame name (the name has to be decided)
                .withConnectionProvider(connectionProvider) // Connection to the new datasource
                .withDialect(DialectName.POSTGRES).build();
        return JaversBuilder.javers()
                .withTerminalChanges(false)
                .withInitialChanges(false)
                .withPackagesToScan("com.docshifter.core.config.entities")
                .withPackagesToScan("com.docshifter.core.monitoring.entities")
                .withPrettyPrint(true) // Formated json?
                .withMappingStyle(MappingStyle.FIELD)
//        There are two mapping styles in JaVers FIELD and BEAN
                // When using FIELD style, JaVers accesses object state directly from fields.
                // In this case, @Id annotation should be placed at the field level.

                // When using BEAN style, JaVers accesses object state by calling getters.
                // @Id annotation should be placed at the method level.

                .withListCompareAlgorithm(ListCompareAlgorithm.SIMPLE) // algorithm to compare the changes
                // There are SIMPLE, LEVENSHTEIN_DISTANCE, as AS_SET algorithms.
                // The AS_SET transforms the list in set and costs more,

                .registerJaversRepository(sqlRepository).build(); // repository to save the changes
    }
}
