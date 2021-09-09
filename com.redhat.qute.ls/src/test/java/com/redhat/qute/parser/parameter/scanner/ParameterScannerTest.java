package com.redhat.qute.parser.parameter.scanner;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.redhat.qute.parser.scanner.Scanner;

public class ParameterScannerTest {

	 private Scanner<TokenType, ScannerState> scanner;

	@Test
	  public void testNestedElement() {
	    scanner = ParameterScanner.createScanner("org.acme.Foo foo");
	    
	    assertOffsetAndToken(0, TokenType.ParameterName, "org.acme.Foo");
	    assertOffsetAndToken(12, TokenType.Whitespace, " ");
	    assertOffsetAndToken(13, TokenType.ParameterValue, "foo");
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
