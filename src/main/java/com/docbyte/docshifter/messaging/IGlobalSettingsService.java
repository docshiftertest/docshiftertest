package com.docbyte.docshifter.messaging;


import com.docshifter.core.config.domain.GlobalSettings;

public interface IGlobalSettingsService {

	public IGlobalSettings get();

	public void save(GlobalSettings settings);
}
