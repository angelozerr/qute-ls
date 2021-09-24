package com.redhat.qute.parser.parameter.scanner;

import com.redhat.qute.parser.scanner.AbstractScanner;

public class ParameterScanner extends AbstractScanner<TokenType, ScannerState> {

	public static ParameterScanner createScanner(String input) {
		return createScanner(input, 0, -1);
	}

	public static ParameterScanner createScanner(String input, int initialOffset, int endOffset) {
		return createScanner(input, initialOffset, endOffset, ScannerState.WithinParameter);
	}

	public static ParameterScanner createScanner(String input, int initialOffset, int endOffset,
			ScannerState initialState) {
		return new ParameterScanner(input, initialOffset, endOffset, initialState);
	}

	private int endOffset;

	ParameterScanner(String input, int initialOffset, int endOffset, ScannerState initialState) {
		super(input, initialOffset, initialState, TokenType.Unknown, TokenType.EOS);
		this.endOffset = endOffset;
	}

	@Override
	protected TokenType internalScan() {
		int offset = stream.pos();
		if (stream.eos() || (endOffset != -1 && offset >= endOffset)) {
			return finishToken(offset, TokenType.EOS);
		}

		String errorMessage = null;
		switch (state) {

		case WithinParameter: {
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}
			state = ScannerState.AfterParameterName;
			return finishToken(offset, TokenType.ParameterName);
		}

		case AfterParameterName: {
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}
			if (stream.advanceIfChar('=')) {
				state = ScannerState.AfterAssign;	
				return finishToken(offset, TokenType.Assign);
			}
			stream.advanceUntilChars(' ');
			state = ScannerState.AfterParameterValue;
			return finishToken(offset, TokenType.ParameterValue);
		}
		
		case AfterAssign: {
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}
		}

		case AfterParameterValue: {
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			while (!(stream.eos())) {
				stream.advance(1);
			}
			return finishToken(offset, TokenType.Unknown);
		}

		default:
		}
		stream.advance(1);
		return finishToken(offset, TokenType.Unknown, errorMessage);
	}

}
