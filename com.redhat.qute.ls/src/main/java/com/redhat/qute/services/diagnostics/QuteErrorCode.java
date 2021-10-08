package com.redhat.qute.services.diagnostics;

public enum QuteErrorCode implements IQuteErrorCode {

	UndefinedVariable, //
	UnkwownType, //
	UnkwownMethod, //
	UnkwownProperty, //
	NotInstanceOfIterable;

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
