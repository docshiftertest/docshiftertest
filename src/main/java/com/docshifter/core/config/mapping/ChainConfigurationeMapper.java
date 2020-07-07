package com.docshifter.core.config.mapping;

import com.docshifter.core.config.domain.ChainConfiguration;
import com.docshifter.core.config.dto.ChainConfigurationDTO;

public class ChainConfigurationeMapper {

	public static ChainConfigurationDTO convertToDto(ChainConfiguration entity) {

		if (entity == null) {
			return null;
		}
		ChainConfigurationDTO dto = new ChainConfigurationDTO();
		convertToDto(entity, dto);
		return dto;
	}

	private static void convertToDto(ChainConfiguration entity, ChainConfigurationDTO dto) {

		dto.setDescription(entity.getDescription());
		dto.setEnabled(entity.isEnabled());
		dto.setId(entity.getId());
		dto.setName(entity.getName());
		dto.setPrinterName(entity.getPrinterName());
		dto.setPriority(entity.getPriority());
		dto.setQueueName(entity.getQueueName());
		dto.setRootNode(NodeMapper.convertToDto(entity.getRootNode()));
		dto.setTimeout(entity.getTimeout());
	}

	public static ChainConfiguration convertToEntity(ChainConfigurationDTO dto) {

		if (dto == null) {
			return null;
		}

		ChainConfiguration entity = new ChainConfiguration();
		convertToEntity(entity, dto);

		return entity;
	}

	private static void convertToEntity(ChainConfiguration entity, ChainConfigurationDTO dto) {

		entity.setDescription(dto.getDescription());
		entity.setEnabled(dto.isEnabled());
		entity.setId(dto.getId());
		entity.setName(dto.getName());
		entity.setPrinterName(dto.getPrinterName());
		entity.setPriority(dto.getPriority());
		entity.setQueueName(dto.getQueueName());
		entity.setRootNode(NodeMapper.convertToEntity(dto.getRootNode()));
		entity.setTimeout(dto.getTimeout());
	}

}
