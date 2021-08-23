package com.redhat.qute.parser;

import java.util.concurrent.CancellationException;

/**
 * Used for processing requests with cancellation support.
 */
public interface CancelChecker {

	/**
	 * Throw a {@link CancellationException} if the currently processed request
	 * has been canceled.
	 */
	void checkCanceled();

	/**
	 * Check for cancellation without throwing an exception.
	 */
	default boolean isCanceled() {
		try {
			checkCanceled();
		} catch (CancellationException ce) {
			return true;
		}
		return false;
	}

}
