package com.docbyte.docshifter.model.vo;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "GLOBALSETTINGS", schema = "DOCSHIFTER")
public class GlobalSettings implements Serializable
{
	private static final long serialVersionUID = -4810606443366108561L;

	private long id;
	
	private String jmsSystem;
	private String jmsURL;
	private String jmsQueue;
	private String jmsUser;
	private String jmsUserPassword;
	
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
	
	public GlobalSettings(String jmsSystem, String jmsQueue, String jmsURL, String jmsUser, String jmsUserPassword)
	{
		this.jmsSystem = jmsSystem;
		this.jmsQueue = jmsQueue;
		this.jmsURL = jmsURL;
		this.jmsUser = jmsUser;
		this.jmsUserPassword = jmsUserPassword;
	}
	
	public GlobalSettings(String defaultOpenOfficeHost, 
			String defaultOpenOfficePort, String defaultTempFolder,
			String jmsSystem, String jmsQueue, String jmsURL, String jmsUser,
			String jmsUserPassword)
	{
		this.defaultOpenOfficeHost = defaultOpenOfficeHost;
		this.defaultOpenOfficePort = defaultOpenOfficePort;
		this.defaultTempFolder = defaultTempFolder;
		this.jmsSystem = jmsSystem;
		this.jmsQueue = jmsQueue;
		this.jmsURL = jmsURL;
		this.jmsUser = jmsUser;
		this.jmsUserPassword = jmsUserPassword;
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

	public String getJmsQueue()
	{
		return jmsQueue;
	}

	public String getJmsURL()
	{
		return jmsURL;
	}

	public String getJmsUser()
	{
		return jmsUser;
	}

	public String getJmsUserPassword()
	{
		return jmsUserPassword;
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

	public void setJmsQueue(String jmsQueue)
	{
		this.jmsQueue = jmsQueue;
	}

	public void setJmsURL(String jmsURL)
	{
		this.jmsURL = jmsURL;
	}

	public void setJmsUser(String jmsUser)
	{
		this.jmsUser = jmsUser;
	}

	public void setJmsUserPassword(String jmsUserPassword)
	{
		this.jmsUserPassword = jmsUserPassword;
	}
	
	public String getJmsSystem() {
		return jmsSystem;
	}

	public void setJmsSystem(String jmsSystem) {
		this.jmsSystem = jmsSystem;
	}
	
}
