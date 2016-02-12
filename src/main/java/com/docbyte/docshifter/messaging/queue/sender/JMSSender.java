package com.docbyte.docshifter.messaging.queue.sender;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;

import com.docbyte.docshifter.config.ConfigurationServer;
import com.docbyte.docshifter.config.Task;
import com.docbyte.docshifter.model.vo.ChainConfiguration;
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

	
	private JMSSender() {
	}

	private JMSSender(String queueNameSuffix) {
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
			ChainConfiguration config = ConfigurationServer.getPrintserviceTransformationConfiguration(queueName);
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

}
