/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.ls.api;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.services.LanguageServer;

import com.redhat.qute.commons.datamodel.JavaDataModelChangeEvent;

/**
 * Qute language server API.
 * 
 * @author Angelo ZERR
 *
 */
public interface QuteLanguageServerAPI extends LanguageServer {

	/**
	 * Notification for Qute data model changed which occurs when:
	 *
	 * <ul>
	 * <li>classpath (java sources and dependencies) changed</li>
	 * <li>only java sources changed</li>
	 * </ul>
	 *
	 * @param event the Qute data model change event which gives the information if
	 *              changed comes from classpath or java sources.
	 */
	@JsonNotification("qute/dataModelChanged")
	void dataModelChanged(JavaDataModelChangeEvent event);
	
	// CompletableFuture<String> generateTemplate(dataModel);
}
