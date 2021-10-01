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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.commons.JavaClassMemberInfo;
import com.redhat.qute.commons.ResolvedJavaClassInfo;
import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.expression.ObjectPart;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.parser.expression.PropertyPart;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.JavaTypeInfoProvider;
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

	private static final String QUTE_SOURCE = "qute";

	private static final String UNDEFINED_PROPERTY_MESSAGE = "`{0}` cannot be resolved to a variable.";

	private static final String UNKWOWN_PROPERTY_MESSAGE = "`{0}` cannot be resolved or is not a field for `{1}` Java type.";

	private static final String RESOLVING_JAVA_TYPE_MESSAGE = "Resolving Java type `{0}`.";

	private static final String UNKWOWN_JAVA_TYPE_MESSAGE = "`{0}` cannot be resolved to a type.";

	private static final ResolvedJavaClassInfo NOW = new ResolvedJavaClassInfo();

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
			QuteValidationSettings validationSettings,
			List<CompletableFuture<ResolvedJavaClassInfo>> resolvingJavaTypeFutures, CancelChecker cancelChecker) {
		if (validationSettings == null) {
			validationSettings = QuteValidationSettings.DEFAULT;
		}
		List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
		if (validationSettings.isEnabled()) {
			// validate(template, diagnostics);
			validate2(template, diagnostics);
			validateDataModel(template, template, resolvingJavaTypeFutures, diagnostics);
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
			Diagnostic diagnostic = new Diagnostic(range, message, DiagnosticSeverity.Error, QUTE_SOURCE, null);
			diagnostics.add(diagnostic);
		}
	}

	private static void validate(Node parent, List<Diagnostic> diagnostics) {
		if (!parent.isClosed()) {
			Range range = QutePositionUtility.createRange(parent);
			String message = parent.getKind() + parent.getNodeName() + " is not closed";
			Diagnostic diagnostic = new Diagnostic(range, message, DiagnosticSeverity.Error, QUTE_SOURCE, null);
			diagnostics.add(diagnostic);
		}
		for (Node child : parent.getChildren()) {
			validate(child, diagnostics);
		}
	}

	/*
	 * public CompletableFuture<List<Diagnostic>> doDiagnostics2(Template template,
	 * QuteTextDocument document, QuteValidationSettings validationSettings,
	 * List<CompletableFuture<ResolvedJavaClassInfo>> resolvingJavaTypeFutures,
	 * CancelChecker cancelChecker) { List<Diagnostic> diagnostics = new
	 * ArrayList<Diagnostic>(); String projectUri = template.getProjectUri(); if
	 * (projectUri != null) { Node parent = template; validateDataModel(parent,
	 * template, resolvingJavaTypeFutures, diagnostics); } return
	 * CompletableFuture.completedFuture(diagnostics); }
	 */

	private List<CompletableFuture<ResolvedJavaClassInfo>> validateDataModel(Node parent, Template template,
			List<CompletableFuture<ResolvedJavaClassInfo>> resolvingJavaTypeFutures, List<Diagnostic> diagnostics) {
		List<Node> children = parent.getChildren();
		for (Node node : children) {
			switch (node.getKind()) {
			case ParameterDeclaration: {
				ParameterDeclaration parameter = (ParameterDeclaration) node;
				String className = parameter.getClassName();
				if (StringUtils.isEmpty(className)) {
					Range range = QutePositionUtility.createRange(parameter);
					String message = "Class must be defined";
					Diagnostic diagnostic = new Diagnostic(range, message, DiagnosticSeverity.Error, QUTE_SOURCE, null);
					diagnostics.add(diagnostic);
				} else {
					// javaCache.getResolvedJavaClass(className, projectUri)
					// javaCache.getResolvedClass(null, 0, projectUri);
				}
				break;
			}
			case Expression: {
				validateExpression((Expression) node, resolvingJavaTypeFutures, diagnostics, template);
				break;
			}
			default:
			}
			validateDataModel(node, template, resolvingJavaTypeFutures, diagnostics);
		}
		return resolvingJavaTypeFutures;
	}

	private void validateExpression(Expression expression,
			List<CompletableFuture<ResolvedJavaClassInfo>> resolvingJavaTypeFutures, List<Diagnostic> diagnostics,
			Template template) {
		String projectUri = template.getProjectUri();
		if (!StringUtils.isEmpty(projectUri)) {
			List<Node> expressionChildren = expression.getExpressionContent();
			for (Node expressionChild : expressionChildren) {
				if (expressionChild.getKind() == NodeKind.ExpressionParts) {
					Parts parts = (Parts) expressionChild;
					validateExpressionParts(projectUri, parts, resolvingJavaTypeFutures, diagnostics);
				}
			}

		}
	}

	private void validateExpressionParts(String projectUri, Parts parts,
			List<CompletableFuture<ResolvedJavaClassInfo>> resolvingJavaTypeFutures, List<Diagnostic> diagnostics) {
		ResolvedJavaClassInfo resolvedJavaClass = null;
		JavaClassMemberInfo previousMember = null;
		for (int i = 0; i < parts.getChildCount(); i++) {
			Part current = ((Part) parts.getChild(i));
			switch (current.getPartKind()) {

			case Object: {
				ObjectPart objectPart = (ObjectPart) current;
				resolvedJavaClass = validateObjectPart(objectPart, projectUri, diagnostics, resolvingJavaTypeFutures);
				if (resolvedJavaClass == null) {
					// The Java type of the object part cannot be resolved, stop the validation of
					// property, method.
					return;
				}
				break;
			}

			case Property: {
				PropertyPart propertyPart = (PropertyPart) current;
				if (previousMember != null) {
					String type = previousMember.getType();
					CompletableFuture<ResolvedJavaClassInfo> resolvingJavaTypeFuture = javaCache.resolveJavaType(type,
							projectUri);
					resolvedJavaClass = resolvingJavaTypeFuture.getNow(NOW);
					if (NOW.equals(resolvedJavaClass)) {
						// Java type must be loaded.
						Range range = QutePositionUtility.createRange(propertyPart);
						String message = MessageFormat.format(RESOLVING_JAVA_TYPE_MESSAGE, type);
						Diagnostic diagnostic = new Diagnostic(range, message, DiagnosticSeverity.Information,
								QUTE_SOURCE, null);
						diagnostics.add(diagnostic);
						resolvingJavaTypeFutures.add(resolvingJavaTypeFuture);
						return;
					}
					if (resolvedJavaClass == null) {
						// Java type doesn't exist
						Range range = QutePositionUtility.createRange(propertyPart);
						String message = MessageFormat.format(UNKWOWN_JAVA_TYPE_MESSAGE, type);
						Diagnostic diagnostic = new Diagnostic(range, message, DiagnosticSeverity.Error, QUTE_SOURCE,
								null);
						diagnostics.add(diagnostic);
						return;
					}
				}

				String property = current.getPartName();
				previousMember = resolvedJavaClass.findMember(property);
				if (previousMember == null) {
					Range range = QutePositionUtility.createRange(current);
					String message = MessageFormat.format(UNKWOWN_PROPERTY_MESSAGE, property,
							resolvedJavaClass.getClassName());
					Diagnostic diagnostic = new Diagnostic(range, message, DiagnosticSeverity.Error, QUTE_SOURCE, null);
					diagnostics.add(diagnostic);
					return;
				}
				break;
			}
			}
		}
	}

	private ResolvedJavaClassInfo validateObjectPart(ObjectPart objectPart, String projectUri,
			List<Diagnostic> diagnostics, List<CompletableFuture<ResolvedJavaClassInfo>> resolvingJavaTypeFutures) {
		JavaTypeInfoProvider javaTypeInfo = objectPart.resolveJavaType();
		if (javaTypeInfo == null) {
			Range range = QutePositionUtility.createRange(objectPart);
			String message = MessageFormat.format(UNDEFINED_PROPERTY_MESSAGE, objectPart.getPartName());
			Diagnostic diagnostic = new Diagnostic(range, message, DiagnosticSeverity.Warning, QUTE_SOURCE, null);
			diagnostics.add(diagnostic);
			return null;
		}

		String resolvingJavaType = javaTypeInfo.getClassName();
		if (StringUtils.isEmpty(resolvingJavaType)) {
			Range range = QutePositionUtility.createRange(objectPart);
			String message = "Cannot be resolved as type";
			Diagnostic diagnostic = new Diagnostic(range, message, DiagnosticSeverity.Warning, QUTE_SOURCE, null);
			diagnostics.add(diagnostic);
			return null;
		}

		CompletableFuture<ResolvedJavaClassInfo> resolvingJavaTypeFuture = javaCache.resolveJavaType(objectPart,
				projectUri);
		ResolvedJavaClassInfo resolvedJavaClass = resolvingJavaTypeFuture.getNow(NOW);
		if (NOW.equals(resolvedJavaClass)) {
			// Java type must be loaded.
			Range range = QutePositionUtility.createRange(objectPart);
			String message = MessageFormat.format(RESOLVING_JAVA_TYPE_MESSAGE, resolvingJavaType);
			Diagnostic diagnostic = new Diagnostic(range, message, DiagnosticSeverity.Information, QUTE_SOURCE, null);
			diagnostics.add(diagnostic);
			resolvingJavaTypeFutures.add(resolvingJavaTypeFuture);
			return null;
		}
		if (resolvedJavaClass == null) {
			// Java type doesn't exist
			Range range = QutePositionUtility.createRange(objectPart);
			String message = MessageFormat.format(UNKWOWN_JAVA_TYPE_MESSAGE, resolvingJavaType);
			Diagnostic diagnostic = new Diagnostic(range, message, DiagnosticSeverity.Error, QUTE_SOURCE, null);
			diagnostics.add(diagnostic);
			return null;
		}
		return resolvedJavaClass;
	}

}
