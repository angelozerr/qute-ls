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

import java.nio.file.Files;
import java.nio.file.Path;
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
import com.redhat.qute.parser.expression.NamespacePart;
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
import com.redhat.qute.parser.template.sections.IncludeSection;
import com.redhat.qute.services.datamodel.JavaDataModelCache;
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

	private static final String TEMPLATE_NOT_FOUND_MESSAGE = "Template not found: `{0}`.";

	private static final String TEMPLATE_NOT_DEFINED_MESSAGE = "Template id must be defined as parameter.";

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
			List<CompletableFuture<?>> resolvingJavaTypeFutures, CancelChecker cancelChecker) {
		if (validationSettings == null) {
			validationSettings = QuteValidationSettings.DEFAULT;
		}
		String projectUri = template.getProjectUri();
		if (projectUri != null) {
			CompletableFuture<?> f = template.getTemplateDataModel();
			if (!f.isDone()) {
				resolvingJavaTypeFutures.add(f);
			}
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

	private void validateDataModel(Node parent, Template template, List<CompletableFuture<?>> resolvingJavaTypeFutures,
			List<Diagnostic> diagnostics) {
		List<Node> children = parent.getChildren();
		for (Node node : children) {
			switch (node.getKind()) {
			case ParameterDeclaration: {
				ParameterDeclaration parameter = (ParameterDeclaration) node;
				String javaTypeToResolve = parameter.getJavaType();
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
					Expression expression = parameter.getJavaTypeExpression();
					if (expression != null) {
						validateExpression(expression, section, template, resolvingJavaTypeFutures, diagnostics);
					}
				}
				switch (section.getSectionKind()) {
				case INCLUDE:
					validateIncludeSection((IncludeSection) section, diagnostics);
					break;
				default:
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
	}

	/**
	 * Validate #include section.
	 * 
	 * @param includeSection the include section
	 * @param diagnostics    the diagnostics to fill.
	 */
	private static void validateIncludeSection(IncludeSection includeSection, List<Diagnostic> diagnostics) {
		Parameter includedTemplateId = includeSection.getParameterAt(0);
		if (includedTemplateId != null) {
			// include defines a template to include
			// ex : {#include base}
			Path templateFile = includeSection.getLinkedTemplateFile();
			if (templateFile == null || Files.notExists(templateFile)) {
				// It doesn't exists a file named base, base.qute.html, base.html, etc
				Range range = QutePositionUtility.createRange(includedTemplateId);
				String message = MessageFormat.format(TEMPLATE_NOT_FOUND_MESSAGE, includedTemplateId.getValue());
				Diagnostic diagnostic = createDiagnostic(range, message, DiagnosticSeverity.Error,
						QuteErrorCode.TemplateNotFound);
				diagnostics.add(diagnostic);
			}
		} else {
			// #include doesn't define a template id
			// ex: {#include}
			Range range = QutePositionUtility.selectStartTagName(includeSection);
			String message = TEMPLATE_NOT_DEFINED_MESSAGE;
			Diagnostic diagnostic = createDiagnostic(range, message, DiagnosticSeverity.Error,
					QuteErrorCode.TemplateNotDefined);
			diagnostics.add(diagnostic);
		}
	}

	private void validateExpression(Expression expression, Section ownerSection, Template template,
			List<CompletableFuture<?>> resolvingJavaTypeFutures, List<Diagnostic> diagnostics) {
		String literalJavaType = expression.getLiteralJavaType();
		if (literalJavaType != null) {
			// The expression is a literal:
			// - {'abcd'} : string literal
			// - {true} : boolean literal
			// - {null} : null literal
			// - {123} : integer literal

		} else {
			// The expression reference Java data model (ex : {item})
			String projectUri = template.getProjectUri();
			List<Node> expressionChildren = expression.getExpressionContent();
			for (Node expressionChild : expressionChildren) {
				if (expressionChild.getKind() == NodeKind.ExpressionParts) {
					Parts parts = (Parts) expressionChild;
					validateExpressionParts(parts, ownerSection, projectUri, resolvingJavaTypeFutures, diagnostics);
				}
			}
		}
	}

	private void validateExpressionParts(Parts parts, Section ownerSection, String projectUri,
			List<CompletableFuture<?>> resolvingJavaTypeFutures, List<Diagnostic> diagnostics) {
		ResolvedJavaClassInfo resolvedJavaClass = null;
		String namespace = null;
		for (int i = 0; i < parts.getChildCount(); i++) {
			Part current = ((Part) parts.getChild(i));
			switch (current.getPartKind()) {

			case Namespace: {
				NamespacePart namespacePart = (NamespacePart) current;
				namespace = namespacePart.getPartName();
				break;
			}

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
				if (resolvedJavaClass.isIterable()) {
					// Expression uses iterable type
					// {@java.util.List<org.acme.Item items>
					// {items.size()}
					// Property, method to validate must be done for iterable type (ex :
					// java.util.List>
					String iterableType = resolvedJavaClass.getIterableType();
					CompletableFuture<ResolvedJavaClassInfo> resolvingJavaTypeFuture = javaCache
							.resolveJavaType(iterableType, projectUri);
					resolvedJavaClass = resolvingJavaTypeFuture.getNow(NOW);
					if (NOW.equals(resolvedJavaClass)) {
						// Java type must be loaded.
						Range range = QutePositionUtility.createRange(current);
						String message = MessageFormat.format(RESOLVING_JAVA_TYPE_MESSAGE, iterableType);
						Diagnostic diagnostic = createDiagnostic(range, message, DiagnosticSeverity.Information, null);
						diagnostics.add(diagnostic);
						resolvingJavaTypeFutures.add(resolvingJavaTypeFuture);
						return;
					}
				}

				resolvedJavaClass = validatePropertyPart(current, ownerSection, projectUri, resolvedJavaClass,
						diagnostics, resolvingJavaTypeFutures);
				if (resolvedJavaClass == null) {
					// The Java type of the previous part cannot be resolved, stop the validation of
					// followings property, method.
					return;
				}
				break;
			}
			}
		}
	}

	private ResolvedJavaClassInfo validateObjectPart(ObjectPart objectPart, Section ownerSection, String projectUri,
			List<Diagnostic> diagnostics, List<CompletableFuture<?>> resolvingJavaTypeFutures) {
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

		String javaTypeToResolve = javaTypeInfo.getJavaType();
		if (javaTypeToResolve == null) {
			// case of (#for item as data.items) where data.items expression must be
			// evaluated
			Expression expression = javaTypeInfo.getJavaTypeExpression();
			if (expression != null) {
				String literalJavaType = expression.getLiteralJavaType();
				if (literalJavaType != null) {
					javaTypeToResolve = literalJavaType;
				} else {
					Part lastPart = expression.getLastPart();
					if (lastPart != null) {
						ResolvedJavaClassInfo alias = javaCache.resolveJavaType(lastPart, projectUri).getNow(null);
						if (alias != null) {
							javaTypeToResolve = alias.getClassName();
						}
					}
				}
			}
		}
		return validateJavaTypePart(objectPart, ownerSection, projectUri, diagnostics, resolvingJavaTypeFutures,
				javaTypeToResolve);
	}

	private ResolvedJavaClassInfo validatePropertyPart(Part part, Section ownerSection, String projectUri,
			ResolvedJavaClassInfo resolvedJavaClass, List<Diagnostic> diagnostics,
			List<CompletableFuture<?>> resolvingJavaTypeFutures) {
		String property = part.getPartName();
		JavaMemberInfo javaMember = javaCache.findMember(property, resolvedJavaClass, projectUri);
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
		if (!part.isLast() || ownerSection != null && ownerSection.isIterable()) {
			// Last part doesn't require to validate the type except if the part expression
			// is inside a loop section
			// to check if the type is an iterable type (ex : {#for item in
			// part.to.validate}
			return validateJavaTypePart(part, ownerSection, projectUri, diagnostics, resolvingJavaTypeFutures,
					javaMember.getMemberType());
		}
		return null;
	}

	private ResolvedJavaClassInfo validateJavaTypePart(Part part, Section ownerSection, String projectUri,
			List<Diagnostic> diagnostics, List<CompletableFuture<?>> resolvingJavaTypeFutures,
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

		CompletableFuture<ResolvedJavaClassInfo> resolvingJavaTypeFuture = null;
		if (part.getPartKind() == PartKind.Object) {
			// Object part case.
			// - if expression is included inside a loop section (#for, etc), we need to get
			// the iterable of. If javaTypeToResolve= 'java.util.List<org.acme.Item>',we
			// must get 'org.acme.Item'.
			// - otherwise resolve the given java type to resolve
			resolvingJavaTypeFuture = javaCache.resolveJavaType(part, projectUri, false);
		} else {
			// Other part kind (property, method), resolve the given java type to resolve
			resolvingJavaTypeFuture = javaCache.resolveJavaType(javaTypeToResolve, projectUri);
		}
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

		return validateIterable(part, ownerSection, resolvedJavaClass, javaTypeToResolve, diagnostics);
	}

	private ResolvedJavaClassInfo validateIterable(Part part, Section ownerSection,
			ResolvedJavaClassInfo resolvedJavaClass, String javaTypeToResolve, List<Diagnostic> diagnostics) {
		if (part.isLast() && ownerSection != null && ownerSection.isIterable()) {
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
