package com.docshifter.core.utils.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "groupingConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlGroupingConfig {

	@XmlElement(name = "groupTitle")
	private String groupTitle;
	@XmlElement(name = "groupCode")
	private String[] groupCodes = null;

	public String getGroupTitle() {
		return groupTitle;
	}

	public void setGroupTitle(String groupTitle) {
		this.groupTitle = groupTitle;
	}

	public String[] getGroupCodes() {
		return groupCodes;
	}

	public void setGroupCodes(String[] groupCodes) {
		this.groupCodes = groupCodes;
	}
	
	@Override
	public String toString() {
		StringBuilder sBuf = new StringBuilder();
		sBuf.append("XmlGroupingConfig{groupTitle=");
		sBuf.append(groupTitle);
		sBuf.append("', groupCodes=");
		for (String code : groupCodes) {
			sBuf.append("'");
			sBuf.append(code);
			sBuf.append("', ");
		}
		sBuf.setLength(sBuf.length() -2);
		return sBuf.toString();
	}
}
