package com.docbyte.docshifter.model.dao.inter;

import com.docbyte.docshifter.model.vo.GlobalSettings;

public interface IGlobalSettingsDAO {

	public GlobalSettings get();
	public void save(GlobalSettings config);
}
