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
public class QuteCompletionInExpressionWithEachSectionTest {

	@Test
	public void objectPart() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each items}\r\n" + //
				"		{|}\r\n" + //
				"{/each}";
		testCompletionFor(template, //
				c("items", "items", r(3, 3, 3, 3)), //
				c("it", "it", r(3, 3, 3, 3)), //
				c("count", "count", r(3, 3, 3, 3)));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each items}\r\n" + //
				"		{i|}\r\n" + //
				"{/each}";
		testCompletionFor(template, //
				c("items", "items", r(3, 3, 3, 4)), //
				c("it", "it", r(3, 3, 3, 4)), //
				c("count", "count", r(3, 3, 3, 4)));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each items}\r\n" + //
				"		{|i}\r\n" + //
				"{/each}";
		testCompletionFor(template, //
				c("items", "items", r(3, 3, 3, 3)), //
				c("it", "it", r(3, 3, 3, 3)), //
				c("count", "count", r(3, 3, 3, 3)));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each items}\r\n" + //
				"		{i|t}\r\n" + //
				"{/each}";
		testCompletionFor(template, //
				c("items", "items", r(3, 3, 3, 5)), //
				c("it", "it", r(3, 3, 3, 5)), //
				c("count", "count", r(3, 3, 3, 5)));
	}

	@Test
	public void propertyPart() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each items}\r\n" + //
				"		{it.|}\r\n" + //
				"{/each}";
		testCompletionFor(template, //
				c("name", "name", r(3, 6, 3, 6)), //
				c("price", "price", r(3, 6, 3, 6)));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each items}\r\n" + //
				"		{it.n|}\r\n" + //
				"{/each}";
		testCompletionFor(template, //
				c("name", "name", r(3, 6, 3, 7)), //
				c("price", "price", r(3, 6, 3, 7)));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each items}\r\n" + //
				"		{it.|n}\r\n" + //
				"{/each}";
		testCompletionFor(template, //
				c("name", "name", r(3, 6, 3, 7)), //
				c("price", "price", r(3, 6, 3, 7)));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each items}\r\n" + //
				"		{it.n|a}\r\n" + //
				"{/each}";
		testCompletionFor(template, //
				c("name", "name", r(3, 6, 3, 8)), //
				c("price", "price", r(3, 6, 3, 8)));
	}

	@Test
	public void expressionInsideIf() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each items}\r\n" + //
				"	{#if it.price > 0}\r\n" + //
				"		{|}\r\n" + //
				"	{/if}	    \r\n" + //
				"{/each}";
		testCompletionFor(template, //
				c("items", "items", r(4, 3, 4, 3)), //
				c("it", "it", r(4, 3, 4, 3)), //
				c("count", "count", r(4, 3, 4, 3)));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each items}\r\n" + //
				"	{#if it.price > 0}\r\n" + //
				"		{it.|}\r\n" + //
				"	{/if}	    \r\n" + //
				"{/each}";
		testCompletionFor(template, //
				c("name", "name", r(4, 6, 4, 6)), //
				c("price", "price", r(4, 6, 4, 6)));

	}
}