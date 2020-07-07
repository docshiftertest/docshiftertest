package com.docshifter.core.config.mapping;

import com.docshifter.core.config.domain.Parameter;
import com.docshifter.core.config.dto.ParameterDTO;

public class ParameterMapper {

	public static ParameterDTO convertToDto(Parameter entity) {

		if (entity == null) {
			return null;
		}
		ParameterDTO dto = new ParameterDTO();
		convertToDto(entity, dto);
		return dto;
	}

	private static void convertToDto(Parameter entity, ParameterDTO dto) {

		dto.setDescription(entity.getDescription());
		dto.setId(entity.getId());
		dto.setName(entity.getName());
		dto.setValuesJson(entity.getValuesJson());
		dto.setRequired(entity.getRequired());
		dto.setType(entity.getType());
	}

	public static Parameter convertToEntity(ParameterDTO dto) {

		if (dto == null) {
			return null;
		}

		Parameter entity = new Parameter();
		convertToEntity(entity, dto);

		return entity;
	}

	private static void convertToEntity(Parameter entity, ParameterDTO dto) {

		entity.setDescription(dto.getDescription());
		entity.setId(dto.getId());
		entity.setName(dto.getName());
		entity.setValuesJson(dto.getValuesJson());
		entity.setRequired(dto.getRequired());
		entity.setType(dto.getType());
	}

}
