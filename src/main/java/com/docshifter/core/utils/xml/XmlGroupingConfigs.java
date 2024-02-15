package com.docshifter.core.utils.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@XmlRootElement(name = "groupingConfigs")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlGroupingConfigs {

	@XmlTransient
	private Set<String[]> keySet;
	
    @XmlElement(name="groupingConfig")
    private List<XmlGroupingConfig> xmlGroupingConfigs = null;

    public List<XmlGroupingConfig> getXmlGroupingConfigs() {
        return xmlGroupingConfigs;
    }

    public void setXmlGroupingConfigs(List<XmlGroupingConfig> xmlGroupingConfigs) {
        this.xmlGroupingConfigs = xmlGroupingConfigs;
    }

    public Optional<XmlGroupingConfig> getByGroupCode(String groupCode) {
    	return xmlGroupingConfigs.stream()
				.filter(cfg -> Arrays.asList(cfg.getGroupCodes()).contains(groupCode))
				.findAny();
	}

    public XmlGroupingConfig getByGroupCodes(String[] groupCodes) {
    	for (XmlGroupingConfig config : this.xmlGroupingConfigs) {
    		if (Arrays.equals(config.getGroupCodes(), groupCodes)) {
    			return config;
    		}
    	}
    	return null;
    }

    public Set<String[]> keySet() {
    	if (keySet == null) {
    		Set<String[]> result = new HashSet<>();
    		for (XmlGroupingConfig config : this.xmlGroupingConfigs) {
    			result.add(config.getGroupCodes());
    		}
    		keySet = result;
    	}
    	return keySet;
    }
}
