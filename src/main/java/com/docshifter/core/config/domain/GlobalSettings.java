package com.docshifter.core.config.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.docshifter.core.security.Encrypted;

@Entity
@Table(schema = "DOCSHIFTER", name="GLOBAL_SETTINGS")
public class GlobalSettings
{
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private long id;
	
	private String mqSystem;
	private String mqURL;
	private String mqQueue;
	private String mqUser;
	@Encrypted
	private String mqUserPassword;

	private String defaultTempFolder;
	private String defaultErrorFolder;

	public GlobalSettings() {}
	
	public GlobalSettings(String defaultTempFolder)
	{
		this.defaultTempFolder = defaultTempFolder;
	}
	
	public GlobalSettings(String mqSystem, String mqQueue, String mqURL, String mqUser, String mqUserPassword)
	{
		this.mqSystem = mqSystem;
		this.mqQueue = mqQueue;
		this.mqURL = mqURL;
		this.mqUser = mqUser;
		this.mqUserPassword = mqUserPassword;
	}
	
	public GlobalSettings(String defaultTempFolder,
						  String mqSystem, String mqQueue, String mqURL, String mqUser,
						  String mqUserPassword)
	{
		this.defaultTempFolder = defaultTempFolder;
		this.mqSystem = mqSystem;
		this.mqQueue = mqQueue;
		this.mqURL = mqURL;
		this.mqUser = mqUser;
		this.mqUserPassword = mqUserPassword;
	}


	public String getDefaultTempFolder()
	{
		return defaultTempFolder;
	}


	public long getId()
	{
		return id;
	}

	public String getMqQueue()
	{
		return mqQueue;
	}

	public String getMqURL()
	{
		return mqURL;
	}

	public String getMqUser()
	{
		return mqUser;
	}

	public String getMqUserPassword()
	{
		return mqUserPassword;
	}

	public void setDefaultTempFolder(String defaultTempFolder)
	{
		this.defaultTempFolder = defaultTempFolder;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public void setMqQueue(String mqQueue)
	{
		this.mqQueue = mqQueue;
	}

	public void setMqURL(String mqURL)
	{
		this.mqURL = mqURL;
	}

	public void setMqUser(String mqUser)
	{
		this.mqUser = mqUser;
	}

	public void setMqUserPassword(String mqUserPassword)
	{
		this.mqUserPassword = mqUserPassword;
	}
	
	public String getMqSystem() {
		return mqSystem;
	}

	public void setMqSystem(String mqSystem) {
		this.mqSystem = mqSystem;
	}

	public void setDefaultErrorFolder(String defaultErrorFolder) {
		this.defaultErrorFolder = defaultErrorFolder;
	}

	public String getDefaultErrorFolder() {
		return defaultErrorFolder;
	}
}
