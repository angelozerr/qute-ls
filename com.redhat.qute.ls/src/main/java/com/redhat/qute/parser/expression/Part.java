package com.redhat.qute.parser.expression;

import com.redhat.qute.parser.expression.Parts.PartKind;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;

public abstract class Part extends Node {
	
	private String textContent;

	public Part(int start, int end) {
		super(start, end);
	}

	@Override
	public String getNodeName() {
		return "#part";
	}

	@Override
	public NodeKind getKind() {
		return NodeKind.ExpressionPart;
	}

	public abstract PartKind getPartKind();

	public String getTextContent() {
		if (textContent != null) {
			return textContent;
		}
		return textContent = getOwnerTemplate().getText(getStart(), getEnd());
	}

	public abstract String getClassName();

}
