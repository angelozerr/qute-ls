package com.redhat.qute.parser.template;

public interface ParametersContainer {

	int getStartParametersOffset();
	
	int getEndParametersOffset();

	Template getOwnerTemplate();
}
