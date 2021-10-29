package com.redhat.qute.parser.template;

import java.util.List;

import com.redhat.qute.parser.expression.ExpressionParser;
import com.redhat.qute.parser.expression.Part;
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
		expressionContent = ExpressionParser.parse(this, getOwnerTemplate().getCancelChecker());
	}

	public List<Node> getExpressionContent() {
		parseExpressionIfNeeded();
		return expressionContent;
	}

	/**
	 * Returns the last part of the expression and null otherwise.
	 * 
	 * @return the last part of the expression and null otherwise.
	 */
	public Part getLastPart() {
		List<Node> nodes = getExpressionContent();
		if (nodes.isEmpty()) {
			return null;
		}
		Parts parts = (Parts) nodes.get(0);
		return (Part) parts.getLastChild();
	}
}
