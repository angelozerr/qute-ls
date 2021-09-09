package com.redhat.qute.parser.template;

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

	public String getClassName() {
		Template template = getOwnerTemplate();
		String text = template.getText();
		StringBuilder className = new StringBuilder();
		for (int i = getStartContent(); i < getEndContent(); i++) {
			char c = text.charAt(i);
			if (c == ' ') {
				break;
			}
			className.append(c);
		}
		return className.toString();
	}

	public boolean isInClassName(int offset) {
		Template template = getOwnerTemplate();
		String text = template.getText();
		int i = getStartContent();
		for (; i < getEndContent(); i++) {
			char c = text.charAt(i);
			if (c == ' ') {
				i--;
				break;
			}
		}
		return offset <= i;
	}

	public String getAlias() {
		Template template = getOwnerTemplate();
		String text = template.getText();
		StringBuilder alias = new StringBuilder();
		int spaceIndex = -1;
		for (int i = getStartContent(); i < getEndContent(); i++) {
			char c = text.charAt(i);
			if (spaceIndex == -1) {
				if (c == ' ') {
					spaceIndex = i;
				}
			} else {
				alias.append(c);
			}
		}
		return alias.toString();
	}
}
