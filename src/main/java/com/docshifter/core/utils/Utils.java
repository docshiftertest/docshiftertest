package com.docshifter.core.utils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

	@Deprecated(since = "Should use that one ConsoleConfig.customObjectMapper()")
	private static final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).findAndRegisterModules();
	private static final ZoneOffset systemDefaultZoneOffSet = OffsetDateTime.now().getOffset();
	private static final List<String> IGNORED_FIELDS = List.of("id", "moduleVersion");

	public static Map<String, Object> mapJSON(String body) {
		ObjectMapper jsonMapper = new ObjectMapper();
		TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {
		};
		try {
			return jsonMapper.readValue(body, typeRef);
		} catch (JsonParseException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(
					"Something went wrong parsing the requestBody. :" + body + ". Please check your JSON body");
		} catch (JsonMappingException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(
					"Something went wrong Mapping the requestBody. :" + body + ". Please check your JSON body");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(
					"Something went wrong Mapping the requestBody. :" + body + ". Please check your JSON body");
		}
	}

    public static String toJSON(Object obj) {
	    String result;
	    if (obj == null) {
	        return null;
        }

        try {
            result = mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(
                    "Not able to process object: " + obj,
                    e);
        }
        return result;
    }

	/**
	 * @param date      given string E.G ( 2021-08-20);
	 * @param startDate true to get epoch for start date or false to get last minute of endDate
	 * @return Epoch Milli with default system zone.
	 */
	public static long parseDateToLong(String date, boolean startDate) {
		LocalTime localTime;
		if (startDate) {
			localTime = LocalTime.of(0, 0, 0);
		}
		else {
			localTime = LocalTime.of(23, 59, 59);
		}

		return LocalDateTime.of(LocalDate.parse(date), localTime).toInstant(systemDefaultZoneOffSet).toEpochMilli();
	}

	/**
	 * Converts a json string to a class
	 * @param json json as String
	 * @param clazz class to be converted to
	 * @return instance of a Class provided
	 */
	public static <T> T clazzFromJson(String json, Class<T> clazz) {

		T result;
		try {
			result = mapper.readValue(json, clazz);
		}
		catch (JsonProcessingException jsonProcessingException) {
			throw new IllegalArgumentException(
					"Not able to process: " + clazz.getSimpleName(),
					jsonProcessingException);
		}

		return result;
	}

}
