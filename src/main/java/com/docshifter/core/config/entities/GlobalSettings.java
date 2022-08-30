package com.docshifter.core.config.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.docshifter.core.security.Encrypted;

@Entity
public class GlobalSettings
{
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private long id;
	
	private String mqSystem;
	private String mqURL;
	private String mqQueue;
	private String mqMetricsQueue;
	private String mqUser;
	@Encrypted
	private String mqUserPassword;

	private String defaultTempFolder;
	private String defaultErrorFolder;

	private String notificationEmailFrom;
	private String notificationEmailTo;
	private String notificationEmailUsername;
	@Encrypted
	private String notificationEmailPassword;
	private String notificationHost;
	private String notificationPort;
	private String notificationSecurityOption;

	private String licenseNotificationDaysBeforeExpire;
	private String licenseTimeInterval;

	public GlobalSettings() {}
	
	public GlobalSettings(String defaultTempFolder)
	{
		this.defaultTempFolder = defaultTempFolder;
	}
	
	public GlobalSettings(String mqSystem, String mqQueue, String mqMetricsQueue, String mqURL, String mqUser, String mqUserPassword)
	{
		this.mqSystem = mqSystem;
		this.mqQueue = mqQueue;
		this.mqMetricsQueue = mqMetricsQueue;
		this.mqURL = mqURL;
		this.mqUser = mqUser;
		this.mqUserPassword = mqUserPassword;
	}
	
	public GlobalSettings(String defaultTempFolder,
					  String mqSystem, String mqQueue, String mqMetricsQueue, String mqURL, String mqUser,
						  String mqUserPassword)
	{
		this.defaultTempFolder = defaultTempFolder;
		this.mqSystem = mqSystem;
		this.mqQueue = mqQueue;
		this.mqMetricsQueue = mqMetricsQueue;
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

	public String getMqMetricsQueue()
	{
		return mqMetricsQueue;
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

	public String getNotificationEmailFrom() {
		return notificationEmailFrom;
	}

	public void setNotificationEmailFrom(String notificationEmailFrom) {
		this.notificationEmailFrom = notificationEmailFrom;
	}

	public String getNotificationEmailTo() {
		return notificationEmailTo;
	}

	public void setNotificationEmailTo(String notificationEmailTo) {
		this.notificationEmailTo = notificationEmailTo;
	}

	public String getNotificationEmailUsername() {
		return notificationEmailUsername;
	}

	public void setNotificationEmailUsername(String notificationEmailUsername) {
		this.notificationEmailUsername = notificationEmailUsername;
	}

	public String getNotificationEmailPassword() {
		return notificationEmailPassword;
	}

	public void setNotificationEmailPassword(String notificationEmailPassword) {
		this.notificationEmailPassword = notificationEmailPassword;
	}

	public String getNotificationHost() {
		return notificationHost;
	}

	public void setNotificationHost(String notificationHost) {
		this.notificationHost = notificationHost;
	}

	public String getNotificationPort() {
		return notificationPort;
	}

	public void setNotificationPort(String notificationPort) {
		this.notificationPort = notificationPort;
	}

	public String getNotificationSecurityOption() {
		return notificationSecurityOption;
	}

	public void setNotificationSecurityOption(String notificationSecurityOption) {
		this.notificationSecurityOption = notificationSecurityOption;
	}

	public String getLicenseNotificationDaysBeforeExpire() {
		return licenseNotificationDaysBeforeExpire;
	}

	public void setLicenseNotificationDaysBeforeExpire(String licenseNotificationDaysBeforeExpire) {
		this.licenseNotificationDaysBeforeExpire = licenseNotificationDaysBeforeExpire;
	}

	public String getLicenseTimeInterval() {
		return licenseTimeInterval;
	}

	public void setLicenseTimeInterval(String licenseTimeInterval) {
		this.licenseTimeInterval = licenseTimeInterval;
	}
}
