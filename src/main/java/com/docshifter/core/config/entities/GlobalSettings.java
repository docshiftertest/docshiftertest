package com.docshifter.core.config.entities;

import com.docshifter.core.security.Encrypted;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
public class GlobalSettings implements Serializable
{
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private long id;
	
	private String mqSystem;
	private String mqURL;
	private String mqQueue;
	private String mqMetricsQueue;
	private String mqOngoingTaskQueue;
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
	
	public GlobalSettings(String mqSystem,
						  String mqQueue, String mqMetricsQueue, String mqOngoingTaskQueue,
						  String mqURL, String mqUser, String mqUserPassword)
	{
		this.mqSystem = mqSystem;
		this.mqQueue = mqQueue;
		this.mqMetricsQueue = mqMetricsQueue;
		this.mqOngoingTaskQueue = mqOngoingTaskQueue;
		this.mqURL = mqURL;
		this.mqUser = mqUser;
		this.mqUserPassword = mqUserPassword;
	}
	
	public GlobalSettings(String defaultTempFolder, String mqSystem,
						  String mqQueue, String mqMetricsQueue, String mqOngoingTaskQueue,
						  String mqURL, String mqUser, String mqUserPassword)
	{
		this.defaultTempFolder = defaultTempFolder;
		this.mqSystem = mqSystem;
		this.mqQueue = mqQueue;
		this.mqMetricsQueue = mqMetricsQueue;
		this.mqOngoingTaskQueue = mqOngoingTaskQueue;
		this.mqURL = mqURL;
		this.mqUser = mqUser;
		this.mqUserPassword = mqUserPassword;
	}

	public GlobalSettings(String mqSystem, String mqURL,
						  String mqQueue, String mqMetricsQueue, String mqOngoingTaskQueue,
						  String mqUser, String mqUserPassword, String defaultTempFolder, String defaultErrorFolder) {
		this.mqSystem = mqSystem;
		this.mqURL = mqURL;
		this.mqQueue = mqQueue;
		this.mqMetricsQueue = mqMetricsQueue;
		this.mqOngoingTaskQueue = mqOngoingTaskQueue;
		this.mqUser = mqUser;
		this.mqUserPassword = mqUserPassword;
		this.defaultTempFolder = defaultTempFolder;
		this.defaultErrorFolder = defaultErrorFolder;
	}

	public GlobalSettings(long id, String mqSystem, String mqURL,
						  String mqQueue, String mqMetricsQueue, String mqOngoingTaskQueue,
						  String mqUser, String mqUserPassword, String defaultTempFolder, String defaultErrorFolder) {
		this.id = id;
		this.mqSystem = mqSystem;
		this.mqURL = mqURL;
		this.mqQueue = mqQueue;
		this.mqMetricsQueue = mqMetricsQueue;
		this.mqOngoingTaskQueue = mqOngoingTaskQueue;
		this.mqUser = mqUser;
		this.mqUserPassword = mqUserPassword;
		this.defaultTempFolder = defaultTempFolder;
		this.defaultErrorFolder = defaultErrorFolder;
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

	public String getMqMetricsQueue() {
		return mqMetricsQueue;
	}

	public String getMqOngoingTaskQueue() {
		return mqOngoingTaskQueue;
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

	public void setMqMetricsQueue(String mqMetricsQueue)
	{
		this.mqMetricsQueue = mqMetricsQueue;
	}

	public void setMqOngoingTaskQueue(String mqOngoingTaskQueue) {
		this.mqOngoingTaskQueue = mqOngoingTaskQueue;
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
