package com.redhat.qute.commons;

public class QuteResolvedJavaClassParams {

	private String className;

	private String uri;

	public QuteResolvedJavaClassParams() {

	}

	public QuteResolvedJavaClassParams(String className, String uri) {
		this.className = className;
		this.uri = uri;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

}
