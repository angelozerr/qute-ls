/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
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

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.ls.QuteTextDocument;
import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.ParameterDeclaration;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.settings.QuteValidationSettings;
import com.redhat.qute.utils.QutePositionUtility;
import com.redhat.qute.utils.StringUtils;

import io.quarkus.qute.Engine;
import io.quarkus.qute.TemplateException;

/**
 * Qute diagnostics support.
 *
 */
class QuteDiagnostics {

	private final JavaDataModelCache javaCache;

	public QuteDiagnostics(JavaDataModelCache javaCache) {
		this.javaCache = javaCache;
	}

	/**
	 * Validate the given Qute <code>template</code>.
	 * 
	 * @param template           the Qute template.
	 * @param document
	 * @param validationSettings the validation settings.
	 * @param cancelChecker      the cancel checker.
	 * @return the result of the validation.
	 */
	public List<Diagnostic> doDiagnostics(Template template, TextDocument document,
			QuteValidationSettings validationSettings, CancelChecker cancelChecker) {
		if (validationSettings == null) {
			validationSettings = QuteValidationSettings.DEFAULT;
		}
		List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
		if (validationSettings.isEnabled()) {
			// validate(template, diagnostics);
			validate2(template, diagnostics);
		}
		return diagnostics;
	}

	private void validate2(Template template, List<Diagnostic> diagnostics) {
		Engine engine = Engine.builder().addDefaults().build();
		String templateContent = template.getText();
		try {
			engine.parse(templateContent);
		} catch (TemplateException e) {
			String message = e.getMessage();
			int line = e.getOrigin().getLine() - 1;
			Position start = new Position(line, e.getOrigin().getLineCharacterStart() - 1);
			Position end = new Position(line, e.getOrigin().getLineCharacterEnd() - 1);
			Range range = new Range(start, end);
			Diagnostic diagnostic = new Diagnostic(range, message, DiagnosticSeverity.Error, "qute", null);
			diagnostics.add(diagnostic);
		}
	}

	private static void validate(Node parent, List<Diagnostic> diagnostics) {
		if (!parent.isClosed()) {
			Range range = QutePositionUtility.createRange(parent);
			String message = parent.getKind() + parent.getNodeName() + " is not closed";
			Diagnostic diagnostic = new Diagnostic(range, message, DiagnosticSeverity.Error, "qute", null);
			diagnostics.add(diagnostic);
		}
		for (Node child : parent.getChildren()) {
			validate(child, diagnostics);
		}
	}

	public CompletableFuture<List<Diagnostic>> doDiagnostics2(Template template, QuteTextDocument document,
			QuteValidationSettings validationSettings, CancelChecker cancelChecker) {
		List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
		String projectUri = template.getProjectUri();
		if (projectUri != null) {			
			List<Node> children = template.getChildren();
			for (Node node : children) {
				if (node.getKind() == NodeKind.ParameterDeclaration) {
					ParameterDeclaration parameter = (ParameterDeclaration) node;
					String className = parameter.getClassName();
					if (StringUtils.isEmpty(className)) {
						Range range = QutePositionUtility.createRange(parameter);
						String message = "Class must be defined";
						Diagnostic diagnostic = new Diagnostic(range, message, DiagnosticSeverity.Error, "qute", null);
						diagnostics.add(diagnostic);
					} else {
						// javaCache.getResolvedClass(null, 0, projectUri);
					}
				}
			}
		}
		return CompletableFuture.completedFuture(diagnostics);
	}

}
