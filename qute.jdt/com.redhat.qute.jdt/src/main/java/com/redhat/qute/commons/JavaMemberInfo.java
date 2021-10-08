package com.redhat.qute.commons;

public abstract class JavaMemberInfo {

	public static enum JavaMemberKind {
		FIELD, METHOD;
	}

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public abstract JavaMemberKind getKind();

	public abstract String getMemberType();
}