package com.redhat.qute.commons.datamodel;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TemplateDataModel<T extends ParameterDataModel> {

	private String templateUri;

	private String sourceType;

	private String sourceMethod;

	private String sourceField;

	private List<T> parameters;

	private volatile Map<String, T> parametersMap;

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

	public String getSourceField() {
		return sourceField;
	}

	public void setSourceField(String sourceField) {
		this.sourceField = sourceField;
	}

	public List<T> getParameters() {
		return parameters;
	}

	public void setParameters(List<T> parameters) {
		this.parameters = parameters;
	}

	public T getParameter(String partName) {
		List<T> parameters = getParameters();
		if (parameters == null) {
			return null;
		}
		return getParametersMap().get(partName);
	}

	public void addParameter(T parameter) {
		parameters.add(parameter);
		getParametersMap().put(parameter.getKey(), parameter);
	}

	private Map<String, T> getParametersMap() {
		if (parametersMap == null) {
			parametersMap = parameters.stream()
					.collect(Collectors.toMap(ParameterDataModel::getKey, Function.identity()));
		}
		return parametersMap;
	}

}
