package com.docshifter.core.utils.dctm;

import com.documentum.fc.common.DfException;

/**
 * Interface for handling DCTM sessions
 *
 * @author $Author$
 * @version $Rev$
 * Last Modification Date: $Date$
 *
 */

public interface ISessionManager<T> {

	T getSession() throws DfException;

	void release(T session) throws DfException;

	String getDocbase();

}
