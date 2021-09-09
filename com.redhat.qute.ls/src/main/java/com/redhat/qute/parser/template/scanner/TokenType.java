package com.redhat.qute.parser.template.scanner;

public enum TokenType {

	// String token types
	StartString, //
	String, //
	EndString, //

	// Comment token types
	StartComment, //
	Comment, //
	EndComment, //

	// Expressions token types
	StartExpression, //
	//Expression, //
	EndExpression, //

	// Section tag token types
	StartTagOpen, //
	StartTag, //
	StartTagSelfClose, //
	StartTagClose, //
	EndTagOpen, //
	EndTag, //
	EndTagClose, //
	ParameterTag, //

	// Parameter declaration
	StartParameterDeclaration, //
	ParameterDeclaration, //
	EndParameterDeclaration, //

	// Other token types
	Content, //
	Whitespace, //
	Unknown, //
	EOS;
}
