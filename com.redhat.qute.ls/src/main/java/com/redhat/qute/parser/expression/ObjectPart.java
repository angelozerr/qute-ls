package com.redhat.qute.parser.expression;

import com.redhat.qute.parser.expression.Parts.PartKind;
import com.redhat.qute.parser.template.ParameterDeclaration;
import com.redhat.qute.parser.template.Template;

public class ObjectPart extends Part {

	public ObjectPart(int start, int end) {
		super(start, end);
	}

	@Override
	public PartKind getPartKind() {
		return PartKind.Object;
	}

	@Override
	public String getClassName() {
		String alias = super.getTextContent();
		Template template = super.getOwnerTemplate();
		ParameterDeclaration parameter = template.findParameterByAlias(alias);
		return parameter != null ? parameter.getClassName() : null;
	}

}
