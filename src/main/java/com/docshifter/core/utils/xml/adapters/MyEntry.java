package com.docshifter.core.utils.xml.adapters;

/**
 * Created by samnang.nop on 11/02/2016.
 */

import jakarta.xml.bind.annotation.XmlAttribute;

public class MyEntry {

	@XmlAttribute(name="value")
	public String value;
	@XmlAttribute(name="key")
	public String key;
}
