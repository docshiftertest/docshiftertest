package com.docbyte.docshifter.messaging;

import javax.jms.Message;

public interface IDocShifterController {

	public abstract boolean onMessage(Message message);

}