package com.redhat.qute.services;

import org.eclipse.lsp4j.Position;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Section;
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

	protected final Node findNodeAt(Template template, int offset) {
		Node node = doFindNodeAt(template, offset);
		if (node == null) {
			return null;
		}
		switch (node.getKind()) {
		case Section: {
			Section section = (Section) node;
			Expression expression = section.getExpressionParameter(offset);
			if (expression != null) {
				Node expressionNode = expression.findNodeExpressionAt(offset);
				if (expressionNode != null) {
					return expressionNode;
				}
				return expression;
			}			
		}
		break;
		case Expression: {
			Expression expression = (Expression) node;
			Node expressionNode = expression.findNodeExpressionAt(offset);
			if (expressionNode != null) {
				return expressionNode;
			}			
		}
		break;
		default:
			return node;
		}
		return node;
	}

	protected abstract Node doFindNodeAt(Template template, int offset);

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
