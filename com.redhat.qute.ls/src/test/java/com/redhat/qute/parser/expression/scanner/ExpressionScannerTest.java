package com.redhat.qute.parser.expression.scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

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

	@Test
	public void testTwoParts() {
		scanner = ExpressionScanner.createScanner("a b");
		assertOffsetAndToken(0, TokenType.ObjectPart, "a");
		assertOffsetAndToken(1, TokenType.Whitespace, " ");
		assertOffsetAndToken(2, TokenType.ObjectPart, "b");
	}

	@Test
	public void testNamespace() {
		scanner = ExpressionScanner.createScanner("data:foo");
		assertOffsetAndToken(0, TokenType.NamespacePart, "data");
		assertOffsetAndToken(4, TokenType.ColonSpace, ":");
		assertOffsetAndToken(5, TokenType.ObjectPart, "foo");
	}

	@Test
	public void testNamespaceWithProperty() {
		scanner = ExpressionScanner.createScanner("data:foo.bar");
		assertOffsetAndToken(0, TokenType.NamespacePart, "data");
		assertOffsetAndToken(4, TokenType.ColonSpace, ":");
		assertOffsetAndToken(5, TokenType.ObjectPart, "foo");
		assertOffsetAndToken(8, TokenType.Dot, ".");
		assertOffsetAndToken(9, TokenType.PropertyPart, "bar");
	}

	@Test
	public void testNamespaceWithMethod() {
		scanner = ExpressionScanner.createScanner("data:foo.bar()");
		assertOffsetAndToken(0, TokenType.NamespacePart, "data");
		assertOffsetAndToken(4, TokenType.ColonSpace, ":");
		assertOffsetAndToken(5, TokenType.ObjectPart, "foo");
		assertOffsetAndToken(8, TokenType.Dot, ".");
		assertOffsetAndToken(9, TokenType.MethodPart, "bar");
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
