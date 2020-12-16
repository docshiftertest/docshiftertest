package com.docshifter.core.utils.veeva;

import java.time.Instant;

/**
 * Interface for handling Veeva sessions
 */

public interface ISession {

	String getSessionID();

	Instant getAcquisitionTime();

	String getHost();

}
