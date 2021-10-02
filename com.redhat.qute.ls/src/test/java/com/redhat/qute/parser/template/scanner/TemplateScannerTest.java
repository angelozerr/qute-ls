package com.redhat.qute.parser.template.scanner;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.redhat.qute.parser.scanner.Scanner;

public class TemplateScannerTest {

	 private Scanner<TokenType, ScannerState> scanner;

	@Test
	  public void testExpression() {
	    scanner = TemplateScanner.createScanner("{abcd}");
	    assertOffsetAndToken(0, TokenType.StartExpression, "{");
	    assertOffsetAndToken(5, TokenType.EndExpression, "}");
	 }
	 
	@Test
	  public void testExpressionWithString() {
	    scanner = TemplateScanner.createScanner("{abc'}'d}");
	    assertOffsetAndToken(0, TokenType.StartExpression, "{");
	    assertOffsetAndToken(4, TokenType.StartString, "'");
	    assertOffsetAndToken(5, TokenType.String, "}");
	    assertOffsetAndToken(6, TokenType.EndString, "'");
	    assertOffsetAndToken(8, TokenType.EndExpression, "}");
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
