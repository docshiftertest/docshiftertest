/**
 * 
 */
package com.docshifter.security.utils;

/**
 * Security properties.
 * @author Created by juan.marques on 05/12/2019.
 */
public enum SecurityProperties {

	TYPE_PASSWORD("password"), 
	SECRET("DS_TOP_SECRET"),
	DEFAULT_ALGORITHM("PBEWITHHMACSHA384ANDAES_256"),
	JASYPT_VM_ARGUMENT("jasypt.encryptor.password"),
	MODULE_PARAMETER_VALUES("parameterValues");

	private String value;

	SecurityProperties(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
