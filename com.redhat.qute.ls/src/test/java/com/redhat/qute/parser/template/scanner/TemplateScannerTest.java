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
		assertOffsetAndToken(6, TokenType.EOS, "");
	}

	@Test
	public void testExpressionWithString() {
		scanner = TemplateScanner.createScanner("{abc'}'d}");
		assertOffsetAndToken(0, TokenType.StartExpression, "{");
		assertOffsetAndToken(4, TokenType.StartString, "'");
		assertOffsetAndToken(5, TokenType.String, "}");
		assertOffsetAndToken(6, TokenType.EndString, "'");
		assertOffsetAndToken(8, TokenType.EndExpression, "}");
		assertOffsetAndToken(9, TokenType.EOS, "");
	}

	@Test
	public void noExpressionWithSpace() {
		scanner = TemplateScanner.createScanner("{ abcd}");
		assertOffsetAndToken(0, TokenType.Content, "{ abcd}");
		assertOffsetAndToken(7, TokenType.EOS, "");
	}

	@Test
	public void noExpressionWithQuote() {
		scanner = TemplateScanner.createScanner("{\"abcd\"}");
		assertOffsetAndToken(0, TokenType.Content, "{\"abcd\"}");
		assertOffsetAndToken(8, TokenType.EOS, "");
	}

	/**
	 * @see https://quarkus.io/guides/qute-reference#identifiers
	 */
	@Test
	public void identifiersAndTags() {
		scanner = TemplateScanner.createScanner("<html>\r\n" + //
				"<body>\r\n" + // text
				"   {_foo.bar}   \r\n" + // expression
				"   {! comment !}\r\n" + // comment
				"   {  foo}      \r\n" + // text
				"   {{foo}}      \r\n" + // text for first { and expression for {foo}
				"   {\"foo\":true} \r\n" + // text
				"</body>\r\n" + // text
				"</html>"); // text
		assertOffsetAndToken(0, TokenType.Content, "<html>\r\n" + //
				"<body>\r\n" + //
				"   ");
		// {_foo.bar}
		assertOffsetAndToken(19, TokenType.StartExpression, "{");
		assertOffsetAndToken(28, TokenType.EndExpression, "}");
		assertOffsetAndToken(29, TokenType.Content, "   \r\n" + //
				"   ");

		// {! comment !}
		assertOffsetAndToken(37, TokenType.StartComment, "{!");
		assertOffsetAndToken(39, TokenType.Comment, " comment ");
		assertOffsetAndToken(48, TokenType.EndComment, "!}");
		assertOffsetAndToken(50, TokenType.Content, "\r\n" + //
				"   ");

		// { foo}
		assertOffsetAndToken(55, TokenType.Content, "{  foo}      \r\n" + //
				"   ");

		// {{foo}}
		assertOffsetAndToken(73, TokenType.Content, "{");
		assertOffsetAndToken(74, TokenType.StartExpression, "{");
		assertOffsetAndToken(78, TokenType.EndExpression, "}");
		assertOffsetAndToken(79, TokenType.Content, "}      \r\n" +
				"   ");
		
		// {\"foo\":true}		
		assertOffsetAndToken(91, TokenType.Content, "{\"foo\":true} \r\n" + // text
				"</body>\r\n" + // 
				"</html>");
		assertOffsetAndToken(122, TokenType.EOS, "");
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
