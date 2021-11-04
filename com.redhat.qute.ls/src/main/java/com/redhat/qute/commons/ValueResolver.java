package com.redhat.qute.commons;

public class ValueResolver extends JavaMethodInfo {

	private String namespace;

	private String sourceType;

	public boolean match(ResolvedJavaClassInfo javaType) {
		JavaMethodParameterInfo parameter = getParameterAt(0);
		if (parameter == null) {
			return false;
		}
		String parameterType = parameter.getType();
		if (parameterType.equals(javaType.getClassName())) {
			return true;
		}
		if (javaType.getExtendedTypes() != null) {
			for (String extendedType : javaType.getExtendedTypes()) {
				if (parameterType.equals(extendedType)) {
					return true;
				}
			}
		}
		return false;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public boolean match(String property) {
		return ResolvedJavaClassInfo.isMatchMethod(this, property);
	}

	@Override
	public boolean hasParameters() {
		if (namespace != null) {
			return !getParameters().isEmpty();
		}
		return getParameters().size() - 1 > 0;
	}

	@Override
	public String getSignature() {
		String signature = super.getSignature();
		return namespace != null ? namespace + ":" + signature : signature;
	}

}
