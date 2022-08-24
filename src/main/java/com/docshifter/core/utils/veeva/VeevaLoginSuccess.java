package com.docshifter.core.utils.veeva;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper=false)
public class VeevaLoginSuccess extends VeevaResponse {

	private String id;
	private String sessionId;
	private long userId;
	private List<VeevaVaultId> vaultIds;
	private long vaultId;
	private ResponseDetails responseDetails;
	private List<Object> data;
	private String responseMessage;

	@Data
	public static class ResponseDetails {
		private String limit;
		private String offset;
		private String size;
		private String total;
	}
}
