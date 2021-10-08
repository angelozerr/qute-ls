package com.redhat.qute.parser.expression.scanner;

public enum ScannerState {
	WithinExpression, //
	WithinParts, //
	WithingMethod, //
	WithinString, //
	AfterNamespace;
}
