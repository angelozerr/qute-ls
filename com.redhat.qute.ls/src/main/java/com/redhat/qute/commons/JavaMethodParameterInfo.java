package com.redhat.qute.commons;

public class JavaMethodParameterInfo {

	private final String name;

	private final String type;

	public JavaMethodParameterInfo(String name, String type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

}
