package com.redhat.qute.parser.expression.scanner;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.redhat.qute.parser.scanner.Scanner;

public class ExpressionScannerTest {

	private Scanner<TokenType, ScannerState> scanner;

	@Test
	public void testObjectPart() {
		scanner = ExpressionScanner.createScanner("item");
		assertOffsetAndToken(0, TokenType.ObjectPart, "item");
	}

	@Test
	public void testObjectAndPropertyPart() {
		scanner = ExpressionScanner.createScanner("item.name");
		assertOffsetAndToken(0, TokenType.ObjectPart, "item");
		assertOffsetAndToken(4, TokenType.Dot, ".");
		assertOffsetAndToken(5, TokenType.PropertyPart, "name");
	}

	public void assertOffsetAndToken(int tokenOffset, TokenType tokenType) {
		TokenType token = scanner.scan();
		assertEquals(tokenOffset, scanner.getTokenOffset());
		assertEquals(tokenType, token);
	}

	public void assertOffsetAndToken(int tokenOffset, TokenType tokenType, String tokenText) {
		TokenType token = scanner.scan();
		assertEquals(tokenOffset, scanner.getTokenOffset());
		assertEquals(tokenType, token);
		assertEquals(tokenText, scanner.getTokenText());
	}
}
