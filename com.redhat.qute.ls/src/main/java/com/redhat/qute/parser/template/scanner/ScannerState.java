package com.redhat.qute.parser.template.scanner;

public enum ScannerState {
	WithinContent, //
	WithinExpression, //
	WithinComment, //

	AfterOpeningStartTag, //
	WithinTag, //
	AfterOpeningEndTag, //
	WithinEndTag, //

	WithinParameterDeclaration, //
	WithinString;
}
