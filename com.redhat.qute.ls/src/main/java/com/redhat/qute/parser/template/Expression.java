package com.redhat.qute.parser.template;

import java.util.List;

import com.redhat.qute.parser.expression.ExpressionParser;
import com.redhat.qute.parser.expression.Parts;

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
		parseExpressionIfNeeded();
		Node node = findNodeAt(expressionContent, offset);
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
}
