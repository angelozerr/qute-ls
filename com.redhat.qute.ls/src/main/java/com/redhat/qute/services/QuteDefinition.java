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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.Node;
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

	public List<? extends LocationLink> findDefinition(Template template, Position position,
			CancelChecker cancelChecker) {
		try {
			int offset = template.offsetAt(position);
			Node node = template.findNodeAt(offset);
			if (node == null) {
				return Collections.emptyList();
			}
			List<LocationLink> locations = new ArrayList<>();
			switch (node.getKind()) {
			case Section:
				// Start end tag definition
				findDefinitionFromSection(offset, (Section) node, template, locations);
				break;
			case ParameterDeclaration:
				findDefinitionFromParameterDeclaration(offset, (ParameterDeclaration) node, template, locations);
				break;
			case Expression:
				findDefinitionFromExpression(offset, (Expression) node, template, locations);
				break;				
			default:
				// do nothing
			}
			return locations;
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In QuteDefinition the client provided Position is at a BadLocation", e);
			return Collections.emptyList();
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
	 * @param request   the definition request
	 * @param locations the locations
	 * @throws BadLocationException
	 */
	private static void findDefinitionFromSection(int offset, Section sectionTag, Template template,
			List<LocationLink> locations) throws BadLocationException {
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
	}

	private static void findDefinitionFromParameterDeclaration(int offset, ParameterDeclaration node, Template template,
			List<LocationLink> locations) {
		if (node.isInClassName(offset)) {
			String className = node.getClassName();
			
		}
	}

	private void findDefinitionFromExpression(int offset, Expression node, Template template,
			List<LocationLink> locations) {
		// TODO Auto-generated method stub
		
	}

}
