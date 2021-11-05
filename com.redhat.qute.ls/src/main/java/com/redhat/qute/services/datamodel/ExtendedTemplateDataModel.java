package com.redhat.qute.services.datamodel;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.redhat.qute.commons.datamodel.ParameterDataModel;
import com.redhat.qute.commons.datamodel.TemplateDataModel;

public class ExtendedTemplateDataModel extends TemplateDataModel<ExtendedParameterDataModel> {

	public ExtendedTemplateDataModel(TemplateDataModel<ParameterDataModel> template) {
		super.setTemplateUri(template.getTemplateUri());
		super.setSourceType(template.getSourceType());
		super.setSourceMethod(template.getSourceMethod());
		super.setSourceField(template.getSourceField());
		super.setParameters(createParameters(template.getParameters(), this));
	}

	private List<ExtendedParameterDataModel> createParameters(List<ParameterDataModel> parameters,
			ExtendedTemplateDataModel template) {
		return parameters.stream() //
				.map(parameter -> new ExtendedParameterDataModel(parameter, template)) //
				.collect(Collectors.toList());
	}
}
