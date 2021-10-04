package com.redhat.qute.services.hover;

import org.eclipse.lsp4j.Position;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.services.AbstractPositionRequest;
import com.redhat.qute.settings.SharedSettings;

public class HoverRequest extends AbstractPositionRequest {

	public HoverRequest(Template template, Position position, SharedSettings settings) throws BadLocationException {
		super(template, position);
	}

	@Override
	protected Node doFindNodeAt(Template template, int offset) {
		return template.findNodeAt(offset);
	}
}
