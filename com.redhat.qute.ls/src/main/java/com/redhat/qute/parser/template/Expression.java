package com.redhat.qute.parser.template;

import java.util.List;

import com.redhat.qute.parser.expression.ExpressionParser;

public class Expression extends Node {

	private List<Node> expressionContent;

	Expression(int start, int end) {
		super(start, end);
		this.expressionContent = null;
	}

	@Override
	public NodeKind getKind() {
		return NodeKind.Expression;
	}

	public String getNodeName() {
		return "#expression";
	}

	public Node findNodeExpressionAt(int offset) {
		Node node = findNodeAt(getExpressionContent(), offset);
		if (node != null) {
			return node;
		}
		return null;
	}

	private synchronized void parseExpressionIfNeeded() {
		if (expressionContent != null) {
			return;
		}
		expressionContent = ExpressionParser.parse(this, null);
	}

	public List<Node> getExpressionContent() {
		parseExpressionIfNeeded();
		return expressionContent;
	}
}
