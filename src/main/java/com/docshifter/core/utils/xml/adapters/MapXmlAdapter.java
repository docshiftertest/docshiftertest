package com.docshifter.core.utils.xml.adapters;

/**
 * Created by samnang.nop on 11/02/2016.
 */

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.Map;


public class MapXmlAdapter extends XmlAdapter<MyMap, Map<String, Object>> {

	@Override
	public Map<String, Object> unmarshal(MyMap value) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public MyMap marshal(Map<String, Object> value) throws Exception {
		MyMap map = new MyMap();
		map.entries = new ArrayList<MyEntry>();
		for (String key : value.keySet()) {
			MyEntry entry = new MyEntry();
			entry.key = key;
			entry.value = value.get(key).toString();
			map.entries.add(entry);
		}
		return map;
	}
}
