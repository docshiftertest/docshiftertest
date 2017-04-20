package com.docshifter.core.messaging.queue.sender;

public interface IMessageSenderOrPublisher {

	void close();

	void run();

}