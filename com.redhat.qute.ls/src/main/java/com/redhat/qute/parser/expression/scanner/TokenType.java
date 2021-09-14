package com.redhat.qute.parser.expression.scanner;

public enum TokenType {
	NamespacePart,

	ObjectPart,

	PropertyPart, //
	Dot, //
	ColonSpace, //
	Whitespace, //
	Unknown, //
	EOS;
}
