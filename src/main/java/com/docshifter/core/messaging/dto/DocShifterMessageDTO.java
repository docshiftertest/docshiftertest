package com.docshifter.core.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author Juan Marques created on 27/11/2020
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocShifterMessageDTO implements Serializable {

	private String jmsMessageID;
	private Long messageID;
	private byte messagePriority;
	private byte newMessagePriority;
	private String type;
	private Long configId;
	private String taskID;
	private String sourceFilePath;
	private String filename;
	private String folderStructure;
	private ArrayList<String> messages;
	private String workflowName;
	private Map<String, Object> data;
	private String hostname;
	private String status;

	public enum Status {

		WAITING_TO_BE_PROCESSED("WAITING TO BE PROCESSED"),
		PROCESSING("PROCESSING"),
		PROCESSED("PROCESSED"),
		REMOVED("REMOVED");

		private final String value;

		Status(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public static boolean isWaitingToBeProcessed(String status) {
			return status.equals(Status.WAITING_TO_BE_PROCESSED.value);
		}

		public static boolean isProcessing(String status) {
			return status.equals(Status.PROCESSING.value);
		}
	}

}
