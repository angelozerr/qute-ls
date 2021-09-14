package com.redhat.qute.parser.expression;

import com.redhat.qute.parser.expression.Parts.PartKind;

public class NamespacePart extends Part {

	public NamespacePart(int start, int end) {
		super(start, end);
	}
	
	public PartKind getPartKind() {
		return PartKind.Namespace;
	}

}
