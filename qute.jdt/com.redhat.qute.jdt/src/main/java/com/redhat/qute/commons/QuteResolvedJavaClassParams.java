package com.redhat.qute.commons;

public class QuteResolvedJavaClassParams {

	private String className;

	private String projectUri;

	public QuteResolvedJavaClassParams() {

	}

	public QuteResolvedJavaClassParams(String className, String projectUri) {
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

}
