package com.docshifter.core.audit.services;

import com.docshifter.core.audit.entities.ModuleConfigurationVersion;
import com.docshifter.core.audit.repositories.ModuleConfigurationVersionRepository;
import com.docshifter.core.audit.services.inter.IModuleConfigurationVersionSharedService;
import com.docshifter.core.config.entities.Module;
import com.docshifter.core.config.entities.ModuleConfiguration;
import com.docshifter.core.config.entities.Node;
import com.docshifter.core.config.entities.Parameter;
import com.docshifter.core.exceptions.NotFoundException;
import com.docshifter.core.utils.Utils;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.isNull;

@Log4j2
@Service
public class ModuleConfigurationVersionSharedService implements IModuleConfigurationVersionSharedService {

    private final ModuleConfigurationVersionRepository moduleConfigurationVersionRepository;

    public ModuleConfigurationVersionSharedService(ModuleConfigurationVersionRepository moduleConfigurationVersionRepository) {
        this.moduleConfigurationVersionRepository = moduleConfigurationVersionRepository;
    }

    public ModuleConfigurationVersionRepository getModuleConfigurationVersionRepository() {
        return moduleConfigurationVersionRepository;
    }

    /**
     * Sets the correct {@link ModuleConfiguration} to the {@link Node}
     * @param node {@link Node} to check
     */
    @Override
    public void setCorrectMcVersionInNode(Node node) {

        // If the version is not locked or the version for the node is null, we use the latest
        if (!node.getLockedVersion() || isNull(node.getVersion())) {
            return;
        }

        ModuleConfiguration mcLatestVersion = node.getModuleConfiguration();

        boolean isTheLatestVersion = checkIfItIsTheLatestVersion(
                mcLatestVersion.getUuid(),
                node.getVersion()
        );

        if (isTheLatestVersion) {
            log.debug("The module configuration [{}] is in the latest version.",
                    mcLatestVersion.getName());
            return;
        }

        log.debug("The module configuration [{}] is in the version [{}].",
                mcLatestVersion.getName(), node.getVersion());

        ModuleConfiguration mc = getMcByVersion(
                mcLatestVersion,
                node.getVersion()
        );

        node.setModuleConfiguration(mc);
    }

    /**
     * Gets the correct {@link ModuleConfiguration} for the {@link Node}
     * @param node {@link Node} to get the {@link ModuleConfiguration} from
     * @return the corresponding {@link ModuleConfiguration} for the {@link Node}
     */
    @Override
    public ModuleConfiguration getCorrectMcVersionForNode(Node node) {

        ModuleConfiguration mcLatestVersion = node.getModuleConfiguration();

        // If the version is not locked or the version for the node is null, we use the latest
        if (!node.getLockedVersion() || isNull(node.getVersion())) {
            return mcLatestVersion;
        }

        boolean isTheLatestVersion = checkIfItIsTheLatestVersion(
                mcLatestVersion.getUuid(),
                node.getVersion()
        );

        if (isTheLatestVersion) {
            log.debug("The module configuration [{}] is in the latest version.",
                    mcLatestVersion.getName());
            return mcLatestVersion;
        }

        log.debug("The module configuration [{}] is in the version [{}].",
                mcLatestVersion.getName(), node.getVersion());

        return getMcByVersion(
                mcLatestVersion,
                node.getVersion()
        );
    }

    /**
     * Checks if the {@link ModuleConfiguration} is in the last version
     * @param uuid unique UUID for the {@link ModuleConfigurationVersion}
     * @param version version to be validated
     * @return either if it is in the latest version or not
     */
    @Override
    public boolean checkIfItIsTheLatestVersion(UUID uuid, Integer version) {
        try {
            return getLastVersionByModuleConfigurationUuid(uuid).getVersion().equals(version);
        }
        catch (NotFoundException notFoundException) {
            return false;
        }
    }

