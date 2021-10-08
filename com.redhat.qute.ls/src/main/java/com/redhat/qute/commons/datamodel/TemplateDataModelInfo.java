package com.redhat.qute.commons.datamodel;

import java.util.List;

public class TemplateDataModelInfo {

	private String templateUri;

	private String sourceType;

	private String sourceMethod;

	private List<ParameterDataModelInfo> parameters;

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

	public List<ParameterDataModelInfo> getParameters() {
		return parameters;
	}

	public void setParameters(List<ParameterDataModelInfo> parameters) {
		this.parameters = parameters;
	}

}
