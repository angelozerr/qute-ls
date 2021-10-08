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
public class QuteCompletionInExpressionWithForSectionTest {

	@Test
	public void objectPart() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{|}    \r\n" + //
				"{/for}}";
		testCompletionFor(template, //
				c("items", "items", r(3, 2, 3, 2)), //
				c("item", "item", r(3, 2, 3, 2)), //
				c("count", "count", r(3, 2, 3, 2)));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{i|}    \r\n" + //
				"{/for}}";
		testCompletionFor(template, //
				c("items", "items", r(3, 2, 3, 3)), //
				c("item", "item", r(3, 2, 3, 3)), //
				c("count", "count", r(3, 2, 3, 3)));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{|i}    \r\n" + //
				"{/for}}";
		testCompletionFor(template, //
				c("items", "items", r(3, 2, 3, 2)), //
				c("item", "item", r(3, 2, 3, 2)), //
				c("count", "count", r(3, 2, 3, 2)));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{i|t}    \r\n" + //
				"{/for}}";
		testCompletionFor(template, //
				c("items", "items", r(3, 2, 3, 4)), //
				c("item", "item", r(3, 2, 3, 4)), //
				c("count", "count", r(3, 2, 3, 4)));
	}

	@Test
	public void propertyPart() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{item.|}    \r\n" + //
				"{/for}}";
		testCompletionFor(template, //
				c("name", "name", r(3, 7, 3, 7)), //
				c("price", "price", r(3, 7, 3, 7)), //
				c("review", "review", r(3, 7, 3, 7)), //
				c("review2", "review2", r(3, 7, 3, 7)), //
				c("getReview2() : org.acme.Review", "getReview2()", r(3, 7, 3, 7)));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{item.n|}    \r\n" + //
				"{/for}}";
		testCompletionFor(template, //
				c("name", "name", r(3, 7, 3, 8)), //
				c("price", "price", r(3, 7, 3, 8)), //
				c("review", "review", r(3, 7, 3, 8)), //
				c("review2", "review2", r(3, 7, 3, 8)), //
				c("getReview2() : org.acme.Review", "getReview2()", r(3, 7, 3, 8)));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{item.|n}    \r\n" + //
				"{/for}}";
		testCompletionFor(template, //
				c("name", "name", r(3, 7, 3, 8)), //
				c("price", "price", r(3, 7, 3, 8)), //
				c("review", "review", r(3, 7, 3, 8)), //
				c("review2", "review2", r(3, 7, 3, 8)), //
				c("getReview2() : org.acme.Review", "getReview2()", r(3, 7, 3, 8)));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{item.n|a}    \r\n" + //
				"{/for}}";
		testCompletionFor(template, //
				c("name", "name", r(3, 7, 3, 9)), //
				c("price", "price", r(3, 7, 3, 9)), //
				c("review", "review", r(3, 7, 3, 9)), //
				c("review2", "review2", r(3, 7, 3, 9)), //
				c("getReview2() : org.acme.Review", "getReview2()", r(3, 7, 3, 9)));

	}

	@Test
	public void expressionInsideIf() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{#if item.price > 0}\r\n" + //
				"		{|}\r\n" + //
				"	{/if}	    \r\n" + //
				"{/for}";
		testCompletionFor(template, //
				c("items", "items", r(4, 3, 4, 3)), //
				c("item", "item", r(4, 3, 4, 3)), //
				c("count", "count", r(4, 3, 4, 3)));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{#if item.price > 0}\r\n" + //
				"		{item.|}\r\n" + //
				"	{/if}	    \r\n" + //
				"{/for}";
		testCompletionFor(template, //
				c("name", "name", r(4, 8, 4, 8)), //
				c("price", "price", r(4, 8, 4, 8)), //
				c("review", "review", r(4, 8, 4, 8)), //
				c("review2", "review2", r(4, 8, 4, 8)), //
				c("getReview2() : org.acme.Review", "getReview2()", r(4, 8, 4, 8)));

	}
}