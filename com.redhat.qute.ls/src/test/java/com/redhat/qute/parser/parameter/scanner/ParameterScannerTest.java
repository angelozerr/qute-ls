package com.redhat.qute.parser.parameter.scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.redhat.qute.parser.scanner.Scanner;

public class ParameterScannerTest {

	private Scanner<TokenType, ScannerState> scanner;

	@Test
	public void parameterDeclarations() {
		// {@org.acme.Foo foo}
		scanner = ParameterScanner.createScanner("org.acme.Foo foo");
		assertOffsetAndToken(0, TokenType.ParameterName, "org.acme.Foo");
		assertOffsetAndToken(12, TokenType.Whitespace, " ");
		assertOffsetAndToken(13, TokenType.ParameterName, "foo");
	}

	@Test
	public void eachSection() {
		// {#each items}
		scanner = ParameterScanner.createScanner("items");
		assertOffsetAndToken(0, TokenType.ParameterName, "items");
	}

	@Test
	public void forSection() {
		// {#for item in items}
		scanner = ParameterScanner.createScanner("item in items");
		assertOffsetAndToken(0, TokenType.ParameterName, "item");
		assertOffsetAndToken(4, TokenType.Whitespace, " ");
		assertOffsetAndToken(5, TokenType.ParameterName, "in");
		assertOffsetAndToken(7, TokenType.Whitespace, " ");
		assertOffsetAndToken(8, TokenType.ParameterName, "items");
	}

	@Test
	public void letSection() {
		// {#let myParent=order.item.parent myPrice=order.price}
		scanner = ParameterScanner.createScanner("myParent=order.item.parent myPrice=order.price");
		assertOffsetAndToken(0, TokenType.ParameterName, "myParent");
		assertOffsetAndToken(8, TokenType.Assign, "=");
		assertOffsetAndToken(9, TokenType.ParameterValue, "order.item.parent");
		assertOffsetAndToken(26, TokenType.Whitespace, " ");
		assertOffsetAndToken(27, TokenType.ParameterName, "myPrice");
		assertOffsetAndToken(34, TokenType.Assign, "=");
		assertOffsetAndToken(35, TokenType.ParameterValue, "order.price");
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
