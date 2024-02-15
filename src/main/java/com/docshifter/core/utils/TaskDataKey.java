package com.docshifter.core.utils;

/**
 * Common keys to be added in task data and used across docshifter.
 * @author Juan Marques created on 08/10/2020
 *
 */
public enum TaskDataKey {

	DOCUMENT_BOUNDARY_TEXT,
	ADD_BOUNDARY_TITLE,

	VEEVA_DOCUMENT_ID,
	VEEVA_MAJOR_VERSION,
	VEEVA_MINOR_VERSION,

	/**
	 * TODO: we added mergeFiles in 7.0, but in the name of consistency we should point people as much as
	 * 	possible to using MERGE_FILES instead, which is in a more similar format compared to other
	 *  "special" task data keys such as DOCUMENT_BOUNDARY_TEXT and ADD_BOUNDARY_TITLE. So this should be
	 * 	completely removed one day...
	 */
	@Deprecated
	MERGE_FILES_OLD("mergeFiles"),

	PROCESS_DIRECTORY("MERGE_FILES"),

	// Used in FileSystem input module and Document hash check option
	PROVIDED_HASH,
	PROVIDED_DIGEST_METHOD,

	// Used in FileSystem export module and Document hash generation (Hash Snapshot module)
	GENERATED_HASH,
	GENERATED_DIGEST_METHOD,

	// Used in Email sender and Email Release
	EMAIL_SENDER,
	EMAIL_RECEIVERS,
	EMAIL_CC,
	EMAIL_BCC,
	EMAIL_SUBJECT,

	/**
	 * Used to check if we should count the pages and fonts in metrics
	 */
	COUNT_ALLOWED,


	/**
	 * Used to store all the output files for a task
	 */
	OUTPUT_FILE_PATH;

	private final String alias;

	TaskDataKey(String alias) {
		this.alias = alias;
	}

	TaskDataKey() {
		alias = null;
	}

	@Override
	public String toString() {
		if (alias == null) {
			return name();
		}
		return alias;
	}
}
