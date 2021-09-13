package com.redhat.qute.parser.scanner;

import com.redhat.qute.parser.template.scanner.TokenType;

public abstract class AbstractScanner<T, S> implements Scanner<T, S> {

	protected final MultiLineStream stream;

	private final T unknownTokenType;
	
	private final T eosTokenType;

	protected S state;

	private int tokenOffset;

	private T tokenType;
	private String tokenError;

	protected AbstractScanner(String input, int initialOffset, S initialState, T unknownTokenType, T eosTokenType) {
		stream = new MultiLineStream(input, initialOffset);
		this.unknownTokenType = unknownTokenType;
		this.eosTokenType = eosTokenType;
		state = initialState;
		tokenOffset = 0;
		tokenType = unknownTokenType;
	}

	@Override
	public T scan() {
		int offset = stream.pos();
		S oldState = state;
		T token = internalScan();
		if (token != eosTokenType && offset == stream.pos()) {
			log("Scanner.scan has not advanced at offset " + offset + ", state before: " + oldState + " after: "
					+ state);
			stream.advance(1);
			return finishToken(offset, unknownTokenType);
		}
		return token;
	}

	protected abstract T internalScan();

	protected T finishToken(int offset, T type) {
		return finishToken(offset, type, null);
	}

	protected T finishToken(int offset, T type, String errorMessage) {
		tokenType = type;
		tokenOffset = offset;
		tokenError = errorMessage;
		return type;
	}

	@Override
	public T getTokenType() {
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
	public S getScannerState() {
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
