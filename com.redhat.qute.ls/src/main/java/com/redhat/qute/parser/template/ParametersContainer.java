package com.redhat.qute.parser.template;

public interface ParametersContainer {

	int getStartParameterOffset();
	
	int getEndParameterOffset();

	Template getOwnerTemplate();
}
