package com.docshifter.core.config.entities;

import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.javers.core.metamodel.annotation.DiffIgnore;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.io.Serializable;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Cacheable
@DiffIgnore
@ToString
public class Parameter implements Comparable<Parameter>, Serializable
{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private String name;
	private String description;
	private String type;
	private Boolean required;

	@Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
	@Column(columnDefinition = "jsonb")
	private String valuesJson;
	private String parameterGroup;
	@ManyToOne
	@Nullable
	private Parameter dependsOn;
	@ManyToOne
	@Nullable
	private Parameter expendableBy;
	@ManyToOne
	@Nullable
	private Parameter aliasOf;
	@Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
	@Column(columnDefinition = "jsonb")
	@Nullable
	private String aliasMappings;

	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@ManyToOne
	@JoinTable(name = "moduleparams",
			joinColumns = {	@JoinColumn(name = "param") },
			inverseJoinColumns = { @JoinColumn(name = "module") })
	private Module module;
	
	public Parameter() {}
	
	public Parameter(String name, String description, ParameterTypes type, Boolean required, String valuesJson,
					 String parameterGroup, @Nullable Parameter dependsOn, @Nullable Parameter expendableBy) {
		this.name = name;
		this.description = description;
		this.type = type.toString();
		this.required = required;
		this.valuesJson = valuesJson;
		this.parameterGroup = parameterGroup;
		if (dependsOn == expendableBy) {
			throw new IllegalArgumentException("Conflicting dependency detected: dependsOn cannot be the same as expendableBy (" + dependsOn.name + ")!");
		}
		this.dependsOn = dependsOn;
		this.expendableBy = expendableBy;
	}

	public Parameter(String name, @Nullable Parameter aliasOf, @Nullable String aliasMappings) {
		this.name = name;
		this.aliasOf = aliasOf;
		this.aliasMappings = aliasMappings;
	}

	public Parameter(String name, String description, ParameterTypes type, Boolean required, String valuesJson,
					 String parameterGroup) {
		this(name, description, type, required, valuesJson, parameterGroup, null, null);
	}

    public Parameter(String name, String description, ParameterTypes type, Boolean required) {
	    this(name, description, type, required, null, null, null, null);
    }
	
	public Parameter(String name, String description, ParameterTypes type) {
		this(name, description, type, false, null, null, null, null);
	}

	public Parameter(String name, ParameterTypes type) {
		this(name, null, type, false, null, null, null, null);
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public long getId()
	{
		return getRealParameter().id;
	}

	public long getRawId() {
		return id;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return getRealParameter().name;
	}

	public String getRawName() {
		return name;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getDescription() {
		return getRealParameter().description;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getType() {
		return getRealParameter().type;
	}

    public String getValuesJson() {
        return getRealParameter().valuesJson;
    }

    public void setValuesJson(String valuesJson) {
        this.valuesJson = valuesJson;
    }
    
	public Boolean getRequired() {
		return getRealParameter().required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

    public String getParameterGroup() {
		return getRealParameter().parameterGroup;
	}

	public void setParameterGroup(String parameterGroup) {
		this.parameterGroup = parameterGroup;
	}

	@Nullable
	public Parameter getDependsOn() {
		return getRealParameter().dependsOn;
	}

	public void setDependsOn(@Nullable Parameter dependsOn) {
		if (dependsOn != null) {
			if (dependsOn == this) {
				throw new IllegalArgumentException("The dependsOn cannot point to itself (" + name + ")!");
			}
			if (dependsOn.getDependsOn() == this) {
				throw new IllegalArgumentException("Circular dependsOn dependency detected: the targeted parameter "
						+ dependsOn.name + " is already pointing to this one (" + name + ")!");
			}
			if (dependsOn == expendableBy) {
				throw new IllegalArgumentException("Conflicting dependency detected: dependsOn cannot be the same as expendableBy (" + dependsOn.name + ")!");
			}
		}
		this.dependsOn = dependsOn;
	}

	@Nullable
	public Parameter getExpendableBy() {
		return getRealParameter().expendableBy;
	}

	public void setExpendableBy(@Nullable Parameter expendableBy) {
		if (expendableBy != null) {
			if (expendableBy == this) {
				throw new IllegalArgumentException("The expendableBy cannot point to itself (" + name + ")!");
			}
			if (expendableBy.getExpendableBy() == this) {
				throw new IllegalArgumentException("Circular expendableBy dependency detected: the targeted parameter "
						+ expendableBy.name + " is already pointing to this one (" + name + ")!");
			}
			if (dependsOn == expendableBy) {
				throw new IllegalArgumentException("Conflicting dependency detected: dependsOn cannot be the same as expendableBy (" + dependsOn.name + ")!");
			}
		}
		this.expendableBy = expendableBy;
	}

	@Nullable
	public Parameter getAliasOf() {
		return aliasOf;
	}

	public void setAliasOf(@Nullable Parameter aliasOf) {
		if (aliasOf != null) {
			if (aliasOf == this) {
				throw new IllegalArgumentException("The aliasOf cannot point to itself (" + name + ")!");
			}
			if (aliasOf.getRealParameter() == this) {
				throw new IllegalArgumentException("Circular aliasOf dependency detected: the targeted parameter "
						+ aliasOf.name + " is already pointing to this one (" + name + ")!");
			}
			this.description = null;
			this.type = null;
			this.required = null;
			this.valuesJson = null;
			this.parameterGroup = null;
			this.dependsOn = null;
			this.expendableBy = null;
		}
		this.aliasOf = aliasOf;
	}

	@Nullable
	public String getAliasMappings() {
		return aliasMappings;
	}

	public void setAliasMappings(@Nullable String aliasMappings) {
		this.aliasMappings = aliasMappings;
	}

	public Parameter getRealParameter() {
		if (aliasOf == null) {
			return this;
		}
		return aliasOf.getRealParameter();
	}

	public Module getModule() {
		return module;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Parameter other = (Parameter) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public int compareTo(Parameter o) {
		if (this.getName() != null && o.getName() != null) {
			return this.getName().compareTo(o.getName());
		}
		else if (this.getDescription() != null && o.getDescription() != null) {
			return this.getDescription().compareTo(o.getDescription());
		}
		else {
			return Long.compare(this.getId(), o.getId());
		}
	}
}
