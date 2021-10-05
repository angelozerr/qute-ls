/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt.internal.ls;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.ls.core.internal.IDelegateCommandHandler;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;

import com.redhat.qute.jdt.IJavaDataModelChangedListener;
import com.redhat.qute.jdt.internal.JavaDataModelListenerManager;

/**
 * Abstract class for Qute JDT LS command handler
 *
 * @author Angelo ZERR
 *
 */
public abstract class AbstractQuteDelegateCommandHandler implements IDelegateCommandHandler {

	private static final Logger LOGGER = Logger.getLogger(AbstractQuteDelegateCommandHandler.class.getName());

	/**
	 * Qute client commands
	 */
	private static final String DATA_MODEL_CHANGED_COMMAND = "qute/dataModelChanged";

	private static final IJavaDataModelChangedListener LISTENER = (event) -> {
		try {
			// Execute client command with a timeout of 5 seconds to avoid blocking jobs.
			JavaLanguageServerPlugin.getInstance().getClientConnection()
					.executeClientCommand(Duration.of(5, ChronoUnit.SECONDS), DATA_MODEL_CHANGED_COMMAND, event);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,
					"Error while sending '" + DATA_MODEL_CHANGED_COMMAND + "' event to the client", e);
		}
	};

	private static boolean initialized;

	public AbstractQuteDelegateCommandHandler() {
		initialize();
	}

	/**
	 * Add MicroProfile properties changed listener if needed.
	 */
	private static synchronized void initialize() {
		if (initialized) {
			return;
		}
		// Add a classpath changed listener to execute client command
		// "qute/javaDataModelChanged"
		JavaDataModelListenerManager.getInstance().addJavaDataModelChangedListener(LISTENER);
		initialized = true;
	}
}
