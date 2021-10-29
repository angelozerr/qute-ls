package com.redhat.qute.commons;

import java.util.ArrayList;
import java.util.List;

public class JavaMethodInfo extends JavaMemberInfo {

	private static final String NO_VALUE = "~";

	private String signature;

	private String returnType;

	private String getterName;
	
	private List<JavaMethodParameterInfo> parameters;

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	public String getReturnType() {
		if (returnType == null) {
			String signature = getSignature();
			int index = signature.lastIndexOf(':');
			returnType = index != -1 ? signature.substring(index + 1, signature.length()).trim() : NO_VALUE;
		}
		return NO_VALUE.equals(returnType) ? null : returnType;
	}

	@Override
	public JavaMemberKind getKind() {
		return JavaMemberKind.METHOD;
	}

	@Override
	public String getMemberType() {
		return getReturnType();
	}

	@Override
	public String getName() {
		String name = super.getName();
		if (name != null) {
			return name;
		}
		String signature = getSignature();
		int index = signature != null ? signature.indexOf('(') : -1;
		if (index != -1) {
			super.setName(signature.substring(0, index));
		}
		return super.getName();
	}

	public String getGetterName() {
		if (getterName == null) {
			getterName = computeGetterName();
		}
		return NO_VALUE.equals(getterName) ? null : getterName;
	}

	private String computeGetterName() {
		if (hasParameters()) {
			return NO_VALUE;
		}
		String methodName = getName();
		int index = -1;
		if (methodName.startsWith("get")) {
			index = 3;
		} else if (methodName.startsWith("is")) {
			index = 2;
		}
		if (index == -1) {
			return NO_VALUE;
		}
		return (methodName.charAt(index) + "").toLowerCase() + methodName.substring(index + 1, methodName.length());
	}

	public boolean hasParameters() {
		String signature = getSignature();
		int start = signature.indexOf('(');
		int end = signature.indexOf(')', start - 1);
		return end - start > 1;
	}
	
	public JavaMethodParameterInfo getParameterAt(int index) {
		List<JavaMethodParameterInfo> parameters = getParameters();
		return parameters.size() > index ? parameters.get(index) : null;
	}
	
	public List<JavaMethodParameterInfo> getParameters() {
		if (parameters == null) {
			parameters = parseParameters();
		}
		return parameters;
	}

	private List<JavaMethodParameterInfo> parseParameters() {
		List<JavaMethodParameterInfo> parameters = new ArrayList<>();
		int start = signature.indexOf('(');
		int end = signature.indexOf(')', start - 1);
		String content = signature.substring(start + 1, end);
		String[] splitParams = content.split(",");
		for (String paramNameAndType : splitParams) {
			int index = paramNameAndType.indexOf(':');
			String paramName = paramNameAndType.substring(0, index).trim();
			String paramType = paramNameAndType.substring(index + 1, paramNameAndType.length()).trim();
			parameters.add(new JavaMethodParameterInfo(paramName, paramType));
		}
		return parameters;
	}

}
