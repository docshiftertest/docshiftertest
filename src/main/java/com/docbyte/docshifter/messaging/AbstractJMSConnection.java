package com.docbyte.docshifter.messaging;

import com.docbyte.docshifter.util.Logger;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Session;

public abstract class AbstractJMSConnection implements ExceptionListener {

	public static final int RETRYDELAY = 5;
	public static int retriesLeft = 3;
	protected IDocShifterController controller = null;
	protected int ackMode = Session.AUTO_ACKNOWLEDGE;
	protected String queueName;
	protected String user;
	protected String password;
	protected String url;
	protected String queueNameSuffix;

	public void setAckMode(String ackMode) {
		if ("CLIENT_ACKNOWLEDGE".equals(ackMode)) {
			this.ackMode = Session.CLIENT_ACKNOWLEDGE;
		}
		if ("AUTO_ACKNOWLEDGE".equals(ackMode)) {
			this.ackMode = Session.AUTO_ACKNOWLEDGE;
		}
		if ("DUPS_OK_ACKNOWLEDGE".equals(ackMode)) {
			this.ackMode = Session.DUPS_OK_ACKNOWLEDGE;
		}
		if ("SESSION_TRANSACTED".equals(ackMode)) {
			this.ackMode = Session.SESSION_TRANSACTED;
		}
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public IDocShifterController getController() {
		return controller;
	}

	public void setController(IDocShifterController controller) {
		this.controller = controller;
	}

	public String getQueueNameSuffix() {
		if (queueNameSuffix != null)
			return queueNameSuffix;
		else
			return "";
	}

	public void setQueueNameSuffix(String queueNameSuffix) {
		this.queueNameSuffix = queueNameSuffix;
	}

	public abstract void run();

	public abstract Connection getConnection();

	public void retry(Exception ex) {
		//if(0<=retriesLeft){
		Logger.error("JMS Exception occured, make sure the JMS provider is running correctly. Retry in " + RETRYDELAY + " minutes", ex);
		try {
			Thread.sleep(RETRYDELAY * 60000);
		} catch (InterruptedException e1) {
			Logger.error("Could not put thread in sleep. AbsctractJMSConnection", e1);
		}
		run();
		//}
		//else {
		//Logger.error("JMS Exception occured, make sure the JMS provider is running correctly. No more retries left, Shutting down...", ex);
		//System.exit(1);
		//}
	}

	public void onException(JMSException e) {
		//Logger.error("JMS Exception occured, make sure the JMS provider is running correctly. Shutting down docShifter ...", e);
		try {
			if (getConnection() != null)
				getConnection().close();
		} catch (JMSException ex) {
		} finally {
			retry(e);
		}
	}

}