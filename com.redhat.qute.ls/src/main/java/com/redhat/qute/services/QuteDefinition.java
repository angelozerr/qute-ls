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

import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.ParameterDeclaration;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.utils.QutePositionUtility;

/**
 * Qute definition support.
 *
 */
class QuteDefinition {

	private static final Logger LOGGER = Logger.getLogger(QuteDefinition.class.getName());

	private final JavaDataModelCache javaCache;

	public QuteDefinition(JavaDataModelCache javaCache) {
		this.javaCache = javaCache;
	}

	public CompletableFuture<List<? extends LocationLink>> findDefinition(Template template, Position position,
			CancelChecker cancelChecker) {
		try {
			int offset = template.offsetAt(position);
			Node node = template.findNodeAt(offset);
			if (node == null) {
				return CompletableFuture.completedFuture(Collections.emptyList());
			}
			switch (node.getKind()) {
			case Section:
				// Start end tag definition
				return findDefinitionFromSection(offset, (Section) node, template);
			case ParameterDeclaration:
				// Return Java class definition
				return findDefinitionFromParameterDeclaration(offset, (ParameterDeclaration) node, template);
			case Expression:
				return findDefinitionFromExpression(offset, (Expression) node, template);
			default:
				// do nothing
			}
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In QuteDefinition the client provided Position is at a BadLocation", e);
		}
		return CompletableFuture.completedFuture(Collections.emptyList());

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
	private static CompletableFuture<List<? extends LocationLink>> findDefinitionFromSection(int offset,
			Section sectionTag, Template template) throws BadLocationException {
		List<LocationLink> locations = new ArrayList<>();
		Range originRange = null;
		Range targetRange = null;
		if (sectionTag.isInStartTagName(offset)) {
			originRange = QutePositionUtility.selectStartTagName(sectionTag);
			targetRange = QutePositionUtility.selectEndTagName(sectionTag);
		} else if (sectionTag.isInEndTag(offset)) {
			originRange = QutePositionUtility.selectEndTagName(sectionTag);
			targetRange = QutePositionUtility.selectStartTagName(sectionTag);
		}
		if (originRange != null && targetRange != null) {
			locations.add(new LocationLink(template.getUri(), targetRange, targetRange, originRange));
		}
		return CompletableFuture.completedFuture(locations);
	}

	private CompletableFuture<List<? extends LocationLink>> findDefinitionFromParameterDeclaration(int offset,
			ParameterDeclaration parameterDeclaration, Template template) {
		String projectUri = template.getProjectUri();
		if (projectUri != null && parameterDeclaration.isInClassName(offset)) {
			String className = parameterDeclaration.getClassName();
			QuteJavaDefinitionParams params = new QuteJavaDefinitionParams(className, projectUri);
			return findJavaDefinition(params,
					() -> QutePositionUtility.createRange(parameterDeclaration.getClassNameStart(),
							parameterDeclaration.getClassNameEnd(), template));
		}
		return CompletableFuture.completedFuture(Collections.emptyList());
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
			switch (part.getPartKind()) {
			case Object:
				ParameterDeclaration parameter = template.findParameterByAlias(part.getPartName());
				if (parameter != null) {
					String targetUri = template.getUri();
					Range targetRange = QutePositionUtility.selectAlias(parameter);
					Range originSelectionRange = QutePositionUtility.createRange(part);
					LocationLink locationLink = new LocationLink(targetUri, targetRange, targetRange,
							originSelectionRange);
					return CompletableFuture.completedFuture(Arrays.asList(locationLink));
				}
				break;
			case Property:
			case Method:
				Parts parts = part.getParent();
				int partIndex = parts.getPreviousPartIndex(part);
				String projectUri = template.getProjectUri();
				if (projectUri != null) {
					return javaCache.getResolvedClass(parts, partIndex, projectUri) //
							.thenCompose(resolvedClass -> {
								if (resolvedClass != null) {
									String property = part.getPartName();
									QuteJavaDefinitionParams params = new QuteJavaDefinitionParams(
											resolvedClass.getClassName(), projectUri);
									// params.setMethod(member.getMethod());
									params.setField(property);
									return findJavaDefinition(params, () -> QutePositionUtility.createRange(part));
								}
								return CompletableFuture.completedFuture(Collections.emptyList());
							});
				}
			default:
			}

		}
		return CompletableFuture.completedFuture(Collections.emptyList());
	}

}
