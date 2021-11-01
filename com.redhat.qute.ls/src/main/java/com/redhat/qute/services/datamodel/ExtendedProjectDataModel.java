package com.redhat.qute.services.datamodel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.redhat.qute.commons.datamodel.ParameterDataModel;
import com.redhat.qute.commons.datamodel.ProjectDataModel;
import com.redhat.qute.commons.datamodel.TemplateDataModel;

public class ExtendedProjectDataModel extends ProjectDataModel<ExtendedTemplateDataModel> {

	public ExtendedProjectDataModel(ProjectDataModel<TemplateDataModel<ParameterDataModel>> project) {
		super.setTemplates(createTemplates(project.getTemplates()));
		super.setValueResolvers(project.getValueResolvers());
	}

	private List<ExtendedTemplateDataModel> createTemplates(List<TemplateDataModel<ParameterDataModel>> templates) {
		if (templates == null || templates.isEmpty()) {
			return Collections.emptyList();
		}
		return templates.stream() //
				.map(template -> {
					return new ExtendedTemplateDataModel(template);
				}) //
				.collect(Collectors.toList());
	}

}
