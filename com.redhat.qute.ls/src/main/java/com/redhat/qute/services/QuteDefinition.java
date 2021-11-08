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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.commons.JavaMemberInfo;
import com.redhat.qute.commons.JavaMemberInfo.JavaMemberKind;
import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.commons.ResolvedJavaClassInfo;
import com.redhat.qute.indexing.QuteIndex;
import com.redhat.qute.indexing.QuteProject;
import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.expression.MethodPart;
import com.redhat.qute.parser.expression.ObjectPart;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.parser.expression.PropertyPart;
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
import com.redhat.qute.services.datamodel.ExtendedParameterDataModel;
import com.redhat.qute.services.datamodel.ExtendedTemplateDataModel;
import com.redhat.qute.services.datamodel.JavaDataModelCache;
import com.redhat.qute.services.definition.DefinitionRequest;
import com.redhat.qute.utils.QutePositionUtility;

/**
 * Qute definition support.
 *
 */
class QuteDefinition {

	private static final Logger LOGGER = Logger.getLogger(QuteDefinition.class.getName());

	private static CompletableFuture<List<? extends LocationLink>> NO_DEFINITION = CompletableFuture
			.completedFuture(Collections.emptyList());

	private final JavaDataModelCache javaCache;

	public QuteDefinition(JavaDataModelCache javaCache) {
		this.javaCache = javaCache;
	}

	public CompletableFuture<List<? extends LocationLink>> findDefinition(Template template, Position position,
			CancelChecker cancelChecker) {
		try {
			DefinitionRequest definitionRequest = new DefinitionRequest(template, position);
			Node node = definitionRequest.getNode();
			if (node == null) {
				return NO_DEFINITION;
			}
			int offset = definitionRequest.getOffset();
			switch (node.getKind()) {
			case Section:
				// - Start end tag definition
				// - Java data model definition
				return findDefinitionFromSection(offset, (Section) node, template);
			case ParameterDeclaration:
				// Return Java class definition
				return findDefinitionFromParameterDeclaration(offset, (ParameterDeclaration) node, template);
			case Expression:
				return findDefinitionFromExpression(offset, (Expression) node, template);
			case ExpressionPart:
				Part part = (Part) node;
				return findDefinitionFromPart(part, template);
			default:
				// none definitions
				return NO_DEFINITION;
			}

		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Failed creating DefinitionRequest", e);
			return NO_DEFINITION;
		}

	}

	/**
	 * Find start end tag definition.
	 * 
	 * @param offset
	 * 
	 * @param document
	 * @param template
	 * 
	 * @param request  the definition request
	 * @return
	 * @throws BadLocationException
	 */
	private CompletableFuture<List<? extends LocationLink>> findDefinitionFromSection(int offset, Section sectionTag,
			Template template) throws BadLocationException {
		List<LocationLink> locations = new ArrayList<>();
		// Try start / end tag definition to jump from start|end tag to end|start tag
		if (!findDefinitionFromStartEndTagSection(offset, sectionTag, template, locations)) {
			// Try Java data model definition
			if (sectionTag.isInParameters(offset)) {
				Parameter parameter = sectionTag.getParameterAtOffset(offset);
				if (parameter != null) {
					Expression expression = parameter.getJavaTypeExpression();
					if (expression != null) {
						return findDefinitionFromExpression(offset, expression, template);
					}
				}
			}
		}
		return CompletableFuture.completedFuture(locations);
	}

