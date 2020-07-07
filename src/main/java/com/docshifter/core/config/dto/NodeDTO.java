package com.docshifter.core.config.dto;

import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NodeDTO {

	private long id;
	private NodeDTO parentNode;
	@Builder.Default
	private Set<NodeDTO> childNodes = new HashSet<>();
	private ModuleConfigurationDTO moduleConfiguration;
}
