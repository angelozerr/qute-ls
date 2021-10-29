package com.redhat.qute.services.datamodel;

import com.redhat.qute.commons.datamodel.ParameterDataModel;
import com.redhat.qute.parser.template.JavaTypeInfoProvider;
import com.redhat.qute.parser.template.Node;

public class ExtendedParameterDataModel extends ParameterDataModel implements JavaTypeInfoProvider {

	private final ExtendedTemplateDataModel template;

	public ExtendedParameterDataModel(ParameterDataModel parameter, ExtendedTemplateDataModel template) {
		super.setKey(parameter.getKey());
		super.setSourceType(parameter.getSourceType());
		this.template = template;
	}

	@Override
	public String getJavaType() {
		return getSourceType();
	}

	@Override
	public Node getJavaTypeOwnerNode() {
		return null;
	}

	public ExtendedTemplateDataModel getTemplate() {
		return template;
	}

}
