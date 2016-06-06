package com.docbyte.docshifter.messaging;

public interface IDocShifterController<T> {

	boolean onMessage(T message) throws Exception;

}