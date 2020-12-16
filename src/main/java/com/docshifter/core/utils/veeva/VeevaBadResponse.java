package com.docshifter.core.utils.veeva;

import java.util.List;

public class VeevaBadResponse extends VeevaResponse {

	private String id;
	private String sessionId;
	private long userId;
	private List<VeevaError> errors;
	private String errorType;
	private String responseMessage;

	/*public void setVeevaBadResponse(String response){
		JSONObject jsonObject = new JSONObject(response);
		setResponseStatus(jsonObject.getString("responseStatus"));
		JSONArray jsonArray = (JSONArray) new JSONObject(response).get("errors");
		List<VeevaError> veevaErrors = new ArrayList<>();
		for(int i=0;i<jsonArray.length();i++){
			JSONObject jsonVeevaError = (JSONObject) (jsonArray.get(0));
			VeevaError veevaError = new VeevaError();
			veevaError.setType(jsonVeevaError.getString("type"));
			veevaError.setMessage(jsonVeevaError.getString("message"));
			veevaErrors.add(veevaError);
		}
		setErrors(veevaErrors);
	}*/

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
	public List<VeevaError> getErrors() {
		return errors;
	}
	public void setErrors(List<VeevaError> errors) {
		this.errors = errors;
	}
	public String getErrorType() {
		return errorType;
	}
	public void setErrorType(String errorType) {
		this.errorType = errorType;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	@Override
	/**
	 * Give a nice representation for debugging
	 */
	public String toString() {
		StringBuilder sBuf = new StringBuilder(super.toString());
		sBuf.append(", Id: ");
		sBuf.append(this.id);
		sBuf.append(", Session Id: ");
		sBuf.append(this.sessionId);
		sBuf.append(", User Id: ");
		sBuf.append(this.userId);
		sBuf.append(", Errors: [");
		for (VeevaError error : this.errors) {
			sBuf.append("Veeva Error: {");
			sBuf.append(error.toString());
			sBuf.append("}, ");
		}
		if (sBuf.length() > 1) {
			sBuf.setLength(sBuf.length() - 2);
		}
		sBuf.append("], Error Type: ");
		sBuf.append(this.errorType);
		sBuf.append(", Response Message: ");
		sBuf.append(this.responseMessage);
		return sBuf.toString();
	}
}
