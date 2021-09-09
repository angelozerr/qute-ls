package com.redhat.qute.parser.template;

public class Expression extends Node {

	Expression(int start, int end) {
		super(start, end);
	}

	@Override
	public NodeKind getKind() {
		return NodeKind.Expression;
	}

	public String getNodeName() {
		return "#expression";
	}
}
