package com.docshifter.core.messaging;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.DateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.springframework.util.Assert.*;

@SpringBootTest
public class DateDeserializerTest {
	
	
	@Test
	public void deserializeShouldFail1() throws Exception {
		DateDeserializer dateDeserializer = new DateDeserializer();
		Object dateObj =dateDeserializer.deserializeDate("notadate");
		
		isNull(dateObj);
	}
	
	@Test
	public void deserializeShouldFail2() throws Exception {
		DateDeserializer dateDeserializer = new DateDeserializer();
		Object dateObj = dateDeserializer.deserializeDate("12345778");
		
		isNull(dateObj);
	}
	
	/**
	 * Timezones in dates differ because of Winter/summer hours
	 */
	@Test
	public void deserializeShouldParse1() throws Exception {
		DateDeserializer dateDeserializer = new DateDeserializer();
		Object dateObj = dateDeserializer.deserializeDate("2017-11-27T15:49:37+01:00");
		notNull(dateObj);
		isInstanceOf(Date.class, dateObj);
		Instant date = ((Date) dateObj).toInstant();
		ZonedDateTime zdt = ZonedDateTime.ofInstant(date, ZoneId.systemDefault());
		
		assertEquals(2017, zdt.getYear());
		assertEquals(11, zdt.getMonthValue());
		assertEquals(27, zdt.getDayOfMonth());
		assertEquals(15, zdt.getHour());
		assertEquals(49, zdt.getMinute());
		assertEquals(37, zdt.getSecond());
	}
	
	/**
	 * Timezones in dates differ because of Winter/summer hours
	 */
	@Test
	public void deserializeShouldParse2() throws Exception {
		DateDeserializer dateDeserializer = new DateDeserializer();
		Object dateObj = dateDeserializer.deserializeDate("1991-09-27T01:05:32+02:00");
		notNull(dateObj);
		isInstanceOf(Date.class, dateObj);
		
		Instant date = ((Date) dateObj).toInstant();
		ZonedDateTime zdt = ZonedDateTime.ofInstant(date, ZoneId.systemDefault());
		
		assertEquals(1991, zdt.getYear());
		assertEquals(9, zdt.getMonthValue());
		assertEquals(27, zdt.getDayOfMonth());
		assertEquals(1, zdt.getHour());
		assertEquals(5, zdt.getMinute());
		assertEquals(32, zdt.getSecond());
		
	}
	
	
	@Test
	public void deserializeShouldParse3() throws Exception {
		DateDeserializer dateDeserializer = new DateDeserializer();
		Object dateObj = dateDeserializer.deserializeDate("1991-09-27T01:05:32Z");
		notNull(dateObj);
		isInstanceOf(Date.class, dateObj);
		
		Instant date = ((Date) dateObj).toInstant();
		ZonedDateTime zdt = ZonedDateTime.ofInstant(date, ZoneId.of("UTC"));
		
		assertEquals(1991, zdt.getYear());
		assertEquals(9, zdt.getMonthValue());
		assertEquals(27, zdt.getDayOfMonth());
		assertEquals(1, zdt.getHour());
		assertEquals(5, zdt.getMinute());
		assertEquals(32, zdt.getSecond());
		
	}
	
	
	
	public void serializedate() throws Exception {
		ZonedDateTime zdt = ZonedDateTime.of(2017, 11, 27, 15, 49, 37, 0,ZoneId.systemDefault());
		ZonedDateTime zdt2 = ZonedDateTime.of(1991, 11, 27, 1, 49, 37, 0,ZoneId.systemDefault());
		
		final DateFormat formatter = new ISO8601DateFormat();
		
		System.out.println(zdt.toString());
		System.out.println(zdt2.toString());
		System.out.println(formatter.format(Date.from(zdt.toInstant())));
		System.out.println(formatter.format(Date.from(zdt2.toInstant())));
		
		
	}
	
}