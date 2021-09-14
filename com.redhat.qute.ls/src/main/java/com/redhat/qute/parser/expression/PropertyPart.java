package com.redhat.qute.parser.expression;

import com.redhat.qute.parser.expression.Parts.PartKind;

public class PropertyPart extends Part {

	public PropertyPart(int start, int end) {
		super(start, end);
	}

	public PartKind getPartKind() {
		return PartKind.Property;
	}
	
	@Override
	public String getClassName() {
		return null;
	}
}
