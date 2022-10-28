package com.docshifter.core.config;

public enum InstallationType {
	CLASSICAL,
	CONTAINERIZED_KUBERNETES,
	CONTAINERIZED_GENERIC;

	public boolean isContainerized() {
		return this != CLASSICAL;
	}
}
