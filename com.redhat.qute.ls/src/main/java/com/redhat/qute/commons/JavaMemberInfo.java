package com.redhat.qute.commons;

public abstract class JavaMemberInfo {

	public static enum JavaMemberKind {
		FIELD, METHOD;
	}

	private String name;

	private transient ResolvedJavaClassInfo resolvedClass;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ResolvedJavaClassInfo getResolvedClass() {
		return resolvedClass;
	}
	
	public void setResolvedClass(ResolvedJavaClassInfo resolvedClass) {
		this.resolvedClass = resolvedClass;
	}
	
	public abstract JavaMemberKind getKind();

	public abstract String getMemberType();
}
