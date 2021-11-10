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

import static com.redhat.qute.services.diagnostics.QuteDiagnosticContants.QUTE_SOURCE;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

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
import com.redhat.qute.parser.template.SectionKind;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.IncludeSection;
import com.redhat.qute.parser.template.sections.LoopSection;
import com.redhat.qute.services.datamodel.JavaDataModelCache;
import com.redhat.qute.services.diagnostics.DiagnosticDataFactory;
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

	private static final ResolvedJavaClassInfo NOW = new ResolvedJavaClassInfo();

	private final JavaDataModelCache javaCache;

	private class ResolutionContext extends HashMap<String, ResolvedJavaClassInfo> {

		private final ResolutionContext parent;

		private ResolvedJavaClassInfo withObject;

		public ResolutionContext() {
			this(null);
		}

		public ResolutionContext(ResolutionContext parent) {
			this.parent = parent;
		}

		public ResolutionContext getParent() {
			return parent;
		}

		public void setWithObject(ResolvedJavaClassInfo withObject) {
			this.withObject = withObject;
		}

		public JavaMemberInfo findMemberWithObject(String property, String projectUri) {
			if (withObject != null) {
				if (withObject.isIterable() && projectUri != null) {
					String iterableType = withObject.getIterableType();
					ResolvedJavaClassInfo resolvedIterableType = javaCache.resolveJavaType(iterableType, projectUri)
							.getNow(null);
					if (resolvedIterableType != null) {
						JavaMemberInfo member = resolvedIterableType.findMember(property);
						if (member != null) {
							return member;
						}
					}
				} else {
					JavaMemberInfo member = withObject.findMember(property);
					if (member != null) {
						return member;
					}
				}
			}
			// Search in parent context
			ResolutionContext parent = this.parent;
			while (parent != null) {
				JavaMemberInfo member = parent.findMemberWithObject(property, projectUri);
				if (member != null) {
					return member;
				}
				parent = parent.getParent();
			}
			return null;
		}

	}

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
			validateDataModel(template, template, resolvingJavaTypeFutures, new ResolutionContext(), diagnostics);
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
			Diagnostic diagnostic = createDiagnostic(range, message);
			diagnostics.add(diagnostic);
		}
	}

	private Diagnostic createDiagnostic(Range range, String message) {
		return new Diagnostic(range, message, DiagnosticSeverity.Error, QUTE_SOURCE, null);
	}

	private void validateDataModel(Node parent, Template template, List<CompletableFuture<?>> resolvingJavaTypeFutures,
			ResolutionContext currentContext, List<Diagnostic> diagnostics) {
		ResolutionContext previousContext = currentContext;
		List<Node> children = parent.getChildren();
		for (Node node : children) {
			switch (node.getKind()) {
			case ParameterDeclaration: {
				ParameterDeclaration parameter = (ParameterDeclaration) node;
				String javaTypeToResolve = parameter.getJavaType();
				if (StringUtils.isEmpty(javaTypeToResolve)) {
					Range range = QutePositionUtility.createRange(parameter);
					String message = "Class must be defined";
					Diagnostic diagnostic = createDiagnostic(range, message);
					diagnostics.add(diagnostic);
				} else {
					String projectUri = template.getProjectUri();
					if (projectUri != null) {
						List<RangeOffset> classNameRanges = parameter.getClassNameRanges();
						for (RangeOffset classNameRange : classNameRanges) {
							String className = template.getText(classNameRange);
							ResolvedJavaClassInfo resolvedJavaClass = resolveJavaType(className, projectUri,
									() -> QutePositionUtility.createRange(classNameRange, template), diagnostics,
									resolvingJavaTypeFutures);
							if (resolvedJavaClass == null) {
								// Java type doesn't exist
								Range range = QutePositionUtility.createRange(classNameRange, template);
								Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error,
										QuteErrorCode.UnkwownType, className);
								diagnostics.add(diagnostic);
							} else {
								currentContext.put(javaTypeToResolve, resolvedJavaClass);
							}
						}
					}
				}
				break;
			}
			case Section: {
				Section section = (Section) node;
				if (canChangeContext(section)) {
					currentContext = new ResolutionContext(currentContext);
				}
				List<Parameter> parameters = section.getParameters();
				// validate expression parameters
				for (Parameter parameter : parameters) {
					Expression expression = parameter.getJavaTypeExpression();
					if (expression != null) {
						ResolvedJavaClassInfo result = validateExpression(expression, section, template,
								previousContext, resolvingJavaTypeFutures, diagnostics);
						switch (section.getSectionKind()) {
						case FOR:
						case EACH:
							String alias = ((LoopSection) section).getAlias();
							currentContext.put(alias, result);
							break;
						case WITH:
							currentContext.setWithObject(result);
							break;
						case LET:
						case SET:
							currentContext.put(parameter.getName(), result);
							break;
						default:
						}
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
				validateExpression((Expression) node, null, template, previousContext, resolvingJavaTypeFutures,
						diagnostics);
				break;
			}
			default:
			}
			validateDataModel(node, template, resolvingJavaTypeFutures, currentContext, diagnostics);
		}
	}

	private static boolean canChangeContext(Section section) {
		SectionKind sectionKind = section.getSectionKind();
		return sectionKind == SectionKind.EACH || sectionKind == SectionKind.FOR || sectionKind == SectionKind.LET
				|| sectionKind == SectionKind.SET || sectionKind == SectionKind.WITH;
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
				Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error,
						QuteErrorCode.TemplateNotFound, includedTemplateId.getValue());
				diagnostics.add(diagnostic);
			}
		} else {
			// #include doesn't define a template id
			// ex: {#include}
			Range range = QutePositionUtility.selectStartTagName(includeSection);
			Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error, QuteErrorCode.TemplateNotDefined);
			diagnostics.add(diagnostic);
		}
	}

	private ResolvedJavaClassInfo validateExpression(Expression expression, Section ownerSection, Template template,
			ResolutionContext resolutionContext, List<CompletableFuture<?>> resolvingJavaTypeFutures,
			List<Diagnostic> diagnostics) {
		String literalJavaType = expression.getLiteralJavaType();
		if (literalJavaType != null) {
			// The expression is a literal:
			// - {'abcd'} : string literal
			// - {true} : boolean literal
			// - {null} : null literal
			// - {123} : integer literal
			return javaCache.resolveJavaType(literalJavaType, literalJavaType).getNow(null);
		}
		// The expression reference Java data model (ex : {item})
		ResolvedJavaClassInfo resolvedJavaClass = null;
		String projectUri = template.getProjectUri();
		List<Node> expressionChildren = expression.getExpressionContent();
		for (Node expressionChild : expressionChildren) {
			if (expressionChild.getKind() == NodeKind.ExpressionParts) {
				Parts parts = (Parts) expressionChild;
				resolvedJavaClass = validateExpressionParts(parts, ownerSection, projectUri, resolutionContext,
						resolvingJavaTypeFutures, diagnostics);
			}
		}
		return resolvedJavaClass;
	}

	private ResolvedJavaClassInfo validateExpressionParts(Parts parts, Section ownerSection, String projectUri,
			ResolutionContext resolutionContext, List<CompletableFuture<?>> resolvingJavaTypeFutures,
			List<Diagnostic> diagnostics) {
		ResolvedJavaClassInfo resolvedJavaClass = null;
		String namespace = null;
		for (int i = 0; i < parts.getChildCount(); i++) {
			Part current = ((Part) parts.getChild(i));
			switch (current.getPartKind()) {

			case Namespace: {
				NamespacePart namespacePart = (NamespacePart) current;
				namespace = validateNamespace(namespacePart, projectUri, diagnostics);
				if (namespace == null) {
					// Invalid namespace
					return null;
				}
				break;
			}

			case Object: {
				ObjectPart objectPart = (ObjectPart) current;
				resolvedJavaClass = validateObjectPart(objectPart, ownerSection, projectUri, resolutionContext,
						diagnostics, resolvingJavaTypeFutures);
				if (resolvedJavaClass == null) {
					// The Java type of the object part cannot be resolved, stop the validation of
					// property, method.
					return null;
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
					// java.util.List
					String iterableType = resolvedJavaClass.getIterableType();
					resolvedJavaClass = resolveJavaType(iterableType, projectUri,
							() -> QutePositionUtility.createRange(current), diagnostics, resolvingJavaTypeFutures);
					if (resolvedJavaClass == null) {
						return null;
					}
				}

				resolvedJavaClass = validatePropertyPart(current, ownerSection, projectUri, resolvedJavaClass,
						diagnostics, resolvingJavaTypeFutures);
				if (resolvedJavaClass == null) {
					// The Java type of the previous part cannot be resolved, stop the validation of
					// followings property, method.
					return null;
				}
				break;
			}
			}
		}
		return resolvedJavaClass;
	}

	private ResolvedJavaClassInfo resolveJavaType(String javaType, String projectUri, Supplier<Range> rangeProvider,
			List<Diagnostic> diagnostics, List<CompletableFuture<?>> resolvingJavaTypeFutures) {
		CompletableFuture<ResolvedJavaClassInfo> resolvingJavaTypeFuture = javaCache.resolveJavaType(javaType,
				projectUri);
		ResolvedJavaClassInfo resolvedJavaClass = resolvingJavaTypeFuture.getNow(NOW);
		if (NOW.equals(resolvedJavaClass)) {
			// Java type must be loaded.
			Range range = rangeProvider.get();
			Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Information,
					QuteErrorCode.ResolvingJavaType, javaType);
			diagnostics.add(diagnostic);
			resolvingJavaTypeFutures.add(resolvingJavaTypeFuture);
			return null;
		}
		return resolvedJavaClass;
	}

	/**
	 * Validate namespace part.
	 * 
	 * @param namespacePart the namespace part to validate.
	 * @param projectUri    the project Uri.
	 * @param diagnostics   the diagnostics list to fill.
	 * @return the namespace of the part if it is an defined namespace and null
	 *         otherwise.
	 */
	private String validateNamespace(NamespacePart namespacePart, String projectUri, List<Diagnostic> diagnostics) {
		String namespace = namespacePart.getPartName();
		if ("data".equals(namespace)) {
			return namespace;
		}
		if (projectUri != null) {
			if (!javaCache.hasNamespace(namespace, projectUri)) {
				Range range = QutePositionUtility.createRange(namespacePart);
				Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Warning,
						QuteErrorCode.UndefinedNamespace, namespacePart.getPartName());
				diagnostics.add(diagnostic);
				return null;
			}
		}
		return namespace;
	}

	private ResolvedJavaClassInfo validateObjectPart(ObjectPart objectPart, Section ownerSection, String projectUri,
			ResolutionContext resolutionContext, List<Diagnostic> diagnostics,
			List<CompletableFuture<?>> resolvingJavaTypeFutures) {
		// Check if object part is a property coming from #with
		JavaMemberInfo javaMember = resolutionContext.findMemberWithObject(objectPart.getPartName(), projectUri);
		if (javaMember != null) {
			return resolveJavaType(javaMember.getMemberType(), projectUri,
					() -> QutePositionUtility.createRange(objectPart), diagnostics, resolvingJavaTypeFutures);
		}

		JavaTypeInfoProvider javaTypeInfo = objectPart.resolveJavaType();
		if (javaTypeInfo == null) {
			// ex : {item} --> undefined variable
			Range range = QutePositionUtility.createRange(objectPart);
			Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Warning, QuteErrorCode.UndefinedVariable,
					objectPart.getPartName());
			// Create data information helpful for code action
			diagnostic.setData(DiagnosticDataFactory.createUndefinedVariableData(objectPart.getPartName(),
					ownerSection != null && ownerSection.isIterable()));
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
			boolean isMethod = part.getPartKind() == PartKind.Method;
			if (isMethod) {
				// ex : {@org.acme.Item item}
				// "{item.getXXXX()}
				errorCode = QuteErrorCode.UnkwownMethod;
			} else {
				// ex : {@org.acme.Item item}
				// "{item.XXXX}
				errorCode = QuteErrorCode.UnkwownProperty;
			}
			Range range = QutePositionUtility.createRange(part);
			Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error, errorCode, property,
					resolvedJavaClass.getClassName());
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
			Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error, QuteErrorCode.UnkwownType,
					part.getPartName());
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
			Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Information,
					QuteErrorCode.ResolvingJavaType, javaTypeToResolve);
			diagnostics.add(diagnostic);
			resolvingJavaTypeFutures.add(resolvingJavaTypeFuture);
			return null;
		}

		if (resolvedJavaClass == null) {
			// Java type doesn't exist
			Range range = QutePositionUtility.createRange(part);
			Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error, QuteErrorCode.UnkwownType,
					javaTypeToResolve);
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
				Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error,
						QuteErrorCode.NotInstanceOfIterable, javaTypeToResolve);
				diagnostics.add(diagnostic);
				return null;
			}
		}
		return resolvedJavaClass;
	}

	private static Diagnostic createDiagnostic(Range range, DiagnosticSeverity severity, IQuteErrorCode errorCode,
			Object... arguments) {
		String message = errorCode.getMessage(arguments);
		return new Diagnostic(range, message, severity, QUTE_SOURCE, errorCode.getCode());
	}

}
