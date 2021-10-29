package com.redhat.qute.commons;

import java.util.Collections;
import java.util.List;

public class ResolvedJavaClassInfo extends JavaClassInfo {

	private List<String> extendedTypes;

	private List<JavaFieldInfo> fields;

	private List<JavaMethodInfo> methods;

	private String iterableType;

	private String iterableOf;

	private Boolean isIterable;

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
		if (isIterable != null) {
			return isIterable.booleanValue();
		}
		isIterable = computeIsIterable();
		return isIterable.booleanValue();
	}

	private synchronized boolean computeIsIterable() {
		if (isIterable != null) {
			return isIterable.booleanValue();
		}
		if (iterableOf != null) {
			return true;
		}
		if (extendedTypes != null) {
			for (String extendedType : extendedTypes) {
				if ("Iterable".equals(extendedType) || extendedType.equals("java.lang.Iterable")) {
					this.iterableOf = "java.lang.Object";
					this.iterableType = getClassName();
					return true;
				}
			}
		}
		return false;
	}

	public JavaMemberInfo findMember(String property) {
		JavaFieldInfo fieldInfo = findField(property);
		if (fieldInfo != null) {
			return fieldInfo;
		}
		return findMethod(property);
	}

	public JavaFieldInfo findField(String fieldName) {
		if (fields == null || fields.isEmpty() || isEmpty(fieldName)) {
			return null;
		}
		for (JavaFieldInfo field : fields) {
			if (fieldName.equals(field.getName())) {
				return field;
			}
		}
		return null;
	}

	public JavaMethodInfo findMethod(String propertyOrMethodName) {
		if (methods == null || methods.isEmpty() || isEmpty(propertyOrMethodName)) {
			return null;
		}
		String getterMethodName = computeGetterName(propertyOrMethodName);
		for (JavaMethodInfo method : methods) {
			if (isMatchMethod(method, propertyOrMethodName, getterMethodName)) {
				return method;
			}
		}
		return null;
	}

	private static String computeGetterName(String propertyOrMethodName) {
		return "get" + (propertyOrMethodName.charAt(0) + "").toUpperCase()
				+ propertyOrMethodName.substring(1, propertyOrMethodName.length());
	}

	public static boolean isMatchMethod(JavaMethodInfo method, String propertyOrMethodName) {
		String getterMethodName = computeGetterName(propertyOrMethodName);
		return isMatchMethod(method, propertyOrMethodName, getterMethodName);
	}

	private static boolean isMatchMethod(JavaMethodInfo method, String propertyOrMethodName, String getterMethodName) {
		if (propertyOrMethodName.equals(method.getName()) || getterMethodName.equals(method.getName())) {
			return true;
		}
		return false;
	}

	private static boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

}
