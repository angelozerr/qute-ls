package com.redhat.qute.services.diagnostics;

public enum QuteErrorCode implements IQuteErrorCode {

	// Error code for namespaces
	EmptyNamespace, //
	UndefinedNamespace, //
	
	// Error code for object, property, method parts
	UndefinedVariable, //
	UnkwownType, //
	UnkwownMethod, //
	UnkwownProperty, //

	// Error code for #for / #each section
	NotInstanceOfIterable,

	// Error code for #include section
	TemplateNotFound, //
	TemplateNotDefined;

	private final String code;

	QuteErrorCode() {
		this(null);
	}

	QuteErrorCode(String code) {
		this.code = code;
	}

	@Override
	public String getCode() {
		if (code == null) {
			return name();
		}
		return code;
	}

	@Override
	public String toString() {
		return getCode();
	}

}
