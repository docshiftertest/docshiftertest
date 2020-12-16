package com.docshifter.core.utils.veeva;

import java.util.List;

public class VeevaLoginSuccess extends VeevaResponse {

	private String id;
	private String sessionId;
	private long userId;
	private List<VeevaVaultId> vaultIds;
	private long vaultId;
	private ResponseDetails responseDetails;
	private List<Object> data;
	private String responseMessage;


	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public List<VeevaVaultId> getVaultIds() {
		return vaultIds;
	}
	public void setVaultIds(List<VeevaVaultId> vaultIds) {
		this.vaultIds = vaultIds;
	}
	public long getVaultId() {
		return vaultId;
	}
	public void setVaultId(long vaultId) {
		this.vaultId = vaultId;
	}
	public String getResponseMessage() {
		return responseMessage;
	}
	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	public ResponseDetails getResponseDetails() {
		return responseDetails;
	}

	public void setResponseDetails(ResponseDetails responseDetails) {
		this.responseDetails = responseDetails;
	}

	public List<Object> getData() {
		return data;
	}

	public void setData(List<Object> data) {
		this.data = data;
	}

	/**
	 * Give a nice representation of this class for debugging
	 */
	@Override
	public String toString() {
		StringBuilder sBuf = new StringBuilder(super.toString());
		sBuf.append(", Id: ");
		sBuf.append(this.id);
		sBuf.append(", Session Id: ");
		sBuf.append(this.sessionId);
		sBuf.append(", User Id: ");
		sBuf.append(this.userId);
		sBuf.append(", Vault Ids: [");
		for (VeevaVaultId vId : this.vaultIds) {
			sBuf.append("Vault Id Object: {");
			sBuf.append(vId.toString());
			sBuf.append("}, ");
		}
		if (sBuf.length() > 1) {
			sBuf.setLength(sBuf.length() - 2);
		}
		sBuf.append("], Vault Id: ");
		sBuf.append(this.vaultId);
		sBuf.append(", Response Deatils: ");
		sBuf.append(this.responseDetails.toString());
		sBuf.append(", Response Message: ");
		sBuf.append(this.responseMessage);
		return sBuf.toString();
	}

	public class ResponseDetails {
		private String limit;
		private String offset;
		private String size;
		private String total;

		public String getLimit() {
			return limit;
		}

		public void setLimit(String limit) {
			this.limit = limit;
		}

		public String getOffset() {
			return offset;
		}

		public void setOffset(String offset) {
			this.offset = offset;
		}

		public String getSize() {
			return size;
		}

		public void setSize(String size) {
			this.size = size;
		}

		public String getTotal() {
			return total;
		}

		public void setTotal(String total) {
			this.total = total;
		}

		/**
		 * Give a nice representation of this class for debugging
		 */
		@Override
		public String toString() {
			StringBuilder sBuf = new StringBuilder();
			sBuf.append("{");
			sBuf.append("limit: ");
			sBuf.append(this.getLimit());
			sBuf.append("offset: ");
			sBuf.append(this.getOffset());
			sBuf.append("size: ");
			sBuf.append(this.getSize());
			sBuf.append("total: ");
			sBuf.append(this.getTotal());
			sBuf.append("}");
			return sBuf.toString();
		}
	}
}
