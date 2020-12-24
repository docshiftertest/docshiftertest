package com.docshifter.core.utils.xml.adapters;

/**
 * Created by samnang.nop on 11/02/2016.
 */

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

public class MyMap {
	@XmlElement(name="attribute")
	public List<MyEntry> entries;
}