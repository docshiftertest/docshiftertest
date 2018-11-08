package com.docshifter.core.config.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Module {

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

	@ManyToMany(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name = "moduleparams",
			joinColumns = {	@JoinColumn(name = "module") },
			inverseJoinColumns = { @JoinColumn(name = "param") })
	private Set<Parameter> parameters = new HashSet<Parameter>();

	public Module() {
	}
	public Module(int id, String description, String name, String classname, String type, String condition, Set<Parameter> parameters) {
		this.id = id;
		this.description = description;
		this.name = name;
		this.classname = classname;
		this.type = type;
		this.condition = condition;
		this.parameters = parameters;
	}
	public Module(String description, String name, String classname, String type, String condition, Set<Parameter> parameters) {
//		this.id = id;
		this.description = description;
		this.name = name;
		this.classname = classname;
		this.type = type;
		this.condition = condition;
		this.parameters = parameters;
	}

	public Module(int id, String description, String name, String classname, String type, Set<Parameter> parameters) {
		this.id = id;
		this.description = description;
		this.name = name;
		this.classname = classname;
		this.type = type;
		this.parameters = parameters;
	}



	public Module(Module module) {
		this(module.getDescription(), module.getName(), module.getClassname(), module.getType(), module.getCondition(), new HashSet<Parameter>(module.getParameters()));
	}

	public void addToParameters(Parameter param) {
		this.getParameters().add(param);
	}

	public void addToParameters(Set<Parameter> params) {
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



	public Set<Parameter> getParameters() {
		return parameters;
	}

	@JsonIgnore
	@Transient
	public List<Parameter> getParametersAsList() {
		return new ArrayList<Parameter>(this.getParameters());
	}

	public String getType() {
		return type;
	}

	public boolean removeFromParameters(Parameter param) {
		return this.getParameters().remove(param);
	}

	public void removeFromParameters(Set<Parameter> params) {
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
		return "Module{" +
				"id=" + id +
				", name='" + name + '\'' +
				", classname='" + classname + '\'' +
				", description='" + description + '\'' +
				", type='" + type + '\'' +
				", condition='" + condition + '\'' +
				", inputFiletype='" + inputFiletype + '\'' +
				", outputFileType='" + outputFileType + '\'' +
				'}';
	}
	
	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

}
