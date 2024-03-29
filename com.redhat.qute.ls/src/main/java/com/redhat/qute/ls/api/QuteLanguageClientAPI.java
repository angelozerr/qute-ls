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

import org.eclipse.lsp4j.services.LanguageClient;

/**
 * Qute language client API.
 * 
 * @author Angelo ZERR
 *
 */
public interface QuteLanguageClientAPI extends LanguageClient, QuteJavaClassesProvider, QuteResolvedJavaClassProvider,
		QuteJavaDefinitionProvider, QuteProjectInfoProvider, QuteProjectDataModelProvider, QuteJavaCodeLensProvider {
}