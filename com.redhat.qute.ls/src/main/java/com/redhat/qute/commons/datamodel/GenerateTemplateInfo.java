package com.redhat.qute.commons.datamodel;

import java.util.List;

public class GenerateTemplateInfo {

	private String projectUri;

	private String templateFileUri;

	private String templateFilePath;

	private List<ParameterDataModel> parameters;

	public String getProjectUri() {
		return projectUri;
	}

	public void setProjectUri(String projectUri) {
		this.projectUri = projectUri;
	}

	public String getTemplateFileUri() {
		return templateFileUri;
	}

	public void setTemplateFileUri(String templateFileUri) {
		this.templateFileUri = templateFileUri;
	}

	public String getTemplateFilePath() {
		return templateFilePath;
	}

	public void setTemplateFilePath(String templateFilePath) {
		this.templateFilePath = templateFilePath;
	}

	public List<ParameterDataModel> getParameters() {
		return parameters;
	}

	public void setParameters(List<ParameterDataModel> parameters) {
		this.parameters = parameters;
	}

}
