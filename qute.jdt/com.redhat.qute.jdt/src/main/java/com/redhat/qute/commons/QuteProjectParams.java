package com.redhat.qute.commons;

public class QuteProjectParams {

	private String templateFileUri;

	public QuteProjectParams(String templateFileUri) {
		setTemplateFileUri(templateFileUri);
	}

	public String getTemplateFileUri() {
		return templateFileUri;
	}

	public void setTemplateFileUri(String templateFileUri) {
		this.templateFileUri = templateFileUri;
	}

}
