package com.docshifter.core.messaging;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;

public class DateDeserializer extends UntypedObjectDeserializer {
	
	private final DateFormat formatter = new ISO8601DateFormat();
	private final String dateRegex = "(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2})([+-](\\d{2}):(\\d{2})|Z)";
	
	@Override
	public Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
		
		if (jp.getCurrentTokenId() == JsonTokenId.ID_STRING) {
			Object deserObj = deserializeDate(jp.getText());
			if (deserObj == null) {
				return super.deserialize(jp, ctxt);
			}
			return deserObj;
			
		} else {
			return super.deserialize(jp, ctxt);
		}
	}
	
	public static ObjectMapper regObjectMapper(ObjectMapper mapper) {
		SimpleModule simpleModule = new SimpleModule();
		simpleModule.addDeserializer(Object.class, new DateDeserializer());
		mapper.registerModule(simpleModule);
		return mapper;
	}
	
	public Object deserializeDate(String text) {
		if (text.matches(dateRegex)) {
			try {
				return formatter.parse(text);
			} catch (ParseException e) {
				return null;
			}
		}
		return null;
	}
}