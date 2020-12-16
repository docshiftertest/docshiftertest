package com.docshifter.core.utils.dctm;

public class FieldInfo {
	private String returnName;
	private String fieldDefinition;
	private String clause;
	
	public FieldInfo(String fieldDefinition, String clause, String returnName) {
		this.returnName = returnName;
		this.fieldDefinition = fieldDefinition;
		this.clause = clause;
	}
	
	public String getReturnName() {
		return returnName;
	}
	
	public String getFieldDefinition() {
		return fieldDefinition;
	}
	
	public String getClause() {
		return clause;
	}
	
	
}