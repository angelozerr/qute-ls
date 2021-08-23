package com.redhat.qute.parser;

public class Expression extends Node {

	private int startExpression;

	private int endExpression;

	Expression(int start, int end) {
		super(start, end);
	}

	@Override
	public NodeKind getKind() {
		return NodeKind.Expression;
	}

	public int getStartExpression() {
		return startExpression;
	}

	void setStartExpression(int startExpression) {
		this.startExpression = startExpression;
	}

	public int getEndExpression() {
		return endExpression;
	}

	void setEndExpression(int endExpression) {
		this.endExpression = endExpression;
	}
	
	public String getNodeName() {
		return "#expression";
	}
}
