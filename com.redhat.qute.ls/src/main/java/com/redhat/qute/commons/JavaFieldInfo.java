package com.redhat.qute.commons;

public class JavaFieldInfo extends JavaMemberInfo {

	private String type;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public JavaMemberKind getKind() {
		return JavaMemberKind.FIELD;
	}

	@Override
	public String getMemberType() {
		return getType();
	}
}