    /**
     * Gets the last version for a {@link ModuleConfigurationVersion}
     * @param uuid unique UUID for the entity {@link ModuleConfigurationVersion}
     * @return the {@link ModuleConfigurationVersion} corresponding to the last version of the {@link ModuleConfigurationVersion}
     */
    @Override
    public ModuleConfigurationVersion getLastVersionByModuleConfigurationUuid(UUID uuid) throws NotFoundException {

        Optional<ModuleConfigurationVersion> mcVersionOptional = moduleConfigurationVersionRepository.findTopByModuleConfigurationUuidOrderByVersionDesc(uuid);

        if (mcVersionOptional.isEmpty()) {
            log.error("Could not find any ModuleConfigurationVersion with uuid [{}].", uuid);
            throw new NotFoundException("It is not possible to find any version for the module configuration.");
        }

        log.debug("The last version for the module configuration with uuid: [{}] is : {}",
                uuid, mcVersionOptional.get().getVersion());

        return mcVersionOptional.get();
    }

    /**
     * Gets the {@link ModuleConfigurationVersion} by its UUID and specific version
     * @param uuid unique UUID for the {@link ModuleConfigurationVersion}
     * @param version version of the {@link ModuleConfiguration}
     * @return the {@link ModuleConfigurationVersion} corresponding to the version
     * @throws NotFoundException exception if it is not possible to find the {@link ModuleConfigurationVersion}
     */
    @Override
    public ModuleConfigurationVersion getVersionByModuleConfigurationUuid(UUID uuid, Integer version) throws NotFoundException {

        Optional<ModuleConfigurationVersion> mcVersionOptional = moduleConfigurationVersionRepository.findByModuleConfigurationUuidAndVersion(uuid, version);

        if (mcVersionOptional.isEmpty()) {
            log.error("Could not find the version [{}] for the module configuration with uuid [{}].", version, uuid);
            throw new NotFoundException("Could not find the version ["+version+"] for the module configuration with uuid ["+uuid+"].");
        }

        return mcVersionOptional.get();
    }

    /**
     * Gets the {@link ModuleConfiguration} for a specific version
     * @param mcLatestVersion {@link ModuleConfiguration} in the latest version
     * @param version version of the {@link ModuleConfiguration}
     * @return the {@link ModuleConfiguration} corresponding to the version
     */
    @Override
    public ModuleConfiguration getMcByVersion(ModuleConfiguration mcLatestVersion, Integer version) throws NotFoundException {
        return deserializeVersionJson(
                mcLatestVersion,
                getVersionByModuleConfigurationUuid(mcLatestVersion.getUuid(), version)
        );
    }

    /**
     * Deserializes a {@link ModuleConfigurationVersion}
     * @param mcVersion {@link ModuleConfigurationVersion} to deserialize
     * @return the {@link ModuleConfiguration}
     */
    @Override
    public ModuleConfiguration deserializeVersionJson(ModuleConfiguration mcLatestVersion, ModuleConfigurationVersion mcVersion) {
        return createModuleConfiguration(
                mcLatestVersion,
                Utils.mapJSON(mcVersion.getModuleConfigurationJson())
        );
    }

