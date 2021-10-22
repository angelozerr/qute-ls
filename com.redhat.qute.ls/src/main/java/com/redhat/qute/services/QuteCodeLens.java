/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.commons.datamodel.ParameterDataModel;
import com.redhat.qute.commons.datamodel.TemplateDataModel;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.services.datamodel.ExtendedParameterDataModel;
import com.redhat.qute.services.datamodel.JavaDataModelCache;
import com.redhat.qute.settings.QuteCodeLensSettings;

/**
 * Qute code lens support.
 * 
 * @author Angelo ZERR
 *
 */
class QuteCodeLens {

	private final JavaDataModelCache javaCache;

	public QuteCodeLens(JavaDataModelCache javaCache) {
		this.javaCache = javaCache;
	}

	public CompletableFuture<List<? extends CodeLens>> getCodelens(Template template, QuteCodeLensSettings settings,
			CancelChecker cancelChecker) {
		return javaCache.getTemplateDataModel(template) //
				.thenApply(dataModel -> {
					if (dataModel == null || dataModel.getSourceType() == null) {
						return Collections.emptyList();
					}
					cancelChecker.checkCanceled();
					List<CodeLens> lenses = new ArrayList<>();

					// Method which is bind with the template
					String title = createCheckedTemplateTitle(dataModel);
					Range range = new Range(new Position(0, 0), new Position(0, 0));
					Command command = new Command(title, "");
					CodeLens codeLens = new CodeLens(range, command, null);
					lenses.add(codeLens);

					// Parameters of the template
					List<ExtendedParameterDataModel> parameters = dataModel.getParameters();
					if (parameters != null) {
						for (ParameterDataModel parameter : parameters) {
							String parameterTitle = parameter.getKey() + " : " + parameter.getSourceType();
							Command parameterCommand = new Command(parameterTitle, "");
							CodeLens parameterCodeLens = new CodeLens(range, parameterCommand, null);
							lenses.add(parameterCodeLens);
						}
					}
					return lenses;
				});
	}

	private String createCheckedTemplateTitle(TemplateDataModel<?> dataModel) {
		String className = dataModel.getSourceType();
		int index = className.lastIndexOf('.');
		className = className.substring(index + 1, className.length());
		return new StringBuilder(className) //
				.append("#") //
				.append(dataModel.getSourceMethod()) //
				.toString();
	}

}
