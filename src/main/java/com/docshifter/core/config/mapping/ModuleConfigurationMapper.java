package com.docshifter.core.config.mapping;

import com.docshifter.core.config.domain.ModuleConfiguration;
import com.docshifter.core.config.dto.ModuleConfigurationDTO;

public class ModuleConfigurationMapper {

	public static ModuleConfigurationDTO convertToDto(ModuleConfiguration entity) {

		if (entity == null) {
			return null;
		}
		ModuleConfigurationDTO dto = new ModuleConfigurationDTO();
		convertToDto(entity, dto);
		return dto;
	}

	private static void convertToDto(ModuleConfiguration entity, ModuleConfigurationDTO dto) {

		dto.setDescription(entity.getDescription());
		dto.setId(entity.getId());
		dto.setName(entity.getName());

		entity.getParameterValues().entrySet()
				.forEach(p -> dto.getParameterValues().put(ParameterMapper.convertToDto(p.getKey()), p.getValue()));
		dto.setModule(ModuleMapper.convertToDto(entity.getModule()));
	}

	public static ModuleConfiguration convertToEntity(ModuleConfigurationDTO dto) {

		if (dto == null) {
			return null;
		}

		ModuleConfiguration entity = new ModuleConfiguration();
		convertToEntity(entity, dto);

		return entity;
	}

	private static void convertToEntity(ModuleConfiguration entity, ModuleConfigurationDTO dto) {

		entity.setDescription(dto.getDescription());
		entity.setId(0);
		entity.setName(dto.getName());

		dto.getParameterValues().entrySet().forEach(
				p -> entity.getParameterValues().put(ParameterMapper.convertToEntity(p.getKey()), p.getValue()));
		
		entity.setModule(ModuleMapper.convertToEntity(dto.getModule()));
	}

}
