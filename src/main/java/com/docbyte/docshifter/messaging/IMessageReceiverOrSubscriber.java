package com.docbyte.docshifter.messaging;

import javax.jms.Message;
import javax.jms.MessageListener;

public interface IMessageReceiverOrSubscriber extends MessageListener {

	public abstract void onMessage(Message message);

	public abstract void run();

}