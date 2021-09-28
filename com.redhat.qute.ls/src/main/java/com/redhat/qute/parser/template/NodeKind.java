package com.redhat.qute.parser.template;

public enum NodeKind {

	// Template nodes
	Template, //
	ParameterDeclaration, //
	Section, //
	Expression, //
	Comment, //
	Text, //
	
	// Expression nodes
	ExpressionParts, //
	ExpressionPart,
	
	// Parameter in section
	Parameter;
}
