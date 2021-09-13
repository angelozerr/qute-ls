package com.redhat.qute.parser.parameter.scanner;

import com.redhat.qute.parser.scanner.AbstractScanner;
import com.redhat.qute.parser.scanner.Scanner;

public class ParameterScanner extends AbstractScanner<TokenType, ScannerState> {

	public static Scanner<TokenType, ScannerState> createScanner(String input) {
		return createScanner(input, 0);
	}

	public static Scanner<TokenType, ScannerState> createScanner(String input, int initialOffset) {
		return createScanner(input, initialOffset, ScannerState.WithinParameter);
	}

	public static Scanner<TokenType, ScannerState> createScanner(String input, int initialOffset,
			ScannerState initialState) {
		return new ParameterScanner(input, initialOffset, initialState);
	}

	ParameterScanner(String input, int initialOffset, ScannerState initialState) {
		super(input, initialOffset, initialState, TokenType.Unknown, TokenType.EOS);
	}

	@Override
	protected TokenType internalScan() {
		int offset = stream.pos();
		if (stream.eos()) {
			return finishToken(offset, TokenType.EOS);
		}

		String errorMessage = null;
		switch (state) {

		case WithinParameter: {
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}
			stream.advanceUntilChars(' ');
			state = ScannerState.AfterParameterName;
			return finishToken(offset, TokenType.ParameterName);
		}

		case AfterParameterName: {
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}
			stream.advanceUntilChars(' ');
			state = ScannerState.AfterParameterValue;
			return finishToken(offset, TokenType.ParameterValue);
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