	private static boolean findDefinitionFromStartEndTagSection(int offset, Section section, Template template,
			List<LocationLink> locations) {
		Range originRange = null;
		Range targetRange = null;
		if (section.isInStartTagName(offset)) {
			originRange = QutePositionUtility.selectStartTagName(section);

			// 1. Jump to custom tag declared in the the {#insert custom-tag of the included
			// Qute template (by using {#include base).
			if (section.getSectionKind() == SectionKind.CUSTOM) {
				QuteProject project = template.getProject();
				if (project != null) {
					Node parent = section.getParent();
					while (parent != null) {
						if (parent.getKind() == NodeKind.Section) {
							Section parentSection = (Section) parent;
							if (parentSection.getSectionKind() == SectionKind.INCLUDE) {
								IncludeSection includeSection = (IncludeSection) parentSection;
								List<QuteIndex> indexes = project
										.findInsertTagParameter(includeSection.getLinkedTemplateId(), section.getTag());
								if (indexes != null) {
									for (QuteIndex index : indexes) {
										String linkedTemplateUri = index.getTemplatePath().toUri().toString();
										Range linkedTargetRange = index.getRange();
										locations.add(new LocationLink(linkedTemplateUri, linkedTargetRange,
												linkedTargetRange, originRange));
									}
								}
							}
						}
						parent = parent.getParent();
					}
				}
			}

			// 2. Jump to end tag section
			targetRange = QutePositionUtility.selectEndTagName(section);
			locations.add(new LocationLink(template.getUri(), targetRange, targetRange, originRange));

			return true;
		} else if (section.isInEndTag(offset)) {
			// Jump to start tag section
			originRange = QutePositionUtility.selectEndTagName(section);
			targetRange = QutePositionUtility.selectStartTagName(section);
			locations.add(new LocationLink(template.getUri(), targetRange, targetRange, originRange));
			return true;
		}
		return false;
	}

	private CompletableFuture<List<? extends LocationLink>> findDefinitionFromParameterDeclaration(int offset,
			ParameterDeclaration parameterDeclaration, Template template) {
		String projectUri = template.getProjectUri();
		if (projectUri != null && parameterDeclaration.isInClassName(offset)) {
			RangeOffset range = parameterDeclaration.getClassNameRange(offset);
			if (range != null) {
				String className = template.getText(range);
				QuteJavaDefinitionParams params = new QuteJavaDefinitionParams(className, projectUri);
				return findJavaDefinition(params, () -> QutePositionUtility.createRange(range, template));
			}
		}
		return NO_DEFINITION;
	}

	private CompletableFuture<List<? extends LocationLink>> findJavaDefinition(QuteJavaDefinitionParams params,
			Supplier<Range> originSelectionRangeProvider) {
		return javaCache.getJavaDefinition(params) //
				.thenApply(location -> {
					if (location != null) {
						String targetUri = location.getUri();
						Range targetRange = location.getRange();
						Range originSelectionRange = originSelectionRangeProvider.get();
						LocationLink locationLink = new LocationLink(targetUri, targetRange, targetRange,
								originSelectionRange);
						return Arrays.asList(locationLink);
					}
					return Collections.emptyList();
				});
	}

	private CompletableFuture<List<? extends LocationLink>> findDefinitionFromExpression(int offset,
			Expression expression, Template template) {
		Node expressionNode = expression.findNodeExpressionAt(offset);
		if (expressionNode != null && expressionNode.getKind() == NodeKind.ExpressionPart) {
			Part part = (Part) expressionNode;
			return findDefinitionFromPart(part, template);

		}
		return NO_DEFINITION;
	}

	private CompletableFuture<List<? extends LocationLink>> findDefinitionFromPart(Part part, Template template) {
		switch (part.getPartKind()) {
		case Object:
			return findDefinitionFromObjectPart((ObjectPart) part, template);
		case Property:
			return findDefinitionFromPropertyPart((PropertyPart) part, template);
		case Method:
			return findDefinitionFromPropertyPart((MethodPart) part, template);
		default:
			return NO_DEFINITION;
		}
	}

