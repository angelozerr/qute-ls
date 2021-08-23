package com.redhat.qute.parser.scanner;

public enum ScannerState {
	WithinContent, //
	WithinExpression, //
	WithinComment, //

	AfterOpeningStartTag, //
	WithinTag, //
	AfterOpeningEndTag, //
	WithinEndTag, //

	WithinParameterDeclaration;
}
