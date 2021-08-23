package com.redhat.qute.parser;

public class Comment extends Node {

	private int startContent;

	private int endContent;

	Comment(int start, int end) {
		super(start, end);
	}

	@Override
	public NodeKind getKind() {
		return NodeKind.Comment;
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
	
	@Override
	public String getNodeName() {
		return "#comment";
	}
}
