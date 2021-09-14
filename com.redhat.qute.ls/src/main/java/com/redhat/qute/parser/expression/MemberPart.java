package com.redhat.qute.parser.expression;

public abstract class MemberPart extends Part {

	public MemberPart(int start, int end) {
		super(start, end);
	}

	@Override
	public String getClassName() {
		return null;
	}

}
