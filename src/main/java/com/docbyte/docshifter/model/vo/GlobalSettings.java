package com.docbyte.docshifter.model.vo;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "GLOBALSETTINGS", schema = "DOCSHIFTER")
public class GlobalSettings implements Serializable
{
	private static final long serialVersionUID = -4810606443366108561L;

	private long id;
	
	private String mqSystem;
	private String mqURL;
	private String mqQueue;
	private String mqUser;
	private String mqUserPassword;
	
	private String defaultOpenOfficeHost;
	private String defaultOpenOfficePort;
	
	private String defaultTempFolder;

	public GlobalSettings() {}
	
	public GlobalSettings(String defaultOpenOfficeHost, String defaultOpenOfficePort, String defaultTempFolder)
	{
		this.defaultOpenOfficeHost = defaultOpenOfficeHost;
		this.defaultOpenOfficePort = defaultOpenOfficePort;
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
	
	public GlobalSettings(String defaultOpenOfficeHost,
						  String defaultOpenOfficePort, String defaultTempFolder,
						  String mqSystem, String mqQueue, String mqURL, String mqUser,
						  String mqUserPassword)
	{
		this.defaultOpenOfficeHost = defaultOpenOfficeHost;
		this.defaultOpenOfficePort = defaultOpenOfficePort;
		this.defaultTempFolder = defaultTempFolder;
		this.mqSystem = mqSystem;
		this.mqQueue = mqQueue;
		this.mqURL = mqURL;
		this.mqUser = mqUser;
		this.mqUserPassword = mqUserPassword;
	}

	public String getDefaultOpenOfficeHost()
	{
		return defaultOpenOfficeHost;
	}

	public String getDefaultOpenOfficePort()
	{
		return defaultOpenOfficePort;
	}

	public String getDefaultTempFolder()
	{
		return defaultTempFolder;
	}

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy= GenerationType.AUTO)
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

	public void setDefaultOpenOfficeHost(String defaultOpenOfficeHost)
	{
		this.defaultOpenOfficeHost = defaultOpenOfficeHost;
	}

	public void setDefaultOpenOfficePort(String defaultOpenOfficePort)
	{
		this.defaultOpenOfficePort = defaultOpenOfficePort;
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
	
}
