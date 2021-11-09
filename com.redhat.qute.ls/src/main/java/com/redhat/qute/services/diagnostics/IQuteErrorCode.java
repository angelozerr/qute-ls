package com.redhat.qute.services.diagnostics;

import java.text.MessageFormat;

/**
 * Qute error code API.
 *
 */
public interface IQuteErrorCode {

	/**
	 * Returns the XML error code.
	 * 
	 * @return the XML error code.
	 */
	String getCode();
	
	String getUnformatedMessage();

	default String getMessage(Object... arguments) {
		return MessageFormat.format(getUnformatedMessage(), arguments);
	}
}
