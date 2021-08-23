package com.redhat.qute.parser.scanner;

import java.util.function.Predicate;

public class QuteScanner implements Scanner {

	private static final Predicate<Integer> TAG_NAME_PREDICATE = ch -> {
		return Character.isLetter(ch);
	};

	public static Scanner createScanner(String input) {
		return createScanner(input, 0);
	}

	public static Scanner createScanner(String input, int initialOffset) {
		return createScanner(input, initialOffset, ScannerState.WithinContent);
	}

	public static Scanner createScanner(String input, int initialOffset, ScannerState initialState) {
		return new QuteScanner(input, initialOffset, initialState);
	}

	private final MultiLineStream stream;
	private ScannerState state;

	private int tokenOffset;

	private TokenType tokenType;
	private String tokenError;

	QuteScanner(String input, int initialOffset, ScannerState initialState) {
		stream = new MultiLineStream(input, initialOffset);
		state = initialState;
		tokenOffset = 0;
		tokenType = TokenType.Unknown;

	}

	public TokenType scan() {
		int offset = stream.pos();
		ScannerState oldState = state;
		TokenType token = internalScan();
		if (token != TokenType.EOS && offset == stream.pos()) {
			log("Scanner.scan has not advanced at offset " + offset + ", state before: " + oldState + " after: "
					+ state);
			stream.advance(1);
			return finishToken(offset, TokenType.Unknown);
		}
		return token;
	}

	private TokenType internalScan() {
		int offset = stream.pos();
		if (stream.eos()) {
			return finishToken(offset, TokenType.EOS);
		}

		String errorMessage = null;
		switch (state) {

		case WithinContent: {
			if (stream.advanceIfChar('{')) {
				if (!stream.eos() && stream.peekChar() == '!') {
					// Comment -> {! This is a comment !}
					state = ScannerState.WithinComment;
					return finishToken(offset, TokenType.StartComment);

				} else if (stream.advanceIfChar('#')) {
					// Section (start) tag -> {#if
					state = ScannerState.AfterOpeningStartTag;
					return finishToken(offset, TokenType.StartTagOpen);
				} else if (stream.advanceIfChar('/')) {
					// Section (end) tag -> {/if}
					state = ScannerState.AfterOpeningEndTag;
					return finishToken(offset, TokenType.EndTagOpen);
				} else if (stream.advanceIfChar('@')) {
					// Parameter declaration -> {@org.acme.Foo foo}
					state = ScannerState.WithinParameterDeclaration;
					return finishToken(offset, TokenType.StartParameterDeclaration);
				} else {
					// Expression
					state = ScannerState.WithinExpression;
					return finishToken(offset, TokenType.StartExpression);
				}
			}
			stream.advanceUntilChar('{');
			return finishToken(offset, TokenType.Content);
		}

		case WithinComment: {
			if (stream.advanceIfChars('!', '}')) {
				state = ScannerState.WithinContent;
				return finishToken(offset, TokenType.EndComment);
			}
			stream.advanceUntilChars('!', '}');
			return finishToken(offset, TokenType.Comment);
		}

		case WithinParameterDeclaration: {
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			if (stream.advanceIfChar('}')) {
				state = ScannerState.WithinContent;
				return finishToken(offset, TokenType.EndParameterDeclaration);
			}
			stream.advanceUntilChars('}');
			return finishToken(offset, TokenType.ParameterDeclaration);
		}

		case WithinExpression: {
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			if (stream.advanceIfChar('}')) {
				state = ScannerState.WithinContent;
				return finishToken(offset, TokenType.EndExpression);
			}
			stream.advanceUntilChars('}');
			return finishToken(offset, TokenType.Expression);
		}

		case AfterOpeningStartTag: {
			if (hasNextTagName()) {
				state = ScannerState.WithinTag;
				return finishToken(offset, TokenType.StartTag);
			}
			if (stream.skipWhitespace()) { // white space is not valid here
				return finishToken(offset, TokenType.Whitespace, "Tag name must directly follow the open bracket.");
			}
			state = ScannerState.WithinTag;
			if (stream.advanceUntilCharOrNewTag('}')) {
				if (stream.peekChar() == '{') {
					state = ScannerState.WithinContent;
				}
				return internalScan();
			}
			return finishToken(offset, TokenType.Unknown);
		}

		case AfterOpeningEndTag:
			if (hasNextTagName()) {
				state = ScannerState.WithinEndTag;
				return finishToken(offset, TokenType.EndTag);
			}
			if (stream.skipWhitespace()) { // white space is not valid here
				return finishToken(offset, TokenType.Whitespace, "Tag name must directly follow the open bracket.");
			}
			state = ScannerState.WithinEndTag;
			if (stream.advanceUntilCharOrNewTag('}')) {
				if (stream.peekChar() == '{') {
					state = ScannerState.WithinContent;
				}
				return internalScan();
			}
			return finishToken(offset, TokenType.Unknown);

		case WithinEndTag:
			if (stream.skipWhitespace()) { // white space is valid here
				return finishToken(offset, TokenType.Whitespace);
			}
			if (stream.advanceIfChar('}')) {
				state = ScannerState.WithinContent;
				return finishToken(offset, TokenType.EndTagClose);
			}
			if (stream.advanceUntilChar('{')) {
				state = ScannerState.WithinContent;
				return internalScan();
			}
			return finishToken(offset, TokenType.Whitespace);

		case WithinTag: {
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			if (stream.advanceIfChar('/')) {
				state = ScannerState.WithinTag;
				if (stream.advanceIfChar('}')) {
					state = ScannerState.WithinContent;
					return finishToken(offset, TokenType.StartTagSelfClose);
				}
				return finishToken(offset, TokenType.Unknown);
			}
			if (stream.advanceIfChar('}')) {
				state = ScannerState.WithinContent;
				return finishToken(offset, TokenType.StartTagClose);
			}

			stream.advanceUntilChars('}');
			return finishToken(offset, TokenType.ParameterTag);
		}

		default:
		}
		stream.advance(1);
		return finishToken(offset, TokenType.Unknown, errorMessage);
	}

	private boolean hasNextTagName() {
		return stream.advanceWhileChar(TAG_NAME_PREDICATE) > 0;
	}

	TokenType finishToken(int offset, TokenType type) {
		return finishToken(offset, type, null);
	}

	TokenType finishToken(int offset, TokenType type, String errorMessage) {
		tokenType = type;
		tokenOffset = offset;
		tokenError = errorMessage;
		return type;
	}

	@Override
	public TokenType getTokenType() {
		return tokenType;
	}

	/**
	 * Starting offset position of the current token
	 * 
	 * @return Starting offset position of the current token
	 */
	@Override
	public int getTokenOffset() {
		return tokenOffset;
	}

	@Override
	public int getTokenLength() {
		return stream.pos() - tokenOffset;
	}

	@Override
	/**
	 * Ending offset position of the current token
	 * 
	 * @return Ending offset position of the current token
	 */
	public int getTokenEnd() {
		return stream.pos();
	}

	@Override
	public String getTokenText() {
		return stream.getSource().substring(tokenOffset, stream.pos());
	}

	@Override
	public ScannerState getScannerState() {
		return state;
	}

	@Override
	public String getTokenError() {
		return tokenError;
	}

	private void log(String message) {
		System.err.println(message);
	}
}
