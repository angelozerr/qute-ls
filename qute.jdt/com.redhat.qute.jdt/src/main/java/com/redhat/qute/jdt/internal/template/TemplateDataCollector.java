/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt.internal.template;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;

import com.redhat.qute.commons.datamodel.ParameterDataModel;
import com.redhat.qute.commons.datamodel.TemplateDataModel;

public class TemplateDataCollector extends ASTVisitor {

	private final IMethod method;

	private final TemplateDataModel<ParameterDataModel> template;

	public TemplateDataCollector(IMethod method, TemplateDataModel<ParameterDataModel> template,
			IProgressMonitor monitor) {
		this.method = method;
		this.template = template;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		if ("data".equals(node.getName().getIdentifier())) {
			// .data("book", book)
			List arguments = node.arguments();
			if (arguments.size() == 2) {

				String paramName = null;
				Object name = arguments.get(0);
				if (name instanceof StringLiteral) {
					paramName = ((StringLiteral) name).getLiteralValue();
				}

				String paramType = null;
				Object second = arguments.get(1);
				if (second instanceof SimpleName) {
					paramType = ((SimpleName) second).getIdentifier();
				}
				if (template.getParameter(paramName) == null) {
					ParameterDataModel parameter = new ParameterDataModel();
					parameter.setKey(paramName);
					parameter.setSourceType("java.lang.String");
					template.addParameter(parameter);
				}
			}
		}

		// TO_REMOVE(node);

		return super.visit(node);
	}

	private void TO_REMOVE(MethodInvocation node) {
		String methodName = node.getName().getIdentifier();
		if (methodName.equals(template.getSourceMethod())) {
			ASTNode parent = node.getParent();
			while (parent != null && parent.getNodeType() == ASTNode.METHOD_INVOCATION) {
				if (parent.getNodeType() == ASTNode.METHOD_INVOCATION) {
					MethodInvocation invocation = (MethodInvocation) parent;
					if ("data".equals(invocation.getName().getIdentifier())) {
						// .data("book", book)
						List arguments = invocation.arguments();
						if (arguments.size() == 2) {

							String paramName = null;
							Object name = arguments.get(0);
							if (name instanceof StringLiteral) {
								paramName = ((StringLiteral) name).getLiteralValue();
							}

							String paramType = null;
							Object second = arguments.get(1);
							if (second instanceof SimpleName) {
								paramType = ((SimpleName) second).getIdentifier();
							}

							if (template.getParameter(paramName) == null) {
								ParameterDataModel parameter = new ParameterDataModel();
								parameter.setKey(paramName);
								parameter.setSourceType("java.lang.String");
								template.addParameter(parameter);
							}
						}
					}
					parent = parent.getParent();
				}
			}
		}
	}

}
