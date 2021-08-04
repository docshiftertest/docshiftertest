package com.docshifter.core.messaging.message;

import com.docshifter.core.metrics.domain.TaskFile;
import com.docshifter.core.metrics.domain.TaskMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Julian Isaac on 29.07.2021
 */
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper=false)
@Transactional(propagation= Propagation.REQUIRED, readOnly=true, noRollbackFor=Exception.class)
public class DocShifterMetricsReceiverMessage extends DocShifterMetricsMessage {

	private Long onMessageHit;
	private Long processingDuration;
	private Long finishTimestamp;
	@Builder.Default
	private final List<TaskFile> taskFiles = new ArrayList<>();
	@Builder.Default
	private final List<TaskMessage> taskMessages = new ArrayList<>();
	private Boolean success;

	public MessageSource getMessageSource() {
		return MessageSource.RECEIVER;
	}

	public void addAllMessages(List<String> messages) {
		if (messages == null) {
			return;
		}
		for (String message : messages) {
			getTaskMessages().add(new TaskMessage(message));
		}
	}
}
