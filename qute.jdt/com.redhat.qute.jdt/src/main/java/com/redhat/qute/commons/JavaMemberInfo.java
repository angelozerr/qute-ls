package com.redhat.qute.commons;

public abstract class JavaMemberInfo {

	public static enum JavaMemberKind {
		FIELD, METHOD;
	}

	private String name;

	private String description;

	private transient ResolvedJavaClassInfo resolvedClass;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ResolvedJavaClassInfo getResolvedClass() {
		return resolvedClass;
	}

	public void setResolvedClass(ResolvedJavaClassInfo resolvedClass) {
		this.resolvedClass = resolvedClass;
	}

	public abstract JavaMemberKind getKind();

	public abstract String getMemberType();

	public String getMemberSimpleType() {
		String type = getMemberType();
		if (type == null) {
			return null;
		}

		int startBracketIndex = type.indexOf('<');
		if (startBracketIndex != -1) {
			int endBracketIndex = type.indexOf('>', startBracketIndex);
			String generic = getSimpleType(type.substring(startBracketIndex + 1, endBracketIndex));
			String mainType = getSimpleType(type.substring(0, startBracketIndex));
			return mainType + '<' + generic + '>';
		}
		return getSimpleType(type);
	}

	private static String getSimpleType(String type) {
		int index = type.lastIndexOf('.');
		return index != -1 ? type.substring(index + 1, type.length()) : type;
	}
}
