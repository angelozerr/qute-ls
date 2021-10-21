package com.redhat.qute.parser.expression;

import com.redhat.qute.parser.expression.Parts.PartKind;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;

public abstract class Part extends Node {

	private String textContent;

	public Part(int start, int end) {
		super(start, end);
	}

	public String getPartName() {
		return getTextContent();
	}

	@Override
	public String getNodeName() {
		return "#part";
	}

	@Override
	public NodeKind getKind() {
		return NodeKind.ExpressionPart;
	}

	@Override
	public Parts getParent() {
		return (Parts) super.getParent();
	}

	public abstract PartKind getPartKind();

	public String getTextContent() {
		if (textContent != null) {
			return textContent;
		}
		return textContent = getOwnerTemplate().getText(getStart(), getEnd());
	}

	@Override
	public String toString() {
		return getPartName();
	}

	public boolean isLast() {
		Parts parts = getParent();
		return parts.getPartIndex(this) == parts.getChildCount() - 1;
	}

}
