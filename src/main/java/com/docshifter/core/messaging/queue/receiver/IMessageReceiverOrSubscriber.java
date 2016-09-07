package com.docshifter.core.messaging.queue.receiver;

import javax.jms.Message;
import javax.jms.MessageListener;

public interface IMessageReceiverOrSubscriber extends MessageListener {

	void onMessage(Message message);

	void run();

}