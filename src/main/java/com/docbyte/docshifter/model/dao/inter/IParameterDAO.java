package com.docbyte.docshifter.model.dao.inter;

import java.util.List;
import java.util.Set;

import com.docbyte.docshifter.model.vo.Parameter;

public interface IParameterDAO
{
	public Parameter get(Long id);
	public Parameter get(String name);
	
	public List<Parameter> find(String name);

	public Long save(Parameter param);
	public void saveAll(Set<Parameter> params);
	
	public void delete(Parameter param);
	public void deleteAll(Set<Parameter> params);
}
