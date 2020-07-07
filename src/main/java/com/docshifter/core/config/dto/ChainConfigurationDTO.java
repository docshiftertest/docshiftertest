package com.docshifter.core.config.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ChainConfigurationDTO {

	private long id;

	private String name;
	private String description;
	private String printerName;
	private String queueName;
	private long timeout;
	private Integer priority;
	private boolean enabled;
	private NodeDTO rootNode;
}
