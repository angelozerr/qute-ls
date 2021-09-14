package com.redhat.qute.commons;

public class QuteJavaDefinitionParams {

	private String uri;

	private String className;

	private String field;

	private String method;

	public QuteJavaDefinitionParams() {

	}

	public QuteJavaDefinitionParams(String className, String uri) {
		setClassName(className);
		setUri(uri);
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
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
}
