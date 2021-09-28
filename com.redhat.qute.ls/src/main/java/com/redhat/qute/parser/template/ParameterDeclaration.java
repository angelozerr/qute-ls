package com.redhat.qute.parser.template;

public class ParameterDeclaration extends Node implements ParametersContainer, JavaTypeInfoProvider {

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
		int classNameStart = getClassNameStart();
		int classNameEnd = getClassNameEnd();
		return template.getText(classNameStart, classNameEnd);
	}

	public int getClassNameStart() {
		return getStartContent();
	}

	public int getClassNameEnd() {
		Template template = getOwnerTemplate();
		String text = template.getText();
		for (int i = getStartContent(); i < getEndContent(); i++) {
			char c = text.charAt(i);
			if (c == ' ') {
				return i;
			}
		}
		return getEndContent();
	}

	public boolean isInClassName(int offset) {
		int classNameStart = getClassNameStart();
		int classNameEnd = getClassNameEnd();
		return offset >= classNameStart && offset <= classNameEnd;
	}

	public String getAlias() {
		int aliasStart = getAliasStart();
		if (aliasStart == -1) {
			return null;
		}
		int aliasEnd = getAliasEnd();
		Template template = getOwnerTemplate();
		return template.getText(aliasStart, aliasEnd);
	}

	public int getAliasStart() {
		Template template = getOwnerTemplate();
		String text = template.getText();
		for (int i = getStartContent(); i < getEndContent(); i++) {
			char c = text.charAt(i);
			if (c == ' ' && (i + 1) < getEndContent()) {
				return i + 1;
			}
		}
		return -1;
	}

	public int getAliasEnd() {
		return getEndContent();
	}

	@Override
	public Node getNode() {
		return this;
	}

	@Override
	public int getStartParametersOffset() {
		return getStartContent();
	}

	@Override
	public int getEndParametersOffset() {
		return getEndContent();
	}
}
