package com.redhat.qute.parser.expression;

import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;

public class Parts extends Node {

	public static enum PartKind {
		Namespace, //
		Object, //
		Property, //
		Method;
	}

	public Parts(int start, int end) {
		super(start, end);
	}

	@Override
	public String getNodeName() {
		return "#parts";
	}

	@Override
	public NodeKind getKind() {
		return NodeKind.ExpressionParts;
	}

	void addPart(Part part) {
		super.addChild(part);
	}

	public void addDot(int tokenOffset) {
		super.setEnd(tokenOffset + 1);
	}

	public void addColonSpace(int tokenOffset) {
		super.setEnd(tokenOffset + 1);
	}
	public Part getPartAt(int offset) {
		Node node = super.findNodeAt(offset);
		if (node != null && node.getKind() == NodeKind.ExpressionPart) {
			return (Part) node;
		}
		return null;
	}

	void setExpressionParent(Expression expression) {
		super.setParent(expression);
	}

}
