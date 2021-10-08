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

	public ObjectPart getObjectPart() {
		if (super.getChildCount() == 0) {
			return null;
		}
		Part firstPart = (Part) super.getChild(0);
		switch (firstPart.getPartKind()) {
		case Object:
			return (ObjectPart) firstPart;
		case Namespace:
			if (super.getChildCount() == 1) {
				return null;
			}
			Part secondPart = (Part) super.getChild(1);
			return (ObjectPart) secondPart;
		default:
			return null;
		}
	}

	void addPart(Part part) {
		super.addChild(part);
		super.setEnd(part.getEnd());
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

	@Override
	public Expression getParent() {
		return (Expression) super.getParent();
	}

	public int getPartIndex(Part part) {
		return super.getChildren().indexOf(part);
	}

	public Part getPreviousPart(Part part) {
		int partIndex = getPreviousPartIndex(part);
		return partIndex != -1 ? (Part) super.getChild(partIndex) : null;
	}

	private int getPreviousPartIndex(Part part) {
		return part != null ? super.getChildren().indexOf(part) - 1 : super.getChildCount() - 1;
	}

}
