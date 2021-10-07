package com.redhat.qute.parser.expression.scanner;

import java.util.function.Predicate;

import com.redhat.qute.parser.scanner.AbstractScanner;

public class ExpressionScanner extends AbstractScanner<TokenType, ScannerState> {

	private static final Predicate<Integer> JAVA_IDENTIFIER_PART_PREDICATE = ch -> {
		return Character.isJavaIdentifierPart(ch);
	};

	public static ExpressionScanner createScanner(String input) {
		return createScanner(input, 0, input.length());
	}

	public static ExpressionScanner createScanner(String input, int initialOffset, int endOffset) {
		return createScanner(input, initialOffset, endOffset, ScannerState.WithinExpression);
	}

	public static ExpressionScanner createScanner(String input, int initialOffset, int endOffset,
			ScannerState initialState) {
		return new ExpressionScanner(input, initialOffset, endOffset, initialState);
	}

	ExpressionScanner(String input, int initialOffset, int endOffset, ScannerState initialState) {
		super(input, initialOffset, endOffset, initialState, TokenType.Unknown, TokenType.EOS);
	}

	@Override
	protected TokenType internalScan() {
		int offset = stream.pos();
		if (stream.eos()) {
			return finishToken(offset, TokenType.EOS);
		}

		String errorMessage = null;
		switch (state) {

		case WithinExpression: {
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}
			if (hasNextJavaIdentifierPart()) {
				return finishTokenPart(offset);
			}
			return finishToken(offset, TokenType.Unknown);
		}

		case WithinParts:
		case AfterNamespace: {
			if (stream.advanceIfChar('.')) {
				return finishToken(offset, TokenType.Dot);
			}
			if (stream.advanceIfChar(':')) {
				return finishToken(offset, TokenType.ColonSpace);
			}
			if (hasNextJavaIdentifierPart()) {
				return finishTokenPart(offset);
			}
			if (stream.skipWhitespace()) {
				state = ScannerState.WithinExpression;
				return finishToken(offset, TokenType.Whitespace);
			}
			// return internalScan();
		}

		default:
		}
		stream.advance(1);
		return finishToken(offset, TokenType.Unknown, errorMessage);
	}

	private TokenType finishTokenPart(int offset) {
		int next = stream.peekChar();
		if (next == ':') {
			state = ScannerState.AfterNamespace;
			return finishToken(offset, TokenType.NamespacePart);
		}
		if (state == ScannerState.WithinParts) {
			if (next == '(') {
				return finishToken(offset, TokenType.MethodPart);
			}
			return finishToken(offset, TokenType.PropertyPart);
		}
		state = ScannerState.WithinParts;
		return finishToken(offset, TokenType.ObjectPart);
	}

	private boolean hasNextJavaIdentifierPart() {
		return stream.advanceWhileChar(JAVA_IDENTIFIER_PART_PREDICATE) > 0;
	}

}
