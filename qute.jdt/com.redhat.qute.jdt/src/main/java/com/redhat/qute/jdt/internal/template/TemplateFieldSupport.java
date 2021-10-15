package com.redhat.qute.jdt.internal.template;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.core.nd.field.IField;

import com.redhat.qute.commons.datamodel.ParameterDataModel;
import com.redhat.qute.commons.datamodel.TemplateDataModel;

class TemplateFieldSupport {

	public static void collectTemplateDataModelForTemplateField(IField field,
			List<TemplateDataModel<ParameterDataModel>> templates, IProgressMonitor monitor) {
		String fieldName = field.getFieldName();
		
	}
}
