package com.redhat.qute.commons;

import java.util.List;
import java.util.Optional;

public class ResolvedJavaClassInfo extends JavaClassInfo {

	private List<JavaClassMemberInfo> members;

	public void setMembers(List<JavaClassMemberInfo> members) {
		this.members = members;
	}

	public List<JavaClassMemberInfo> getMembers() {
		return members;
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
