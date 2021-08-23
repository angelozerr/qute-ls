package com.redhat.qute.parser.scanner;

public enum TokenType {

	// Comment token types
	StartComment, //
	Comment, //
	EndComment, //

	// Expressions token types
	StartExpression, //
	Expression, //
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
