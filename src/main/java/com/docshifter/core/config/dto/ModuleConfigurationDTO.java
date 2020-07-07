package com.docshifter.core.config.dto;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModuleConfigurationDTO {

	private long id;
	private ModuleDTO module;
	private String name;
	private String description;

	@Builder.Default
	private Map<ParameterDTO, String> parameterValues = new HashMap<ParameterDTO, String>();
}
