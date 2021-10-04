package com.redhat.qute.parser.template;

public class ParameterInfo {

	public static final String EMPTY = "$empty$";
	
	private final String name;

	private final String defaultValue;

	private final boolean optional;;

	public ParameterInfo(String name, String defaultValue, boolean optional) {
		this.name = name;
		this.defaultValue = defaultValue;
		this.optional = optional;
	}

	public String getName() {
		return name;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public boolean isOptional() {
		return optional;
	}
}
