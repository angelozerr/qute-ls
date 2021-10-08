package com.redhat.qute.commons.datamodel;

public class QuteProjectDataModelParams {

	private String projectUri;

	public QuteProjectDataModelParams() {

	}

	public QuteProjectDataModelParams(String projectUri) {
		setProjectUri(projectUri);
	}

	public String getProjectUri() {
		return projectUri;
	}

	public void setProjectUri(String projectUri) {
		this.projectUri = projectUri;
	}

}
