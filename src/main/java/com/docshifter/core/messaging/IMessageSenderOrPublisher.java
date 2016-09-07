package com.docshifter.core.messaging;

public interface IMessageSenderOrPublisher {

	void close();

	void run();

}