package com.redhat.qute.commons;

public class ValueResolver extends JavaMethodInfo {

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

	public boolean match(String property) {
		return ResolvedJavaClassInfo.isMatchMethod(this, property);
	}
	
	@Override
	public boolean hasParameters() {
		return getParameters().size() - 1 > 0;
	}

}
