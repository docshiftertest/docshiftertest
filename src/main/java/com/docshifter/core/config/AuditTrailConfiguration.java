package com.docshifter.core.config;


import com.docshifter.core.config.entities.ChainConfiguration;
import com.docshifter.core.config.entities.ModuleConfiguration;
import com.docshifter.core.config.entities.Node;
import com.docshifter.core.config.repositories.ChainConfigurationRepository;
import com.docshifter.core.config.repositories.ModuleConfigurationRepository;
import com.docshifter.core.config.repositories.ModuleRepository;
import com.docshifter.core.config.repositories.NodeRepository;
import com.docshifter.core.config.repositories.ParameterRepository;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.MappingStyle;
import org.javers.core.diff.ListCompareAlgorithm;
import org.javers.repository.sql.ConnectionProvider;
import org.javers.repository.sql.DialectName;
import org.javers.repository.sql.JaversSqlRepository;
import org.javers.repository.sql.SqlRepositoryBuilder;
import org.javers.spring.auditable.AuthorProvider;
import org.javers.spring.auditable.CommitPropertiesProvider;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Configuration
@ComponentScan(basePackages = {"com.docshifter.core", "com.docshifter.core.monitoring"})
public class AuditTrailConfiguration {

    public NodeRepository nodeRepository;
    public ChainConfigurationRepository chainConfigurationRepository;
    public ModuleRepository moduleRepository;
    public ModuleConfigurationRepository moduleConfigurationRepository;
    public ParameterRepository parameterRepository;

    public AuditTrailConfiguration(NodeRepository nodeRepository, ChainConfigurationRepository chainConfigurationRepository, ModuleRepository moduleRepository, ModuleConfigurationRepository moduleConfigurationRepository, ParameterRepository parameterRepository) {
        this.nodeRepository = nodeRepository;
        this.chainConfigurationRepository = chainConfigurationRepository;
        this.moduleRepository = moduleRepository;
        this.moduleConfigurationRepository = moduleConfigurationRepository;
        this.parameterRepository = parameterRepository;
    }

    /**
     * Bean to configure the author of the changes
     *
     * @return the author of the changes
     */
    @Bean
    public AuthorProvider authorProvider() {
        return new SpringSecurityAuthorProvider();
    }


    @Bean
    public CommitPropertiesProvider commitPropertiesProvider() {
        return new CommitPropertiesProvider() {

            @Override
            public Map<String, String> provideForDeleteById(Class<?> domainObjectClass, Object domainObjectId) {

                return switch (domainObjectClass.getSimpleName()) {
                    case "Node" -> findWorkFlowValues(nodeRepository.findById((Long) domainObjectId));
                    case "ModuleConfiguration" -> findWorkFlowValues(moduleConfigurationRepository.findById((Long) domainObjectId));
                    case "ChainConfiguration" -> findWorkFlowValues(chainConfigurationRepository.findById((Long) domainObjectId));
                    default -> CommitPropertiesProvider.super.provideForDeleteById(domainObjectClass, domainObjectId);
                };

            }

            @Override
            public Map<String, String> provideForDeletedObject(Object domainObject) {

                return findWorkFlowValues(domainObject);
            }

            @Override
            public Map<String, String> provideForCommittedObject(Object domainObject) {
                return findWorkFlowValues(domainObject);

            }

            private Map<String, String> findWorkFlowValues(Object domainObject) {
                if (domainObject instanceof ChainConfiguration chain) {

                    return populateMap(chain, chain.getRootNode().getModuleConfiguration(),  chain.getRootNode(), chain.getRootNode());

                } else if (domainObject instanceof ModuleConfiguration moduleConfiguration) {

                    Node rootNode = null;
                    Node currentNode = null;
                    ChainConfiguration chain = null;
                    Optional<Node> optNode = nodeRepository.findNodeByModuleConfiguration(moduleConfiguration).stream().findFirst();

                    if (optNode.isPresent()) {
                        currentNode = optNode.get();
                        rootNode = currentNode;

                        while (rootNode.getParentNode() != null) {
                            rootNode = rootNode.getParentNode();
                        }

                        chain = chainConfigurationRepository.findByRootNode(rootNode);

                        if(chain == null){
                            chain = rootNode.getChainConfigurationList().get(0);
                        }
                    }



                    return populateMap(chain, moduleConfiguration, rootNode, currentNode);

                } else if (domainObject instanceof Node node) {

                    Node rootNode;
                    Node currentNode;

                    currentNode = node;
                    rootNode = currentNode;

                    while (rootNode.getParentNode() != null) {
                        rootNode = rootNode.getParentNode();
                    }
                    ModuleConfiguration rootNodeModuleConfiguration = rootNode.getModuleConfiguration();

                    ChainConfiguration chain = chainConfigurationRepository.findByRootNode(node);

                    if (chain == null) {
                        return Collections.emptyMap();
                    }

                    return populateMap(chain, rootNodeModuleConfiguration, rootNode, currentNode);
                }
                return Collections.emptyMap();
            }


            private Map<String, String> populateMap(ChainConfiguration chain, ModuleConfiguration rootModuleConfiguration,
                                                     Node rootNode, Node currentNode){

                Map<String, String> mapModifications = new HashMap<>();
                mapModifications.put("workflowName", chain == null ? "Workflow not saved" : chain.getName());
                if(chain != null) {
                    mapModifications.put("workflowId", chain.getId() != 0 ? Long.toString(chain.getId()) : "");
                }

                //root node
                mapModifications.put("moduleConfigurationName", rootModuleConfiguration.getName());
                mapModifications.put("moduleConfigurationId", Long.toString(rootModuleConfiguration.getId()));
                mapModifications.put("moduleName", rootModuleConfiguration.getModule().getName());
                mapModifications.put("moduleId", Long.toString(rootModuleConfiguration.getModule().getId()));

                if(currentNode != null) {
                    mapModifications.put("nodeId", Long.toString(currentNode.getId()));
                }
                if (rootNode != null) {
                    mapModifications.put("rootNodeId", Long.toString(rootNode.getId()));
                }

                return mapModifications;
            }
        };
    }

    /**
     * Bean to configure the javers Object
     *
     * @return the javers bean
     */
    @Bean
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