	private CompletableFuture<List<? extends LocationLink>> findDefinitionFromObjectPart(Part part, Template template) {
		JavaTypeInfoProvider resolvedJavaType = ((ObjectPart) part).resolveJavaType();
		if (resolvedJavaType != null) {
			Node node = resolvedJavaType.getJavaTypeOwnerNode();
			if (node != null) {
				switch (node.getKind()) {
				case ParameterDeclaration: {
					ParameterDeclaration parameter = (ParameterDeclaration) node;
					String targetUri = template.getUri();
					Range targetRange = QutePositionUtility.selectAlias(parameter);
					Range originSelectionRange = QutePositionUtility.createRange(part);
					LocationLink locationLink = new LocationLink(targetUri, targetRange, targetRange,
							originSelectionRange);
					return CompletableFuture.completedFuture(Arrays.asList(locationLink));
				}
				case Parameter: {
					Parameter parameter = (Parameter) node;
					Section section = parameter.getOwnerSection();
					if (section != null && (section.getSectionKind() == SectionKind.FOR
							|| section.getSectionKind() == SectionKind.EACH)) {
						LoopSection loopSection = (LoopSection) section;
						parameter = loopSection.getAliasParameter();
					}
					if (parameter != null) {
						String targetUri = template.getUri();
						Range targetRange = QutePositionUtility.selectParameterName(parameter);
						Range originSelectionRange = QutePositionUtility.createRange(part);
						LocationLink locationLink = new LocationLink(targetUri, targetRange, targetRange,
								originSelectionRange);
						return CompletableFuture.completedFuture(Arrays.asList(locationLink));
					}
					return NO_DEFINITION;
				}
				default:
				}
			} else {
				if (resolvedJavaType instanceof ExtendedParameterDataModel) {
					String projectUri = template.getProjectUri();
					if (projectUri != null) {
						ExtendedParameterDataModel parameter = (ExtendedParameterDataModel) resolvedJavaType;
						ExtendedTemplateDataModel templateDataModel = parameter.getTemplate();
						String sourceType = templateDataModel.getSourceType();
						String sourceField = templateDataModel.getSourceField();
						String sourceMethod = templateDataModel.getSourceMethod();
						String sourceParameter = parameter.getKey();

						QuteJavaDefinitionParams params = new QuteJavaDefinitionParams(sourceType, projectUri);
						params.setField(sourceField);
						params.setMethod(sourceMethod);
						params.setMethodParameter(sourceParameter);
						return findJavaDefinition(params, () -> QutePositionUtility.createRange(part));
					}
				}
			}
		}
		return NO_DEFINITION;
	}

	private CompletableFuture<List<? extends LocationLink>> findDefinitionFromPropertyPart(Part part,
			Template template) {
		String projectUri = template.getProjectUri();
		if (projectUri != null) {
			Parts parts = part.getParent();
			Part previousPart = parts.getPreviousPart(part);
			return javaCache.resolveJavaType(previousPart, projectUri) //
					.thenCompose(previousResolvedType -> {
						if (previousResolvedType != null) {
							if (previousResolvedType.isIterable()) {
								// Expression uses iterable type
								// {@java.util.List<org.acme.Item items>
								// {items.si|ze()}
								// Property, method to find as definition must be done for iterable type (ex :
								// java.util.List>
								String iterableType = previousResolvedType.getIterableType();
								CompletableFuture<ResolvedJavaClassInfo> iterableResolvedTypeFuture = javaCache
										.resolveJavaType(iterableType, projectUri);
								return iterableResolvedTypeFuture.thenCompose((iterableResolvedType) -> {
									return findDefinitionFromPropertyPart(part, projectUri, iterableResolvedType);
								});
							}
							return findDefinitionFromPropertyPart(part, projectUri, previousResolvedType);
						}
						return NO_DEFINITION;
					});
		}
		return NO_DEFINITION;
	}

	private CompletableFuture<List<? extends LocationLink>> findDefinitionFromPropertyPart(Part part, String projectUri,
			ResolvedJavaClassInfo previousResolvedType) {
		// The Java class type from the previous part has been resolved, resolve the
		// property
		String property = part.getPartName();
		JavaMemberInfo member = javaCache.findMember(property, previousResolvedType, projectUri);
		if (member == null || member.getResolvedClass() == null) {
			return NO_DEFINITION;
		}
		QuteJavaDefinitionParams params = new QuteJavaDefinitionParams(member.getResolvedClass().getClassName(),
				projectUri);
		if (member != null && member.getKind() == JavaMemberKind.METHOD) {
			// Try to find a method definition
			params.setMethod(member.getName());
		} else {
			// Try to find a field definition
			params.setField(property);
		}
		return findJavaDefinition(params, () -> QutePositionUtility.createRange(part));
	}

}
