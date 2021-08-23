package com.redhat.qute.parser;

public class Text extends Node {

	Text(int start, int end) {
		super(start, end);
		super.setClosed(true);
	}

	@Override
	public NodeKind getKind() {
		return NodeKind.Text;
	}

	@Override
	public String getNodeName() {
		return "#text";
	}
}
