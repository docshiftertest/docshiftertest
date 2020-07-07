package com.docshifter.core.config.mapping;

import java.util.HashSet;

import com.docshifter.core.config.domain.Node;
import com.docshifter.core.config.dto.NodeDTO;

public class NodeMapper {

	public static NodeDTO convertToDto(Node entity) {
		if (entity == null) {
			return null;
		}
		NodeDTO dto = new NodeDTO();
		convertToDto(entity, dto);
		return dto;
	}

	private static void convertToDto(Node entity, NodeDTO dto) {

		updateDTO(entity, dto);
	}

	public static Node convertToEntity(NodeDTO dto) {

		if (dto == null) {
			return null;
		}

		Node entity = new Node();
		convertToEntity(entity, dto);
		return entity;
	}

	private static void convertToEntity(Node entity, NodeDTO dto) {

		entity.setChildNodes(new HashSet<Node>());
		updateEntity(entity, dto);
	}

	private static void updateDTO(Node entity, NodeDTO dto) {
		dto.setModuleConfiguration(ModuleConfigurationMapper.convertToDto(entity.getModuleConfiguration()));

		if (entity.getParentNode() != null) {

			dto.setParentNode(NodeDTO.builder()
					.moduleConfiguration(
							ModuleConfigurationMapper.convertToDto(entity.getParentNode().getModuleConfiguration()))
					.build());
		}

		for (Node oldChild : entity.getChildNodes()) {
			NodeDTO newChild = new NodeDTO();
			newChild.setChildNodes(new HashSet<NodeDTO>());
			dto.getChildNodes().add(newChild);
			if (entity.getParentNode() != null) {

				dto.setParentNode(NodeDTO.builder()
						.moduleConfiguration(
								ModuleConfigurationMapper.convertToDto(entity.getParentNode().getModuleConfiguration()))
						.build());
				dto.getParentNode().getChildNodes().add(newChild);
			}
			updateDTO(oldChild, newChild);
		}
	}

	private static void updateEntity(Node entity, NodeDTO dto) {
		entity.setModuleConfiguration(ModuleConfigurationMapper.convertToEntity(dto.getModuleConfiguration()));

		if (dto.getParentNode() != null) {
			Node newNode = new Node();
			newNode.setId(dto.getParentNode().getId());
			newNode.setModuleConfiguration(
					ModuleConfigurationMapper.convertToEntity(dto.getParentNode().getModuleConfiguration()));
			entity.setParentNode(newNode);
		}

		for (NodeDTO oldChild : dto.getChildNodes()) {
			Node newChild = new Node();
			entity.setChildNodes(new HashSet<Node>());
			entity.getChildNodes().add(NodeMapper.convertToEntity(oldChild));

			if (dto.getParentNode() != null) {
				Node newNode = new Node();
				newNode.setId(dto.getParentNode().getId());
				newNode.setModuleConfiguration(
						ModuleConfigurationMapper.convertToEntity(dto.getParentNode().getModuleConfiguration()));
				entity.setParentNode(newNode);

				entity.getParentNode().setChildNodes(new HashSet<Node>());
				entity.getParentNode().getChildNodes().add(newNode);
			}

			updateEntity(newChild, oldChild);
		}
	}

}
