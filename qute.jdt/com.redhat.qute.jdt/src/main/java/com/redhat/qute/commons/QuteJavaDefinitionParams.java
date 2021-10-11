package com.redhat.qute.commons;

public class QuteJavaDefinitionParams {

	private String projectUri;

	private String className;

	private String field;

	private String method;

	private String methodParameter;

	public QuteJavaDefinitionParams() {

	}

	public QuteJavaDefinitionParams(String className, String projectUri) {
		setClassName(className);
		setProjectUri(projectUri);
	}

	public String getProjectUri() {
		return projectUri;
	}

	public void setProjectUri(String projectUri) {
		this.projectUri = projectUri;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getMethodParameter() {
		return methodParameter;
	}

	public void setMethodParameter(String methodParameter) {
		this.methodParameter = methodParameter;
	}
}
