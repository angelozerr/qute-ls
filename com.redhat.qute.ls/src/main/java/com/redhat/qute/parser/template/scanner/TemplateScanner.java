package com.redhat.qute.parser.template.scanner;

import java.util.function.Predicate;

import com.redhat.qute.parser.scanner.AbstractScanner;
import com.redhat.qute.parser.scanner.Scanner;

public class TemplateScanner extends AbstractScanner<TokenType, ScannerState> {

	private static final Predicate<Integer> TAG_NAME_PREDICATE = ch -> {
		return Character.isLetter(ch);
	};

	public static Scanner<TokenType, ScannerState> createScanner(String input) {
		return createScanner(input, 0);
	}

	public static Scanner<TokenType, ScannerState> createScanner(String input, int initialOffset) {
		return createScanner(input, initialOffset, ScannerState.WithinContent);
	}

	public static Scanner<TokenType, ScannerState> createScanner(String input, int initialOffset,
			ScannerState initialState) {
		return new TemplateScanner(input, initialOffset, initialState);
	}

	TemplateScanner(String input, int initialOffset, ScannerState initialState) {
		super(input, initialOffset, initialState, TokenType.Unknown);
	}

	@Override
	protected TokenType internalScan() {
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
			stream.advanceUntilChar('}', '"', '\'');
			if (stream.peekChar() == '"' || stream.peekChar() == '\'') {
				stream.advance(1);
				state = ScannerState.WithinString;
				return finishToken(stream.pos() - 1, TokenType.StartString);
			}
			return internalScan(); //(offset, TokenType.Expression);
		}

		case WithinString: {
			if (stream.advanceIfAnyOfChars('"', '\'')) {
				state = ScannerState.WithinExpression;
				return finishToken(offset, TokenType.EndString);
			}
			stream.advanceUntilChar('"', '\'');
			return finishToken(offset, TokenType.String);
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

}
