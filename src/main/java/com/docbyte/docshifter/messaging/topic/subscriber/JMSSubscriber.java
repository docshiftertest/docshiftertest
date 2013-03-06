package com.docbyte.docshifter.messaging.topic.subscriber;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import com.docbyte.docshifter.messaging.IDocShifterSender;
import com.docbyte.docshifter.util.Logger;

public class JMSSubscriber extends AbstractJMSSubscriber implements IMessageSubscriber{
		
	private IDocShifterSender docShifterInstance;

	public JMSSubscriber(IDocShifterSender instance) {
		docShifterInstance = instance;
	}

	public void onMessage(Message message) {
		try {
			if(((TextMessage) message).getText().equalsIgnoreCase("configUpdated")){
				docShifterInstance.restartNotStatic();
			}
		} catch (JMSException e) {
			Logger.error("JMSSub(onMsg): JMS Exception occured, make sure the JMS provider is running correctly.", e);
		}
	}
	
	
}
