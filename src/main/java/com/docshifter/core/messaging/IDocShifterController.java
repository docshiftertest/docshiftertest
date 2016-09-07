package com.docshifter.core.messaging;

public interface IDocShifterController<T> {

	boolean onMessage(T message) throws Exception;

}