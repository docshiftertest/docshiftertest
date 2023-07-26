package com.docshifter.core.config.repositories;

import com.docshifter.core.config.entities.Module;
import com.docshifter.core.config.entities.Parameter;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by michiel.vandriessche@docbyte.com on 8/19/16.
 */
public interface ParameterRepository extends CrudRepository<Parameter, Long> {
    /**
     * Finds all {@link Parameter}s with a given name, across all {@link Module}s.
     * @param name The name of the {@link Parameter}.
     * @return The {@link Parameter}.
     */
    List<Parameter> findByName(String name);

    /**
     * Finds all {@link Parameter}s for a specific {@link Module}.
     * @param module The {@link Module}.
     * @return All {@link Parameter}s for the provided {@link Module}, including aliases.
     */
    List<Parameter> findByModule(Module module);

    /**
     * Finds all actual {@link Parameter}s for a specific {@link Module}.
     * @param module The {@link Module}.
     * @return All {@link Parameter}s for the provided {@link Module}, excluding aliases.
     */
    List<Parameter> findByModuleAndAliasOfIsNull(Module module);

    /**
     * Finds a {@link Parameter} with a given name for a specific {@link Module}.
     * param name The name of the {@link Parameter}.
     * @param module The {@link Module}.
     * @return The {@link Parameter} if it was found, {@code null} otherwise.
     */
    Parameter findOneByNameAndModule(String name, Module module);
}
