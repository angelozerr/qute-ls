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
import org.eclipse.jdt.core.IField;

import com.redhat.qute.commons.datamodel.ParameterDataModel;
import com.redhat.qute.commons.datamodel.TemplateDataModel;

class TemplateFieldSupport {

	public static void collectTemplateDataModelForTemplateField(IField field,
			List<TemplateDataModel<ParameterDataModel>> templates, IProgressMonitor monitor) {
		//String fieldName = field.getFieldName();
		
	}
}
