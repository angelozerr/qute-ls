package com.redhat.qute.parser.template;

import com.redhat.qute.parser.expression.Part;

public interface JavaTypeInfoProvider {

	String getClassName();
	
	Node getNode();
	
	default Part getPartToResolve() {
		return null;
	}
}
