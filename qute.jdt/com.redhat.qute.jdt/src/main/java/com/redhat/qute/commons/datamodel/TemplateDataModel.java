package com.redhat.qute.commons.datamodel;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TemplateDataModel {

	private String templateUri;

	private String sourceType;

	private String sourceMethod;

	private List<ParameterDataModel> parameters;

	private volatile Map<String, ParameterDataModel> parametersMap;

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

	public ParameterDataModel getParameter(String partName) {
		if (parameters == null) {
			return null;
		}
		if (parametersMap == null) {
			parametersMap = parameters.stream()
					.collect(Collectors.toMap(ParameterDataModel::getKey, Function.identity()));
		}
		return parametersMap.get(partName);
	}

}
