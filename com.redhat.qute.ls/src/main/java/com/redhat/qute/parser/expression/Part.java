package com.redhat.qute.parser.expression;

import com.redhat.qute.parser.expression.Parts.PartKind;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;

public class Part extends Node {

	private PartKind partKind;

	public Part(int start, int end) {
		super(start, end);
		this.partKind = PartKind.Object;
	}

	@Override
	public String getNodeName() {
		return "#part";
	}

	@Override
	public NodeKind getKind() {
		return NodeKind.ExpressionPart;
	}

	void setPartKind(PartKind partKind) {
		this.partKind = partKind;
	}

	public PartKind getPartKind() {
		return partKind;
	}

}
