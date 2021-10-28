/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.parser.template.sections;

import java.util.List;

import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;

/**
 * Base class for #set and #let section.
 * 
 * @author Angelo ZERR
 * 
 * @see https://quarkus.io/guides/qute-reference#letset-section
 *
 */
public abstract class AssignSection extends Section {

	public AssignSection(String tag, int start, int end) {
		super(tag, start, end);
	}

	protected void initializeParameters(List<Parameter> parameters) {
		// All parameters can have expression (ex : {#set myParent=order.item.parent
		// isActive=false age=10}
		parameters.forEach(parameter -> {
			parameter.setCanHaveExpression(parameter.hasValueAssigned());
		});
	}
}
