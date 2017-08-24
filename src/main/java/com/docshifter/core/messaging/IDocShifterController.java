package com.docshifter.core.messaging;

import com.docshifter.core.task.Task;

public interface IDocShifterController<T> {

	Task onMessage(T message) throws Exception;

}