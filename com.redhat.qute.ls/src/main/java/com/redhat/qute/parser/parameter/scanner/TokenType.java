package com.redhat.qute.parser.parameter.scanner;

public enum TokenType {

	ParameterName, //
	Assign, //
	ParameterValue,
	// Other token types
	Whitespace, //
	Unknown, //
	EOS;
}
