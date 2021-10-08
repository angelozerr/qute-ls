package com.redhat.qute.commons.datamodel;

import java.util.List;

public class TemplateDataModel {

	private String templateUri;

	private String sourceType;

	private String sourceMethod;

	private List<ParameterDataModel> parameters;

	public String getTemplateUri() {
		return templateUri;
	}

	public void setTemplateUri(String templateUri) {
		this.templateUri = templateUri;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public String getSourceMethod() {
		return sourceMethod;
	}

	public void setSourceMethod(String sourceMethod) {
		this.sourceMethod = sourceMethod;
	}

	public List<ParameterDataModel> getParameters() {
		return parameters;
	}

	public void setParameters(List<ParameterDataModel> parameters) {
		this.parameters = parameters;
	}

}
