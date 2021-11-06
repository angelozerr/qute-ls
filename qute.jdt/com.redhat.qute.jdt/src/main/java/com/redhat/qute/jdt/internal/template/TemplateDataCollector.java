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
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.StringLiteral;

import com.redhat.qute.commons.datamodel.ParameterDataModel;
import com.redhat.qute.commons.datamodel.TemplateDataModel;

/**
 * AST visitor used to collect {@link ParameterDataModel} parameter for a given
 * {@link TemplateDataModel} template.
 * 
 * This visitor track the invocation of method
 * io.quarkus.qute.Template#data(String key, Object data) to collect parameters.
 * 
 * For instance, with this following code:
 * 
 * <code>
 * private final Template page;
 * ...
 * page.data("age", 13);
   page.data("name", "John");
 * </code>
 * 
 * the AST visitor will collect the following parameters:
 * 
 * <ul>
 * <li>parameter key='age', sourceType='int'</li>
 * <li>parameter key='name', sourceType='java.lang.String'</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public class TemplateDataCollector extends ASTVisitor {

	private static final String DATA_METHOD = "data";

	private final TemplateDataModel<ParameterDataModel> template;

	public TemplateDataCollector(TemplateDataModel<ParameterDataModel> template, IProgressMonitor monitor) {
		this.template = template;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		String methodName = node.getName().getIdentifier();
		if (DATA_METHOD.equals(methodName)) {
			// .data("book", book)
			@SuppressWarnings("rawtypes")
			List arguments = node.arguments();
			if (arguments.size() == 2) {
				String paramName = null;
				Object name = arguments.get(0);
				if (name instanceof StringLiteral) {
					paramName = ((StringLiteral) name).getLiteralValue();
				}
				String paramType = "java.lang.Object";
				Object type = arguments.get(1);
				if (type instanceof Expression) {
					ITypeBinding binding = ((Expression) type).resolveTypeBinding();
					paramType = binding.getQualifiedName();
				}

				if (paramName != null && template.getParameter(paramName) == null) {
					ParameterDataModel parameter = new ParameterDataModel();
					parameter.setKey(paramName);
					parameter.setSourceType(paramType);
					template.addParameter(parameter);
				}
			}
		}
		return super.visit(node);
	}

}
