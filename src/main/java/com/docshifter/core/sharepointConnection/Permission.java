package com.docshifter.core.sharepointConnection;

/**
 * @author Juan Marques created on 29/07/2020
 *
 */
public enum Permission {

	 Full_Control("1073741829"),
	 Design("1073741828"),
	 Edit("1073741830"),
	 Contribute("1073741827"),	
	 Read("1073741826"),
	 View_Only("1073741924");

	private final String code;

	Permission(String string) {
		this.code = string;
	}

	public String toString() {
		return code;
	}
	
}
