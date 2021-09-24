package com.redhat.qute.parser.parameter;

import java.util.ArrayList;
import java.util.List;

import com.redhat.qute.parser.CancelChecker;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.parser.expression.scanner.ExpressionScanner;
import com.redhat.qute.parser.parameter.scanner.TokenType;
import com.redhat.qute.parser.parameter.scanner.ParameterScanner;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.ParametersContainer;
import com.redhat.qute.parser.template.Template;

public class ParameterParser {

	private static CancelChecker DEFAULT_CANCEL_CHECKER = () -> {
	};
	
	public static List<Parameter> parse(ParametersContainer expression, CancelChecker cancelChecker) {
		if (cancelChecker == null) {
			cancelChecker = DEFAULT_CANCEL_CHECKER;
		}
		Template template = expression.getOwnerTemplate();
		String text = template.getText();
		int start = expression.getStartParameterOffset();
		int end = expression.getEndParameterOffset();
		ParameterScanner scanner = ParameterScanner.createScanner(text, start, end);
		TokenType token = scanner.scan();
		List<Parameter> parameters = new ArrayList<>();
		Parts currentParts = null;
		while (token != TokenType.EOS ) {
			
			token = scanner.scan();
		}
		return parameters;
	}
}
