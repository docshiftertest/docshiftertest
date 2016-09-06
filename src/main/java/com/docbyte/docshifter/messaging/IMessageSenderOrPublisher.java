package com.docbyte.docshifter.messaging;

public interface IMessageSenderOrPublisher {

	void close();

	void run();

}