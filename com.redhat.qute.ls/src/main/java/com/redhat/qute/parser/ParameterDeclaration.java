package com.redhat.qute.parser;

public class ParameterDeclaration extends Node {

	private int startContent;

	private int endContent;

	ParameterDeclaration(int start, int end) {
		super(start, end);
	}

	@Override
	public NodeKind getKind() {
		return NodeKind.ParameterDeclaration;
	}

	public int getStartContent() {
		return startContent;
	}

	void setStartContent(int startContent) {
		this.startContent = startContent;
	}

	public int getEndContent() {
		return endContent;
	}

	void setEndContent(int endContent) {
		this.endContent = endContent;
	}
	
	public String getNodeName() {
		return "#parameter-declaration";
	}
}
