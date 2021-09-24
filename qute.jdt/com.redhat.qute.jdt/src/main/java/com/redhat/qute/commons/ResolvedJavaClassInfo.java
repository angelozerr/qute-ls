package com.redhat.qute.commons;

import java.util.List;
import java.util.Optional;

public class ResolvedJavaClassInfo extends JavaClassInfo {

	private List<String> extendedTypes;

	private List<JavaClassMemberInfo> members;

	private String iterableType;

	private String iterableOf;

	public void setExtendedTypes(List<String> extendedTypes) {
		this.extendedTypes = extendedTypes;
	}

	public List<String> getExtendedTypes() {
		return extendedTypes;
	}

	public void setMembers(List<JavaClassMemberInfo> members) {
		this.members = members;
	}

	public List<JavaClassMemberInfo> getMembers() {
		return members;
	}

	public String getIterableType() {
		return iterableType;
	}

	public void setIterableType(String iterableType) {
		this.iterableType = iterableType;
	}

	public void setIterableOf(String iterableOf) {
		this.iterableOf = iterableOf;
	}

	public String getIterableOf() {
		return iterableOf;
	}

	public boolean isIterable() {
		return iterableOf != null;
	}

	public JavaClassMemberInfo findMember(String property) {
		if (members == null) {
			return null;
		}
		String getter = "get" + (property.charAt(0) + "").toUpperCase() + property.substring(1, property.length())
				+ "()";
		Optional<JavaClassMemberInfo> memberInfo = members.stream()//
				.filter(member -> property.equals(member.getField()) || getter.equals(member.getMethod())) //
				.findFirst();
		return memberInfo.isPresent() ? memberInfo.get() : null;
	}
}
