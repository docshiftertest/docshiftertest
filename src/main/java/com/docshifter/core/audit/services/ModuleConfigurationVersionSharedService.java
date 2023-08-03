package com.docshifter.core.audit.services;

import com.docshifter.core.audit.entities.ModuleConfigurationVersion;
import com.docshifter.core.audit.repositories.ModuleConfigurationVersionRepository;
import com.docshifter.core.audit.services.inter.IModuleConfigurationVersionSharedService;
import com.docshifter.core.config.entities.ModuleConfiguration;
import com.docshifter.core.config.entities.Node;
import com.docshifter.core.exceptions.NotFoundException;
import com.docshifter.core.utils.Utils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

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

        ModuleConfiguration mc = getModuleConfigurationByUuidAndVersion(
                mcLatestVersion.getUuid(),
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

        return getModuleConfigurationByUuidAndVersion(
                mcLatestVersion.getUuid(),
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
        catch (NotFoundException e) {
            throw new RuntimeException(e);
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
     * Gets the {@link ModuleConfiguration} by its UUID and specific version
     * @param uuid unique UUID for the {@link ModuleConfigurationVersion}
     * @param version version of the {@link ModuleConfiguration}
     * @return the {@link ModuleConfiguration} corresponding to the version
     */
    @Override
    public ModuleConfiguration getModuleConfigurationByUuidAndVersion(UUID uuid, Integer version) throws NotFoundException {
        return deserializeVersionJson(
                getVersionByModuleConfigurationUuid(uuid, version)
        );
    }

    /**
     * Deserializes a {@link ModuleConfigurationVersion}
     * @param mcVersion {@link ModuleConfigurationVersion} to deserialize
     * @return the {@link ModuleConfiguration}
     */
    @Override
    public ModuleConfiguration deserializeVersionJson(ModuleConfigurationVersion mcVersion) {
        return Utils.clazzFromJson(mcVersion.getModuleConfigurationJson(), ModuleConfiguration.class);
    }

}
