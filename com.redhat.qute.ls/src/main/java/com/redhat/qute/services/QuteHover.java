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

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.ParameterDeclaration;
import com.redhat.qute.parser.template.RangeOffset;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.services.datamodel.JavaDataModelCache;
import com.redhat.qute.services.hover.HoverRequest;
import com.redhat.qute.settings.SharedSettings;
import com.redhat.qute.utils.QutePositionUtility;

/**
 * Qute hover support.
 *
 * @author Angelo ZERR
 *
 */
public class QuteHover {

	private static final Logger LOGGER = Logger.getLogger(QuteHover.class.getName());

	private static CompletableFuture<Hover> NO_HOVER = CompletableFuture.completedFuture(null);

	private final JavaDataModelCache javaCache;

	public QuteHover(JavaDataModelCache javaCache) {
		this.javaCache = javaCache;
	}

	public CompletableFuture<Hover> doHover(Template template, Position position, SharedSettings settings,
			CancelChecker cancelChecker) {
		cancelChecker.checkCanceled();
		HoverRequest hoverRequest = null;
		try {
			hoverRequest = new HoverRequest(template, position, settings);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Failed creating HoverRequest", e);
			return NO_HOVER;
		}
		Node node = hoverRequest.getNode();
		if (node == null) {
			return NO_HOVER;
		}
		switch (node.getKind()) {
		case ParameterDeclaration:
			ParameterDeclaration parameterDeclaration = (ParameterDeclaration) node;
			return doHoverForParameterDeclaration(parameterDeclaration, template, hoverRequest, cancelChecker);
		case ExpressionPart:
			Part part = (Part) node;
			return doHoverForExpressionPart(part, template, hoverRequest, cancelChecker);
		default:
			return NO_HOVER;
		}
	}

	private CompletableFuture<Hover> doHoverForParameterDeclaration(ParameterDeclaration parameterDeclaration,
			Template template, HoverRequest hoverRequest, CancelChecker cancelChecker) {
		RangeOffset classNameRange = parameterDeclaration.getClassNameRange(hoverRequest.getOffset());
		if (classNameRange != null) {
			if (classNameRange != null) {
				String className = template.getText(classNameRange);
				return javaCache.resolveJavaType(className, template.getProjectUri()) //
						.thenApply(resolvedType -> {
							if (resolvedType != null) {
								boolean hasMarkdown = true;
								String markupKind = hasMarkdown ? MarkupKind.MARKDOWN : MarkupKind.PLAINTEXT;
								String content = resolvedType.getClassName();
								Range range = QutePositionUtility.createRange(classNameRange, template);
								return new Hover(new MarkupContent(markupKind, content), range);
							}
							return null;
						});
			}
		}
		return NO_HOVER;
	}

	private CompletableFuture<Hover> doHoverForExpressionPart(Part part, Template template, HoverRequest hoverRequest,
			CancelChecker cancelChecker) {
		return javaCache.resolveJavaType(part, template.getProjectUri()) //
				.thenApply(resolvedType -> {
					if (resolvedType != null) {
						boolean hasMarkdown = true;
						String markupKind = hasMarkdown ? MarkupKind.MARKDOWN : MarkupKind.PLAINTEXT;
						String content = resolvedType.getClassName();
						Range range = QutePositionUtility.createRange(part);
						return new Hover(new MarkupContent(markupKind, content), range);
					}
					return null;
				});
	}
}
