package com.redhat.qute.services.completions;

import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCompletionFor;

import org.junit.jupiter.api.Test;

/**
 * Tests for Qute completion in expression.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteCompletionInExpressionTest {

	@Test
	public void completionInExpressionForObjectPart() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"Item: {|}";
		testCompletionFor(template, //
				c("item", "item", r(1, 7, 1, 7)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {i|}";
		testCompletionFor(template, //
				c("item", "item", r(1, 7, 1, 8)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {|i}";
		testCompletionFor(template, //
				c("item", "item", r(1, 7, 1, 7)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {i|te}";
		testCompletionFor(template, //
				c("item", "item", r(1, 7, 1, 10)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item|}";
		testCompletionFor(template, //
				c("item", "item", r(1, 7, 1, 11)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item. |}";
		testCompletionFor(template, //
				c("item", "item", r(1, 13, 1, 13)));
	}

	@Test
	public void completionInExpressionForPropertyPart() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.|}";
		testCompletionFor(template, //
				c("name", "name", r(1, 12, 1, 12)), //
				c("price", "price", r(1, 12, 1, 12)), //
				c("review", "review", r(1, 12, 1, 12)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.n|}";
		testCompletionFor(template, //
				c("name", "name", r(1, 12, 1, 13)), //
				c("price", "price", r(1, 12, 1, 13)), //
				c("review", "review", r(1, 12, 1, 13)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.|n}";
		testCompletionFor(template, //
				c("name", "name", r(1, 12, 1, 13)), //
				c("price", "price", r(1, 12, 1, 13)), //
				c("review", "review", r(1, 12, 1, 13)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.n|a}";
		testCompletionFor(template, //
				c("name", "name", r(1, 12, 1, 14)), //
				c("price", "price", r(1, 12, 1, 14)), //
				c("review", "review", r(1, 12, 1, 14)));
	}

	@Test
	public void completionInExpressionForSecondPropertyPart() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.review.|}";
		testCompletionFor(template, //
				c("name", "name", r(1, 19, 1, 19)), //
				c("average", "average", r(1, 19, 1, 19)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.review.n|}";
		testCompletionFor(template, //
				c("name", "name", r(1, 19, 1, 20)), //
				c("average", "average", r(1, 19, 1, 20)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.review.|n}";
		testCompletionFor(template, //
				c("name", "name", r(1, 19, 1, 20)), //
				c("average", "average", r(1, 19, 1, 20)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.review.n|a}";
		testCompletionFor(template, //
				c("name", "name", r(1, 19, 1, 21)), //
				c("average", "average", r(1, 19, 1, 21)));
	}
	
	@Test
	public void completionInExpressionWithMethod() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.getReview2().|}";
		testCompletionFor(template, //
				c("name", "name", r(1, 20, 1, 20)), //
				c("average", "average", r(1, 20, 1, 20)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.getReview2().n|}";
		testCompletionFor(template, //
				c("name", "name", r(1, 20, 1, 21)), //
				c("average", "average", r(1, 20, 1, 21)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.getReview2().|n}";
		testCompletionFor(template, //
				c("name", "name", r(1, 20, 1, 21)), //
				c("average", "average", r(1, 20, 1, 21)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.getReview2().n|a}";
		testCompletionFor(template, //
				c("name", "name", r(1, 20, 1, 22)), //
				c("average", "average", r(1, 20, 1, 22)));
	}

	@Test
	public void completionInExpressionWithGetterMethod() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.review2.|}";
		testCompletionFor(template, //
				c("name", "name", r(1, 20, 1, 20)), //
				c("average", "average", r(1, 20, 1, 20)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.review2.n|}";
		testCompletionFor(template, //
				c("name", "name", r(1, 20, 1, 21)), //
				c("average", "average", r(1, 20, 1, 21)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.review2.|n}";
		testCompletionFor(template, //
				c("name", "name", r(1, 20, 1, 21)), //
				c("average", "average", r(1, 20, 1, 21)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.review2.n|a}";
		testCompletionFor(template, //
				c("name", "name", r(1, 20, 1, 22)), //
				c("average", "average", r(1, 20, 1, 22)));
	}
}