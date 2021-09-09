package com.redhat.qute.commons;

public class JavaClassInfo {

	private String className;

	private String uri;

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
	
	public boolean isPackage() {
		return uri == null;
	}
}
