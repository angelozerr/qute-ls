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

import com.redhat.qute.commons.JavaMemberInfo;
import com.redhat.qute.commons.ResolvedJavaClassInfo;
import com.redhat.qute.parser.expression.ObjectPart;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.parser.expression.Parts.PartKind;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.JavaTypeInfoProvider;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.ParameterDeclaration;
import com.redhat.qute.parser.template.RangeOffset;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.services.diagnostics.IQuteErrorCode;
import com.redhat.qute.services.diagnostics.QuteErrorCode;
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

	private static final String UNDEFINED_VARIABLE_MESSAGE = "`{0}` cannot be resolved to a variable.";

	private static final String UNKWOWN_PROPERTY_MESSAGE = "`{0}` cannot be resolved or is not a field for `{1}` Java type.";

	private static final String UNKWOWN_METHOD_MESSAGE = "`{0}` cannot be resolved or is not a method for `{1}` Java type.";

	private static final String RESOLVING_JAVA_TYPE_MESSAGE = "Resolving Java type `{0}`.";

	private static final String UNKWOWN_JAVA_TYPE_MESSAGE = "`{0}` cannot be resolved to a type.";

	private static final String ITERABLE_REQUIRED_MESSAGE = "`{0}` is not an instance of `java.lang.Iterable`.";

	private static final ResolvedJavaClassInfo NOW = new ResolvedJavaClassInfo();

	private final JavaDataModelCache javaCache;

	public QuteDiagnostics(JavaDataModelCache javaCache) {
		this.javaCache = javaCache;
	}

	/**
	 * Validate the given Qute <code>template</code>.
	 * 
	 * @param template           the Qute template.
	 * @param validationSettings the validation settings.
	 * @param cancelChecker      the cancel checker.
	 * @return the result of the validation.
	 */
	public List<Diagnostic> doDiagnostics(Template template, QuteValidationSettings validationSettings,
			List<CompletableFuture<ResolvedJavaClassInfo>> resolvingJavaTypeFutures, CancelChecker cancelChecker) {
		if (validationSettings == null) {
			validationSettings = QuteValidationSettings.DEFAULT;
		}
		List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
		if (validationSettings.isEnabled()) {
			validateWithRealQuteParser(template, diagnostics);
			validateDataModel(template, template, resolvingJavaTypeFutures, diagnostics);
		}
		return diagnostics;
	}

	private void validateWithRealQuteParser(Template template, List<Diagnostic> diagnostics) {
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
			Diagnostic diagnostic = createDiagnostic(range, message, DiagnosticSeverity.Error, null);
			diagnostics.add(diagnostic);
		}
	}

	private static void validate(Node parent, List<Diagnostic> diagnostics) {
		if (!parent.isClosed()) {
			Range range = QutePositionUtility.createRange(parent);
			String message = parent.getKind() + parent.getNodeName() + " is not closed";
			Diagnostic diagnostic = createDiagnostic(range, message, DiagnosticSeverity.Error, null);
			diagnostics.add(diagnostic);
		}
		for (Node child : parent.getChildren()) {
			validate(child, diagnostics);
		}
	}

	private List<CompletableFuture<ResolvedJavaClassInfo>> validateDataModel(Node parent, Template template,
			List<CompletableFuture<ResolvedJavaClassInfo>> resolvingJavaTypeFutures, List<Diagnostic> diagnostics) {
		List<Node> children = parent.getChildren();
		for (Node node : children) {
			switch (node.getKind()) {
			case ParameterDeclaration: {
				ParameterDeclaration parameter = (ParameterDeclaration) node;
				String javaTypeToResolve = parameter.getClassName();
				if (StringUtils.isEmpty(javaTypeToResolve)) {
					Range range = QutePositionUtility.createRange(parameter);
					String message = "Class must be defined";
					Diagnostic diagnostic = createDiagnostic(range, message, DiagnosticSeverity.Error, null);
					diagnostics.add(diagnostic);
				} else {
					String projectUri = template.getProjectUri();
					if (projectUri != null) {
						List<RangeOffset> classNameRanges = parameter.getClassNameRanges();
						for (RangeOffset classNameRange : classNameRanges) {
							String className = template.getText(classNameRange);
							CompletableFuture<ResolvedJavaClassInfo> resolvingJavaTypeFuture = javaCache
									.resolveJavaType(className, projectUri);
							ResolvedJavaClassInfo resolvedJavaClass = resolvingJavaTypeFuture.getNow(NOW);
							if (NOW.equals(resolvedJavaClass)) {
								// Java type must be loaded.
								Range range = QutePositionUtility.createRange(classNameRange, template);
								String message = MessageFormat.format(RESOLVING_JAVA_TYPE_MESSAGE, className);
								Diagnostic diagnostic = createDiagnostic(range, message, DiagnosticSeverity.Information,
										null);
								diagnostics.add(diagnostic);
								resolvingJavaTypeFutures.add(resolvingJavaTypeFuture);
							}

							if (resolvedJavaClass == null) {
								// Java type doesn't exist
								Range range = QutePositionUtility.createRange(classNameRange, template);
								String message = MessageFormat.format(UNKWOWN_JAVA_TYPE_MESSAGE, className);
								Diagnostic diagnostic = createDiagnostic(range, message, DiagnosticSeverity.Error,
										QuteErrorCode.UnkwownType);
								diagnostics.add(diagnostic);
							}
						}
					}
				}
				break;
			}
			case Section: {
				Section section = (Section) node;
				List<Parameter> parameters = section.getParameters();
				for (Parameter parameter : parameters) {
					Expression expression = parameter.getExpression();
					if (expression != null) {
						validateExpression(expression, section, template, resolvingJavaTypeFutures, diagnostics);
					}
				}
				break;
			}
			case Expression: {
				validateExpression((Expression) node, null, template, resolvingJavaTypeFutures, diagnostics);
				break;
			}
			default:
			}
			validateDataModel(node, template, resolvingJavaTypeFutures, diagnostics);
		}
		return resolvingJavaTypeFutures;
	}

	private void validateExpression(Expression expression, Section ownerSection, Template template,
			List<CompletableFuture<ResolvedJavaClassInfo>> resolvingJavaTypeFutures, List<Diagnostic> diagnostics) {
		String projectUri = template.getProjectUri();
		List<Node> expressionChildren = expression.getExpressionContent();
		for (Node expressionChild : expressionChildren) {
			if (expressionChild.getKind() == NodeKind.ExpressionParts) {
				Parts parts = (Parts) expressionChild;
				validateExpressionParts(parts, ownerSection, projectUri, resolvingJavaTypeFutures, diagnostics);
			}
		}
	}

	private void validateExpressionParts(Parts parts, Section ownerSection, String projectUri,
			List<CompletableFuture<ResolvedJavaClassInfo>> resolvingJavaTypeFutures, List<Diagnostic> diagnostics) {
		ResolvedJavaClassInfo resolvedJavaClass = null;
		for (int i = 0; i < parts.getChildCount(); i++) {
			Part current = ((Part) parts.getChild(i));
			switch (current.getPartKind()) {

			case Object: {
				ObjectPart objectPart = (ObjectPart) current;
				resolvedJavaClass = validateObjectPart(objectPart, ownerSection, projectUri, diagnostics,
						resolvingJavaTypeFutures);
				if (resolvedJavaClass == null) {
					// The Java type of the object part cannot be resolved, stop the validation of
					// property, method.
					return;
				}
				break;
			}

			case Method:
			case Property: {
				resolvedJavaClass = validatePropertyPart(current, ownerSection, projectUri, resolvedJavaClass,
						diagnostics, resolvingJavaTypeFutures);
				if (resolvedJavaClass == null) {
					// The Java type of the previous part cannot be resolved, stop the validation of
					// followings property, method.
					return;
				}
				break;
			}

			default:
			}
		}
	}

	private ResolvedJavaClassInfo validateObjectPart(ObjectPart objectPart, Section ownerSection, String projectUri,
			List<Diagnostic> diagnostics, List<CompletableFuture<ResolvedJavaClassInfo>> resolvingJavaTypeFutures) {
		JavaTypeInfoProvider javaTypeInfo = objectPart.resolveJavaType();
		if (javaTypeInfo == null) {
			// ex : {item}
			Range range = QutePositionUtility.createRange(objectPart);
			String message = MessageFormat.format(UNDEFINED_VARIABLE_MESSAGE, objectPart.getPartName());
			Diagnostic diagnostic = createDiagnostic(range, message, DiagnosticSeverity.Warning,
					QuteErrorCode.UndefinedVariable);
			diagnostics.add(diagnostic);
			return null;
		}

		String javaTypeToResolve = javaTypeInfo.getClassName();
		return validateJavaTypePart(objectPart, ownerSection, projectUri, diagnostics, resolvingJavaTypeFutures,
				javaTypeToResolve);
	}

	private ResolvedJavaClassInfo validatePropertyPart(Part part, Section ownerSection, String projectUri,
			ResolvedJavaClassInfo resolvedJavaClass, List<Diagnostic> diagnostics,
			List<CompletableFuture<ResolvedJavaClassInfo>> resolvingJavaTypeFutures) {
		String property = part.getPartName();
		JavaMemberInfo javaMember = resolvedJavaClass.findMember(property);
		if (javaMember == null) {
			IQuteErrorCode errorCode = null;
			String message = null;
			boolean isMethod = part.getPartKind() == PartKind.Method;
			if (isMethod) {
				// ex : {@org.acme.Item item}
				// "{item.getXXXX()}
				message = MessageFormat.format(UNKWOWN_METHOD_MESSAGE, property, resolvedJavaClass.getClassName());
				errorCode = QuteErrorCode.UnkwownMethod;
			} else {
				// ex : {@org.acme.Item item}
				// "{item.XXXX}
				message = MessageFormat.format(UNKWOWN_PROPERTY_MESSAGE, property, resolvedJavaClass.getClassName());
				errorCode = QuteErrorCode.UnkwownProperty;
			}
			Range range = QutePositionUtility.createRange(part);
			Diagnostic diagnostic = createDiagnostic(range, message, DiagnosticSeverity.Error, errorCode);
			diagnostics.add(diagnostic);
			return null;
		}
		Parts parts = part.getParent();
		boolean last = parts.getPartIndex(part) == parts.getChildCount() - 1;
		if (last) {
			return null;
		}
		return validateJavaTypePart(part, ownerSection, projectUri, diagnostics, resolvingJavaTypeFutures,
				javaMember.getMemberType());
	}

	private ResolvedJavaClassInfo validateJavaTypePart(Part part, Section ownerSection, String projectUri,
			List<Diagnostic> diagnostics, List<CompletableFuture<ResolvedJavaClassInfo>> resolvingJavaTypeFutures,
			String javaTypeToResolve) {
		if (StringUtils.isEmpty(javaTypeToResolve)) {
			Range range = QutePositionUtility.createRange(part);
			String message = MessageFormat.format(UNKWOWN_JAVA_TYPE_MESSAGE, part.getPartName());
			Diagnostic diagnostic = createDiagnostic(range, message, DiagnosticSeverity.Error,
					QuteErrorCode.UnkwownType);
			diagnostics.add(diagnostic);
			return null;
		}

		if (projectUri == null) {
			return null;
		}

		CompletableFuture<ResolvedJavaClassInfo> resolvingJavaTypeFuture = javaCache.resolveJavaType(part, projectUri,
				false);
		ResolvedJavaClassInfo resolvedJavaClass = resolvingJavaTypeFuture.getNow(NOW);
		if (NOW.equals(resolvedJavaClass)) {
			// Java type must be loaded.
			Range range = QutePositionUtility.createRange(part);
			String message = MessageFormat.format(RESOLVING_JAVA_TYPE_MESSAGE, javaTypeToResolve);
			Diagnostic diagnostic = createDiagnostic(range, message, DiagnosticSeverity.Information, null);
			diagnostics.add(diagnostic);
			resolvingJavaTypeFutures.add(resolvingJavaTypeFuture);
			return null;
		}

		if (resolvedJavaClass == null) {
			// Java type doesn't exist
			Range range = QutePositionUtility.createRange(part);
			String message = MessageFormat.format(UNKWOWN_JAVA_TYPE_MESSAGE, javaTypeToResolve);
			Diagnostic diagnostic = createDiagnostic(range, message, DiagnosticSeverity.Error,
					QuteErrorCode.UnkwownType);
			diagnostics.add(diagnostic);
			return null;
		}

		if (ownerSection != null && ownerSection.isIterable()) {
			// The expression is declared inside an iterable section like #for, #each.
			// Ex: {#for item in items}
			if (!resolvedJavaClass.isIterable()) {
				// The Java class is not an iterable class like java.util.List
				Range range = QutePositionUtility.createRange(part);
				String message = MessageFormat.format(ITERABLE_REQUIRED_MESSAGE, javaTypeToResolve);
				Diagnostic diagnostic = createDiagnostic(range, message, DiagnosticSeverity.Error,
						QuteErrorCode.NotInstanceOfIterable);
				diagnostics.add(diagnostic);
				return null;
			}
		}
		return resolvedJavaClass;
	}

	private static Diagnostic createDiagnostic(Range range, String message, DiagnosticSeverity severity,
			IQuteErrorCode errorCode) {
		return new Diagnostic(range, message, severity, QUTE_SOURCE, errorCode != null ? errorCode.getCode() : null);
	}

}
