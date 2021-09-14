package com.redhat.qute.commons;

public class QuteJavaClassesParams {

	private String uri;
	
	private String pattern;

	public QuteJavaClassesParams() {
	}
	
	public QuteJavaClassesParams(String pattern, String uri) {
		setPattern(pattern);
		setUri(uri);
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getPattern() {
		return pattern;
	}
	
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
}
