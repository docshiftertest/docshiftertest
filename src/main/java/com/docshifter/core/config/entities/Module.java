package com.docshifter.core.config.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.extern.log4j.Log4j2;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A module functions as a core processing unit within DocShifter workflows. Each module contains logic that handles
 * documents in its own unique manner and can be customized through {@link Parameter}s. A set of {@link Parameter} for
 * a given module is called a {@link ModuleConfiguration}.
 */
@Entity
@Log4j2
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Cacheable
public class Module implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private String name;

	@Column(unique = true)
	private String classname;
	private String description;
	private String type;
	private String condition;
	private String inputFiletype;
	private String outputFileType;
	@Column(unique = true)
	private String code;

	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@OneToMany(cascade= CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name = "moduleparams",
			joinColumns = {	@JoinColumn(name = "module") },
			inverseJoinColumns = { @JoinColumn(name = "param") })
	private Set<Parameter> parameters = new HashSet<>();

	public Module() {
	}
	public Module(int id, String description, String name, String classname, String type, String condition, Collection<Parameter> parameters) {
		this.id = id;
		this.description = description;
		this.name = name;
		this.classname = classname;
		this.type = type;
		this.condition = condition;
		this.parameters.addAll(parameters);
	}
	public Module(String description, String name, String classname, String type, String condition, Collection<Parameter> parameters) {
		this.description = description;
		this.name = name;
		this.classname = classname;
		this.type = type;
		this.condition = condition;
		this.parameters.addAll(parameters);
	}

	public Module(int id, String description, String name, String classname, String type, Collection<Parameter> parameters) {
		this.id = id;
		this.description = description;
		this.name = name;
		this.classname = classname;
		this.type = type;
		this.parameters.addAll(parameters);
	}

	public Module(long id, String name, String classname, String description, String type, String condition,
				  String inputFiletype, String outputFileType, String code, Collection<Parameter> parameters) {
		this.id = id;
		this.name = name;
		this.classname = classname;
		this.description = description;
		this.type = type;
		this.condition = condition;
		this.inputFiletype = inputFiletype;
		this.outputFileType = outputFileType;
		this.code = code;
		this.parameters.addAll(parameters);
	}

	public Module(Module module) {
		this(module.getDescription(), module.getName(), module.getClassname(), module.getType(), module.getCondition(), module.parameters);
	}

	/**
	 * Adds a {@link Parameter} to this module.
	 * @param param The {@link Parameter} to add.
	 */
	public void addToParameters(Parameter param) {
		parameters.add(param);
	}

	/**
	 * Adds one or more {@link Parameter}s to this module.
	 * @param params The {@link Parameter}s to add.
	 */
	public void addToParameters(Iterable<Parameter> params) {
		for (Parameter param : params) {
			this.addToParameters(param);
		}
	}

	public String getDescription() {
		return description;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	/**
	 * Gets a {@link Stream} of all actual {@link Parameter}s. These are {@link Parameter}s that do not function as aliases.
	 */
	private Stream<Parameter> getFilteredParameterStream() {
		return parameters.stream()
				.filter(parameter -> parameter.getAliasOf() == null);
	}

	/**
	 * Gets a {@link Set} of all actual {@link Parameter}s. These are {@link Parameter}s that do not function as aliases.
	 */
	public Set<Parameter> getParameters() {
		return getFilteredParameterStream().collect(Collectors.toUnmodifiableSet());
	}

	/**
	 * Gets a {@link Set} of ALL {@link Parameter}s. This includes ones function as aliases.
	 */
	@JsonIgnore
	@Transient
	public Set<Parameter> getRawParameters() {
		return Collections.unmodifiableSet(parameters);
	}

	/**
	 * Gets a sorted {@link List} of all actual {@link Parameter}s. These are {@link Parameter}s that do not function as aliases.
	 */
	@JsonIgnore
	@Transient
	public List<Parameter> getParametersAsList() {
		return getFilteredParameterStream().sorted().toList();
	}

	/**
	 * Gets a {@link Parameter} by name.
	 * @param name The name to look for.
	 * @param raw {@code true} if you always want the raw {@link Parameter} even if it is an alias, {@code false}
	 *                           if you always want to get the actual {@link Parameter}, so to redirect aliases.
	 * @return The {@link Parameter} that was found, or {@code null} if nothing was found.
	 */
	private Parameter getParameter(String name, boolean raw) {
		log.debug("Getting parameter for name: {}", name);
		for (Parameter param : parameters) {
			if (param == null) {
				log.warn("Param was NULL getting parameter using name: {}", name);
			}
			else {
				if (param.getName() == null) {
					log.warn("Param getName() was NULL getting parameter using name: {}. Description is: {}", name,
							param.getDescription());
				}
				if (name.equals(param.getName())) {
					if (raw) {
						return param;
					}
					return param.getRealParameter();
				}
			}
		}
		return null;
	}

	/**
	 * Gets an actual {@link Parameter} by name. So if the provided name points to an alias, it will be redirected to
	 * the {@link Parameter} it is referring to.
	 * @param name The name to look for.
	 * @return The {@link Parameter} that was found, or {@code null} if nothing was found.
	 */
	@JsonIgnore
	@Transient
	public Parameter getParameter(String name) {
		return getParameter(name, false);
	}

	/**
	 * Gets a raw {@link Parameter} by name. So you will always get the exact {@link Parameter} with that name if it
	 * exists, even if it merely functions as an alias.
	 * @param name The name to look for.
	 * @return The {@link Parameter} that was found, or {@code null} if nothing was found.
	 */
	@JsonIgnore
	@Transient
	public Parameter getRawParameter(String name) {
		return getParameter(name, true);
	}

	public String getType() {
		return type;
	}

	/**
	 * Removes a {@link Parameter} from this module.
	 * @param param The {@link Parameter} to remove.
	 * @return {@code true} it was found and therefore removed, {@code false} otherwise.
	 */
	public boolean removeFromParameters(Parameter param) {
		return parameters.remove(param);
	}

	/**
	 * Removes one or more {@link Parameter}s from this module.
	 * @param params The {@link Parameter}s to remove.
	 */
	public void removeFromParameters(Iterable<Parameter> params) {
		for (Parameter param : params) {
			this.removeFromParameters(param);
		}
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParameters(Set<Parameter> parameters) {
		this.parameters = parameters;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getClassname() {
		return classname;
	}

	public void setClassname(String classname) {
		this.classname = classname;
	}

	@Transient
	public String getInputFiletype() {
		return inputFiletype;
	}

	public void setInputFiletype(String inputFiletype) {
		this.inputFiletype = inputFiletype;
	}

	@Transient
	public String getOutputFileType() {
		return outputFileType;
	}

	public void setOutputFileType(String outputFileType) {
		this.outputFileType = outputFileType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((classname == null) ? 0 : classname.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((parameters == null) ? 0 : parameters.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Module other = (Module) obj;
		if (classname == null) {
			if (other.classname != null)
				return false;
		} else if (!classname.equals(other.classname))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (!parameters.equals(other.parameters))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "{" +
				"\"id\": " + id +
				", \"name\": \"" + name + '\"' +
				", \"classname\": \"" + classname + '\"' +
				", \"description\": \"" + description + '\"' +
				", \"type\": \"" + type + '\"' +
				", \"condition\": \"" + condition + '\"' +
				", \"inputFiletype\": \"" + inputFiletype + '\"' +
				", \"outputFileType\": \"" + outputFileType + '\"' +
				'}';
	}
	
	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
