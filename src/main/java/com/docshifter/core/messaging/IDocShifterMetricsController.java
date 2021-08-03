package com.docshifter.core.messaging;

/**
 * Created by Julian Isaac on 29.07.2021
 */
public interface IDocShifterMetricsController<T> {

	void onMessage(T message) throws Exception;

}