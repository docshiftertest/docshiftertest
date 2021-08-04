package com.docshifter.core.messaging.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Created by Julian Isaac on 29.07.2021
 */
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper=false)
public class DocShifterMetricsSenderMessage extends DocShifterMetricsMessage {

	private Long senderPickedUp;
	private String workflowName;

	public MessageSource getMessageSource() {
		return MessageSource.SENDER;
	}
}
