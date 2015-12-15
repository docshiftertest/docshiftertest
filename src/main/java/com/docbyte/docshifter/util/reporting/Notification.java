package com.docbyte.docshifter.util.reporting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public abstract class Notification {

	
	private String destination;
	private String origin;
	private String subject;
	private String message;
	private File attachment;
	
	
	
	
	public Notification(String destination, String origin, String subject,	String message, File attachment) {
		this.destination = destination;
		this.origin = origin;
		this.subject = subject;
		this.message = message;
		this.attachment = attachment;
	}

	public Notification(String destination, String origin, String subject,	String message) {
		this(destination, origin, subject, message, null);
	}


	public abstract boolean send();


	public static Notification getFromProps(String properties) {
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(new File(properties)));
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
		 
		return getFromProps(prop);
		
	}
	
	public static Notification getFromProps(Properties prop) {
		
		
		
		Type type = Type.valueOf(prop.getProperty("docshifter.notification.type"));
		
		switch (type) {
			case MAIL:
				String destination = prop.getProperty("docshifter.notification.destination");
				String origin = prop.getProperty("docshifter.notification.origin");
				String host = prop.getProperty("docshifter.notification.host");
				return new EmailNotification(destination, origin, host);
			default:
				return null;
		}
		
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public File getAttachment() {
		return attachment;
	}

	public void setAttachment(File attachment) {
		this.attachment = attachment;
	}

	public enum Type {
		MAIL
	}
}
