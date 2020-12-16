package com.docshifter.core.utils.veeva;

/**
 * Interface for handling Veeva sessions
 */

public interface ISessionManager<T> {

	T getSession() throws Exception ;

	String getHost();
}
