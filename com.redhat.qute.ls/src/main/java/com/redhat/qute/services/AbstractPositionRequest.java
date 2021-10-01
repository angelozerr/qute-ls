package com.redhat.qute.services;

import org.eclipse.lsp4j.Position;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Template;

public abstract class AbstractPositionRequest {

	private final Template template;
	private final int offset;
	private final Node node;

	public AbstractPositionRequest(Template template, Position position) throws BadLocationException {
		this.template = template;
		this.offset = template.offsetAt(position);
		this.node = findNodeAt(template, offset);
		if (node == null) {
			throw new BadLocationException("node is null at offset " + offset);
		}
	}

	protected Node findNodeAt(Template template, int offset) {
		return template.findNodeAt(offset);
	}

	public Template getTemplate() {
		return template;
	}

	public int getOffset() {
		return offset;
	}

	public Node getNode() {
		return node;
	}
}
