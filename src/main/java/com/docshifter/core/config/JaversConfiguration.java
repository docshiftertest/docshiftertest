package com.docshifter.core.config;


import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.MappingStyle;
import org.javers.core.diff.ListCompareAlgorithm;
import org.jgroups.annotations.Component;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"com.docshifter.core", "com.docshifter.core.monitoring"})
public class JaversConfiguration {

    /**
     * Bean to configure the javers Object
     *
     * @return the javers bean
     */
    @Component
    public Javers javers() {
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

                .withListCompareAlgorithm(ListCompareAlgorithm.SIMPLE).build();  // algorithm to compare the changes
                // There are SIMPLE, LEVENSHTEIN_DISTANCE, as AS_SET algorithms.
                // The AS_SET transforms the list in set and costs more,
    }
}
