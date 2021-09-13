package com.redhat.qute.parser.expression;

import java.util.ArrayList;
import java.util.List;

import com.redhat.qute.parser.CancelChecker;
import com.redhat.qute.parser.expression.scanner.ExpressionScanner;
import com.redhat.qute.parser.expression.scanner.TokenType;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Template;

public class ExpressionParser {

	private static CancelChecker DEFAULT_CANCEL_CHECKER = () -> {
	};

	public static List<Node> parse(Expression expression, CancelChecker cancelChecker) {
		if (cancelChecker == null) {
			cancelChecker = DEFAULT_CANCEL_CHECKER;
		}
		Template template = expression.getOwnerTemplate();
		String text = template.getText();
		int start = expression.getStart() + 1;
		int end = expression.getEnd();
		ExpressionScanner scanner = ExpressionScanner.createScanner(text, start);
		TokenType token = scanner.scan();
		List<Node> expressionContent = new ArrayList<>();
		Parts parts = null;
		while (token != TokenType.EOS && getAdjustedOffset(scanner.getTokenOffset(), start) <= end) {
			cancelChecker.checkCanceled();
			int tokenOffset = getAdjustedOffset(scanner.getTokenOffset(), start);
			int tokenEnd = getAdjustedOffset(scanner.getTokenEnd(), start);
			switch (token) {
			case Part:
				
				Part part = new Part(tokenOffset, tokenEnd);
				if (parts == null) {
					parts = new Parts(tokenOffset, tokenEnd);
					expressionContent.add(parts);
				}
				parts.addPart(part);
				break;
			case Dot:
				parts.addDot(tokenOffset);
				break;
			default:
				parts = null;
				break;
			}
			token = scanner.scan();
		}
		return expressionContent;
	}
	
	private static int getAdjustedOffset(int offset, int start) {
		return offset;
	}
}
