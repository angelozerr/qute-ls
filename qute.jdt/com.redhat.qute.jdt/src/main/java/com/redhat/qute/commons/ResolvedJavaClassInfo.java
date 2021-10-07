package com.redhat.qute.commons;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ResolvedJavaClassInfo extends JavaClassInfo {

	private List<String> extendedTypes;

	private List<JavaFieldInfo> fields;

	private List<JavaMethodInfo> methods;

	private String iterableType;

	private String iterableOf;

	public void setExtendedTypes(List<String> extendedTypes) {
		this.extendedTypes = extendedTypes;
	}

	public List<String> getExtendedTypes() {
		return extendedTypes;
	}

	public List<JavaFieldInfo> getFields() {
		return fields != null ? fields : Collections.emptyList();
	}

	public void setFields(List<JavaFieldInfo> fields) {
		this.fields = fields;
	}

	public List<JavaMethodInfo> getMethods() {
		return methods != null ? methods : Collections.emptyList();
	}

	public void setMethods(List<JavaMethodInfo> methods) {
		this.methods = methods;
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

	public JavaMemberInfo findMember(String property) {
		JavaFieldInfo fieldInfo = findField(property);
		if (fieldInfo != null) {
			return fieldInfo;
		}
		return findMethod(property);
	}

	public JavaFieldInfo findField(String fieldName) {
		if (fields == null) {
			return null;
		}

		Optional<JavaFieldInfo> fieldInfo = fields.stream()//
				.filter(field -> fieldName.equals(field.getName())) //
				.findFirst();
		return fieldInfo.isPresent() ? fieldInfo.get() : null;
	}

	public JavaMethodInfo findMethod(String propertyOrMethodName) {
		if (fields == null) {
			return null;
		}

		String getterMethodName = "get" + (propertyOrMethodName.charAt(0) + "").toUpperCase() + propertyOrMethodName.substring(1, propertyOrMethodName.length());

		Optional<JavaMethodInfo> methodInfo = methods.stream()//
				.filter(method -> propertyOrMethodName.equals(method.getName()) || getterMethodName.equals(method.getName())) //
				.findFirst();
		return methodInfo.isPresent() ? methodInfo.get() : null;
	}
}
