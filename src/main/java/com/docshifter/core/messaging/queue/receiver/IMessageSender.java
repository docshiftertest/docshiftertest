package com.docshifter.core.messaging.queue.receiver;

import com.docshifter.core.messaging.message.DocShifterMetricsReceiverMessage;

/**
 * Created by Julian Isaac on 29.07.2021
 */
public interface IMessageSender {

	/**
	 * Send a Metrics message
	 * @param message The DocShifterMetricsReceiverMessage to send
	 */
	void sendMetrics(DocShifterMetricsReceiverMessage message);
}
