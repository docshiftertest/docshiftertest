package com.docshifter.core.config.mapping;

import com.docshifter.core.config.domain.Module;
import com.docshifter.core.config.dto.ModuleDTO;

public class ModuleMapper {

	public static ModuleDTO convertToDto(Module entity) {
		if (entity == null) {
			return null;
		}
		ModuleDTO dto = new ModuleDTO();
		convertToDto(entity, dto);
		return dto;
	}

	private static void convertToDto(Module entity, ModuleDTO dto) {

		dto.setClassname(entity.getClassname());
		dto.setCondition(entity.getCondition());
		dto.setDescription(entity.getDescription());
		dto.setId(entity.getId());
		dto.setInputFiletype(entity.getInputFiletype());
		dto.setName(entity.getName());
		dto.setOutputFileType(entity.getOutputFileType());

		entity.getParameters().forEach(p -> dto.getParameters().add((ParameterMapper.convertToDto(p))));

		dto.setType(entity.getType());

	}

	public static Module convertToEntity(ModuleDTO dto) {

		if (dto == null) {
			return null;
		}

		Module entity = new Module();
		convertToEntity(entity, dto);
		return entity;
	}

	private static void convertToEntity(Module entity, ModuleDTO dto) {

		entity.setClassname(dto.getClassname());
		entity.setCondition(dto.getCondition());
		entity.setDescription(dto.getDescription());
		entity.setId(dto.getId());
		entity.setInputFiletype(dto.getInputFiletype());
		entity.setName(dto.getName());
		entity.setOutputFileType(dto.getOutputFileType());

		dto.getParameters().forEach(p -> entity.getParameters().add((ParameterMapper.convertToEntity(p))));

		entity.setType(dto.getType());

	}

}