    /**
     * Creates a {@link ModuleConfiguration} with the parameters from a previous version
     * @param mcLatestVersion {@link ModuleConfiguration} in the latest version
     * @param moduleConfigurationMap map with the representation of the {@link ModuleConfiguration} in a previous version
     * @return the {@link ModuleConfiguration} in a previous version
     */
    private ModuleConfiguration createModuleConfiguration(ModuleConfiguration mcLatestVersion,
                                                          Map<String, Object> moduleConfigurationMap) {

        Module module = mcLatestVersion.getModule();

        // Creating a ModuleConfiguration with the module
        ModuleConfiguration moduleConfiguration = new ModuleConfiguration(module);

        // Setting the id, name and description
        moduleConfiguration.setId(mcLatestVersion.getId());
        moduleConfiguration.setName(String.valueOf(moduleConfigurationMap.get("name")));
        moduleConfiguration.setDescription(String.valueOf(moduleConfigurationMap.get("description")));

        // Getting the parameters from the previous moduleConfiguration
        Map<String, Object> moduleMap = (Map<String, Object>) moduleConfigurationMap.get("module");
        List<Map<String, Object>> previousModuleParameters = (List<Map<String, Object>>) moduleMap.get("parameters");

        // Map the Id given to the parameter that we have in the db
        Map<Long, Parameter> mappedParameterById = new HashMap<>();

        for (Map<String, Object> paramMap : previousModuleParameters) {

            // We want to fetch the raw parameter as we do need to take aliases into account later on
            Parameter foundParameter = module.getRawParameter((String) paramMap.get("name"));

            if (foundParameter == null) {
                log.info("Parameter {} no longer exists for the current module version and will not be used.",
                        paramMap.get("name"));
                continue;
            }

            // Default the mapping as Import Id to Import Id
            Long previousParameterId = Long.valueOf((Integer) paramMap.get("id"));
            log.debug("previousParameterId Id: {} ", previousParameterId);

            mappedParameterById.put(previousParameterId, foundParameter);
            log.debug("mappedParameterById.size(): {}", mappedParameterById.size());
        }

        Map<Parameter, String> nameValueParameterMap = getParameterStringMap(
                moduleConfigurationMap,
                mappedParameterById
        );

        moduleConfiguration.setParameterValues(nameValueParameterMap);
        log.debug("Returning moduleConfiguration...");

        return moduleConfiguration;
    }

    @SuppressWarnings("Raw")
    @NotNull
    private Map<Parameter, String> getParameterStringMap(Map<String, Object> moduleConfigurationMap,
                                                         Map<Long, Parameter> mappedParameterById) {

        Map<Parameter, String> nameValueParameterMap = new HashMap<>();

        // These are the values of the parameters being imported with just id and value
        List<Map<String, Object>> previousMcParameterValues = (List<Map<String, Object>>) moduleConfigurationMap.get("parameters");

        for (Map<String, Object> previousMcParameterValue : previousMcParameterValues) {

            log.debug("id: [{}], value: [{}]",
                    previousMcParameterValue.get("id"), previousMcParameterValue.get("value"));

            Parameter parameter = mappedParameterById.get(Long.valueOf((String) previousMcParameterValue.get("id")));

            //Checking if got a param value without configuration
            if (parameter == null) {
                log.debug("Found a param value without configuration");
                continue;
            }

            final Parameter realParam = parameter.getRealParameter();

            String value = (String) previousMcParameterValue.get("value");

            // It's time to fill in the nameValueParameterMap, redirecting any values set for alias parameters to
            // their real counterparts.
            // On conflicts, we do not want to overwrite an already existing value present in the map.
            // Notable exception however: if there is both a value for an alias pointing to a parameter, and for
            // that parameter itself, we want to accept and prioritize the value from the latter, given that its
            // value is not null.
            final boolean isThereAlreadyAValue = nameValueParameterMap.get(realParam) != null;

            if ((parameter.getAliasOf() == null && isThereAlreadyAValue && value == null)
                    || (parameter.getAliasOf() != null && isThereAlreadyAValue)) {

                String name = parameter.getRawName();

                if (parameter.getAliasOf() != null) {
                    name += "(alias of " + realParam.getRawName() + ")";
                }

                log.info("Parameter [{}] already has a value, so we will not overwrite it", name);

                continue;
            }

            if (parameter.getAliasMappings() != null) {
                // Map to the correct value if we've got some mapping rules configured for the current param,
                // lowercase, so we can perform a case-insensitive lookup
                final String keyToSearch = value == null ? "" : value.toLowerCase();
                value = Utils.mapJSON(parameter.getAliasMappings()).entrySet().stream()
                        .filter(kv -> kv.getKey().toLowerCase().equals(keyToSearch))
                        .findAny()
                        .map(Map.Entry::getValue)
                        .map(Object::toString)
                        .orElse(value);
            }

            // Again, make sure we input the value in the actual parameter if the current one is an alias
            nameValueParameterMap.put(realParam, value);
        }

        return nameValueParameterMap;
    }

}
