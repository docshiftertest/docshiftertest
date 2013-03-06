package com.docbyte.docshifter.messaging;

import com.docbyte.docshifter.model.vo.GlobalSettings;

public interface IGlobalSettingsService {

	public IGlobalSettings get();
	public void save(GlobalSettings settings);
}
