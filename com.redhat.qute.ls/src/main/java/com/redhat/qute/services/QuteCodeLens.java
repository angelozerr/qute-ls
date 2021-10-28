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
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.commons.datamodel.ParameterDataModel;
import com.redhat.qute.commons.datamodel.TemplateDataModel;
import com.redhat.qute.indexing.QuteProject;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionKind;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.services.datamodel.ExtendedParameterDataModel;
import com.redhat.qute.services.datamodel.JavaDataModelCache;
import com.redhat.qute.settings.QuteCodeLensSettings;
import com.redhat.qute.utils.QutePositionUtility;

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
					List<CodeLens> lenses = new ArrayList<>();

					// #insert code lens references
					QuteProject project = template.getProject();
					collectCodeLenses(template, template, project, lenses, cancelChecker);

					if (dataModel == null || dataModel.getSourceType() == null) {
						return lenses;
					}
					cancelChecker.checkCanceled();

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

	private void collectCodeLenses(Node parent, Template template, QuteProject project, List<CodeLens> lenses,
			CancelChecker cancelChecker) {
		cancelChecker.checkCanceled();
		if (parent.getKind() == NodeKind.Section) {
			Section section = (Section) parent;
			if (section.getSectionKind() == SectionKind.INSERT) {

				if (project != null) {
					Parameter parameter = section.getParameterAt(0);
					if (parameter != null) {
						String tag = parameter.getValue();
						// TODO : implement findNbreferencesOfInsertTag correctly
						int nbReferences = 0; // project.findNbreferencesOfInsertTag(template.getTemplateId(), tag);
						if (nbReferences > 0) {
							String title = nbReferences == 1 ? "1 reference" : nbReferences + " references";
							Range range = QutePositionUtility.createRange(parameter);
							Command command = new Command(title, "");
							CodeLens codeLens = new CodeLens(range, command, null);
							lenses.add(codeLens);
						}
					}
				}

			}

		}
		List<Node> children = parent.getChildren();
		for (Node node : children) {
			collectCodeLenses(node, template, project, lenses, cancelChecker);
		}
	}

}
