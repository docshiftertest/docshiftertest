package com.docbyte.docshifter.messaging.queue.sender;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;

import com.docbyte.docshifter.config.ConfigurationServer;
import com.docbyte.docshifter.config.Task;
import com.docbyte.docshifter.config.TransformationConfigurationBean;
import com.docbyte.docshifter.util.Logger;

public class JMSSender extends AbstractJMSSender implements IMessageSender{
	
	private static IMessageSender instance = null;
		
	private synchronized static void createInstance (String queueNameSuffix) {
        if (instance == null){ 
        	instance = new JMSSender(queueNameSuffix);
        }
    }
	 
    public static IMessageSender getInstance (String queueNameSuffix) {
        if (instance == null){
        	createInstance (queueNameSuffix);
        }
        return instance;
    }

	
	public JMSSender() {
	}

	public JMSSender(String queueNameSuffix) {
		setQueueNameSuffix(queueNameSuffix);
	}

	public void sendDocumentumTask(long chainConfigurationID, Task task) throws JMSException{
		if(isStarted()){
			ObjectMessage message=session.createObjectMessage(task);
			message.setStringProperty("type","dctm");
			message.setLongProperty("configID",chainConfigurationID);
			String msg = message.getObject().toString();
			if (msg.length() > 88) {
				msg = msg.substring(0, 88) + "...";
			}
			Logger.info("Sending dctm-message: " + msg+" for file: "+task.getFilePath(),null);
			producer.send(message);
		}else throw new JMSException("Must start the JMSSender first, got "+getNrStarted()+" Connections");
	}

	public void sendTask(long senderConfigurationID, Task task) throws JMSException{
		if(isStarted()){
			ObjectMessage message=session.createObjectMessage(task);
			message.setStringProperty("type","normal");
			message.setLongProperty("configID",senderConfigurationID);
			Logger.info("Sending message: " + senderConfigurationID +" for file: "+task.getFilePath(),null);
			producer.send(message);
		}else throw new JMSException("Must start the JMSSender first, got "+getNrStarted()+" Connections");
	}
	

	public void sendPrintTask(Task task) throws JMSException{
		if(isStarted()){
			ObjectMessage message=session.createObjectMessage(task);
			message.setStringProperty("type","print");
			
			Logger.info("Sending message for file: "+task.getFilePath(),null);
			producer.send(message);
		}else throw new JMSException("Must start the JMSSender first");
	}
	
	
	public void sendTask(String queueName, Task task) throws JMSException{
		if(isStarted()){
			TransformationConfigurationBean config = ConfigurationServer.getPrintserviceTransformationConfiguration(queueName);
			if(config != null){
				long chainConfigurationID = config.getId();
				
				ObjectMessage message=session.createObjectMessage(task);
				message.setStringProperty("type","print");
				message.setLongProperty("configID",chainConfigurationID);
				Logger.info("Sending message: " + chainConfigurationID +" for file: "+task.getFilePath(),null);
				
				producer.send(message);
			}
			else{ 
				Logger.error("No transformation available for queue " +queueName +".", null);
				throw new JMSException("No transformation available for queue " +queueName +".");
			}		
		}
		else { 
			Logger.error("Must start the JMSSender first", null);
			throw new JMSException("Unable to process request.");
		}
	}

	/*
	public void sendCustomTask(HashMap parameters) throws JMSException{
		//why the conversion and not just send this hashmap with setObjectProperty();
		//do the element from hashmap get transfered aswell etc???
		//how expensive is this operation? how expensive does the message get because of this?
		if(started){
			ObjectMessage message=session.createObjectMessage();
			Set s=parameters.keySet();
			Iterator iter=s.iterator();
			Object obj=null;
			String key=null;
			message.setStringProperty("msg","test");//default msg should get overwritten by the "msg" value from HashMap parameters
			while(iter.hasNext()){
				obj=iter.next();
				if(obj instanceof String){
					key=(String)obj;
					message.setObjectProperty(key,parameters.get(key));
				}
			}
			message.setObjectProperty("params",parameters);
			message.setStringProperty("type","custom");

			String tmsg = "no msg";
			if(message.getObject()!=null)tmsg=message.getObject().toString();
			else {
				obj=parameters.get("chaintype");
				if(obj instanceof String)tmsg=(String)obj+" message with msg: ";
				obj=parameters.get("msg");
				if(obj instanceof String)tmsg+=(String)obj;
			}
			if (tmsg.length() > 88) {
				tmsg = tmsg.substring(0, 88) + "...";
			}
			//Logger.info(this,"Sending message: " + tmsg,null,null);
			producer.send(message);
		}else throw new JMSException("Must start the JMSSender first");
	}

	public void sendLocalTask(String inFilePath, String msg) throws JMSException{
		//System.out.println("insendlocal in jmssender");
		if(started){
			ObjectMessage message=session.createObjectMessage();
			message.setStringProperty("type","local");
			message.setStringProperty("infilepath",inFilePath);
			message.setStringProperty("msg",msg);

			String tmsg=null;
			if(message.getObject()!=null)tmsg=message.getObject().toString();
			else {
				tmsg="local message with infilepath: "+inFilePath+" and msg: "+msg;
			}
			if (tmsg.length() > 88) {
				tmsg = tmsg.substring(0, 88) + "...";
			}
			//System.out.println("great");
			//Logger.info(this,"Sending message: " + tmsg,null,null);
			producer.send(message);
		}else throw new JMSException("Must start the JMSSender first");
	}

	public void sendDctmTask(Task task, String docbaseName) throws JMSException{
		if(started){
			ObjectMessage message=session.createObjectMessage(task);
			message.setStringProperty("type","dctm");
			message.setStringProperty("docbasename",docbaseName);
			String msg = message.getObject().toString();
			if (msg.length() > 88) {
				msg = msg.substring(0, 88) + "...";
			}
			Logger.info("Sending message: " + msg,null);
			producer.send(message);
		}else throw new JMSException("Must start the JMSSender first");
	}

	public void sendCustomDctmTask(Task task, String docbaseName, HashMap parameters) throws JMSException{
	if(started){
			ObjectMessage message=session.createObjectMessage(task);
			message.setStringProperty("docbasename",docbaseName);
			Set s=parameters.keySet();
			Iterator iter=s.iterator();
			Object obj=null;
			String key=null;
			message.setStringProperty("msg","test");//default msg should get overwritten by the "msg" value from HashMap parameters
			while(iter.hasNext()){
				obj=iter.next();
				if(obj instanceof String){
					key=(String)obj;
					message.setObjectProperty(key,parameters.get(key));
				}
			}
			message.setObjectProperty("params",parameters);
			message.setStringProperty("type","customdctm");

			String tmsg = "no msg";
			if(message.getObject()!=null)tmsg=message.getObject().toString();
			else {
				obj=parameters.get("chaintype");
				if(obj instanceof String)tmsg=(String)obj+" message with msg: ";
				obj=parameters.get("msg");
				if(obj instanceof String)tmsg+=(String)obj;
			}
			if (tmsg.length() > 88) {
				tmsg = tmsg.substring(0, 88) + "...";
			}
			Logger.info("Sending custom dctm message: " + tmsg,null);
			producer.send(message);
		}else throw new JMSException("Must start the JMSSender first");
	}*/
}
