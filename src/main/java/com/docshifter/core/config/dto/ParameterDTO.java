package com.docshifter.core.config.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParameterDTO {

	private long id;
	private String name;
	private String description;
	private String type;
	private Boolean required;
	private String valuesJson;
}
