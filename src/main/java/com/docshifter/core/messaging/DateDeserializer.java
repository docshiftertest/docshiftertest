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

public class DateDeserializer extends UntypedObjectDeserializer {
	
	private final DateFormat formatter = new ISO8601DateFormat();
	
	@Override
	public Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
		
		if (jp.getCurrentTokenId() == JsonTokenId.ID_STRING) {
			try {
				return formatter.parse(jp.getText());
			} catch (Exception e) {
				return super.deserialize(jp, ctxt);
			}
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
}