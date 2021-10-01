package com.redhat.qute.services.hover;

import org.eclipse.lsp4j.Position;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.services.AbstractPositionRequest;
import com.redhat.qute.settings.SharedSettings;

public class HoverRequest extends AbstractPositionRequest {

	public HoverRequest(Template template, Position position, SharedSettings settings) throws BadLocationException {
		super(template, position);
	}

	@Override
	protected Node findNodeAt(Template template, int offset) {
		Node node =  super.findNodeAt(template, offset);
		if (node != null && node.getKind()== NodeKind.Expression) {
			Expression expression = (Expression) node;
			Node expressionNode = expression.findNodeExpressionAt(offset);
			if (expressionNode != null) {
				return expressionNode;
			}
		}
		return node;
	}
}
