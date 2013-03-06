package com.docbyte.docshifter.messaging;

public interface IMessageSenderOrPublisher {

	public abstract void close();

	public abstract void run();

}