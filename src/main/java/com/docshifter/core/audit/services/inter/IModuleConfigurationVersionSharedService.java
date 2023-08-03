package com.docshifter.core.audit.services.inter;

import com.docshifter.core.audit.entities.ModuleConfigurationVersion;
import com.docshifter.core.config.entities.ModuleConfiguration;
import com.docshifter.core.config.entities.Node;
import com.docshifter.core.exceptions.NotFoundException;

import java.util.UUID;

public interface IModuleConfigurationVersionSharedService {

    /**
     * Sets the correct {@link ModuleConfiguration} to the {@link Node}
     * @param node {@link Node} to check
     */
    void setCorrectMcVersionInNode(Node node);

    /**
     * Gets the correct {@link ModuleConfiguration} for the {@link Node}
     * @param node {@link Node} to get the {@link ModuleConfiguration} from
     * @return the corresponding {@link ModuleConfiguration} for the {@link Node}
     */
    ModuleConfiguration getCorrectMcVersionForNode(Node node);

    /**
     * Checks if the {@link ModuleConfiguration} is in the last version
     * @param uuid unique UUID for the {@link ModuleConfigurationVersion}
     * @param version version to be validated
     * @return either if it is in the latest version or not
     */
    boolean checkIfItIsTheLatestVersion(UUID uuid, Integer version);

    /**
     * Gets the last version for a {@link ModuleConfigurationVersion}
     * @param uuid unique UUID for the entity {@link ModuleConfigurationVersion}
     * @return the {@link ModuleConfigurationVersion} corresponding to the last version of the {@link ModuleConfigurationVersion}
     */
    ModuleConfigurationVersion getLastVersionByModuleConfigurationUuid(UUID uuid) throws NotFoundException;

    /**
     * Gets the {@link ModuleConfigurationVersion} by its UUID and specific version
     * @param uuid unique UUID for the {@link ModuleConfigurationVersion}
     * @param version version of the {@link ModuleConfiguration}
     * @return the {@link ModuleConfigurationVersion} corresponding to the version
     * @throws NotFoundException exception if it is not possible to find the {@link ModuleConfigurationVersion}
     */
    ModuleConfigurationVersion getVersionByModuleConfigurationUuid(UUID uuid, Integer version) throws NotFoundException;

    /**
     * Gets the {@link ModuleConfiguration} by its UUID and specific version
     * @param uuid unique UUID for the {@link ModuleConfigurationVersion}
     * @param version version of the {@link ModuleConfiguration}
     * @return the {@link ModuleConfiguration} corresponding to the version
     */
    ModuleConfiguration getModuleConfigurationByUuidAndVersion(UUID uuid, Integer version) throws NotFoundException;

    /**
     * Deserializes a {@link ModuleConfigurationVersion}
     * @param mcVersion {@link ModuleConfigurationVersion} to deserialize
     * @return the {@link ModuleConfiguration}
     */
    ModuleConfiguration deserializeVersionJson(ModuleConfigurationVersion mcVersion);
}
