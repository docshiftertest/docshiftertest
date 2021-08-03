package com.docshifter.core.messaging.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Julian Isaac on 29.07.2021
 */
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DocShifterMetricsReceiverMessage extends DocShifterMetricsMessage {

	private Long onMessageHit;
	private Long processingDuration;
	private Long finishTimestamp;
	private Long fileSize;
	private List<String> taskMessages = new ArrayList<>();
	private Boolean success;

	public MessageSource getMessageSource() {
		return MessageSource.RECEIVER;
	}
}
