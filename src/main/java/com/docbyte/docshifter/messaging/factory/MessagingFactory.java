package com.docbyte.docshifter.messaging.factory;

import com.docbyte.docshifter.messaging.IDocShifterController;
import com.docbyte.docshifter.messaging.IDocShifterSender;
import com.docbyte.docshifter.messaging.queue.receiver.IMessageReceiver;
import com.docbyte.docshifter.messaging.queue.receiver.JMSReceiver;
import com.docbyte.docshifter.messaging.queue.sender.IMessageSender;
import com.docbyte.docshifter.messaging.queue.sender.JMSSender;
import com.docbyte.docshifter.messaging.topic.subscriber.IMessageSubscriber;
import com.docbyte.docshifter.messaging.topic.subscriber.JMSSubscriber;

public class MessagingFactory {

	public static IMessageReceiver getMessageReceiver(
			IDocShifterController docShifterController, String queueNameSuffix) {
		return new JMSReceiver(docShifterController,queueNameSuffix);
	}

	public static IMessageReceiver getMessageReceiverInstance(
			IDocShifterController docShifterController) {
		return JMSReceiver.getInstance(docShifterController);
	}

	public static IMessageSender getMessageSender() {
		return new JMSSender();
	}

	public static IMessageSender getMessageSender(String queueNameSuffix) {
		return new JMSSender(queueNameSuffix);
	}

	public static IMessageSender getMessageSenderInstance(String queueNameSuffix) {
		return JMSSender.getInstance(queueNameSuffix);
	}

	public static IMessageSubscriber getMessageSubscriber(
			IDocShifterSender instance) {
		return new JMSSubscriber(instance);
	}

}
