package com.redhat.qute.parser.expression.scanner;

public enum TokenType {
	NamespacePart, //
	ObjectPart, //
	PropertyPart, //
	MethodPart, //
	OpenBracket, //
	CloseBracket, //

	Dot, //
	ColonSpace, //
	StartString, //
	EndString, //
	String, //

	ElvisOperator, //
	TernaryOperator, //

	Whitespace, //
	Unknown, //
	EOS;
}
