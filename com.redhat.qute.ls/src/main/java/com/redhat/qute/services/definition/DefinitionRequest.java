package com.redhat.qute.services.definition;

import org.eclipse.lsp4j.Position;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.services.AbstractPositionRequest;

public class DefinitionRequest extends AbstractPositionRequest {

	public DefinitionRequest(Template template, Position position) throws BadLocationException {
		super(template, position);
	}

	@Override
	protected Node doFindNodeAt(Template template, int offset) {
		return template.findNodeAt(offset);
	}
}
