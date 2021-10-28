package com.redhat.qute.parser.template;

import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts;

public interface JavaTypeInfoProvider {

	default String getClassName() {
		return null;
	}

	Node getNode();

	default Part getPartToResolve() {
		Expression expression = getExpression();
		if (expression == null) {
			return null;
		}
		Parts parts = (Parts) expression.getExpressionContent().get(0);
		return (Part) parts.getLastChild();
	}

	default Expression getExpression() {
		return null;
	}

}
