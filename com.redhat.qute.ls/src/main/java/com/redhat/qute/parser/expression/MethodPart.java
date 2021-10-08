package com.redhat.qute.parser.expression;

import com.redhat.qute.parser.expression.Parts.PartKind;

public class MethodPart extends MemberPart {

	public MethodPart(int start, int end) {
		super(start, end);
	}

	public PartKind getPartKind() {
		return PartKind.Method;
	}

	public void setOpenBracket(int tokenOffset) {
		// TODO Auto-generated method stub
		
	}

	public void setCloseBracket(int tokenOffset) {
		// TODO Auto-generated method stub
		
	}

}
