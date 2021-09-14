package com.redhat.qute.commons;

import java.util.List;

public class ResolvedJavaClassInfo extends JavaClassInfo  {

	private List<JavaClassMemberInfo> members;
	
	public void setMembers(List<JavaClassMemberInfo> members) {
		this.members = members;
	}
	
	public List<JavaClassMemberInfo> getMembers() {
		return members;
	}
}
