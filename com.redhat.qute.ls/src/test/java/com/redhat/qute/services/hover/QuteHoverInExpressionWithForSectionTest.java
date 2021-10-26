package com.redhat.qute.services.hover;

import static com.redhat.qute.QuteAssert.assertHover;
import static com.redhat.qute.QuteAssert.r;

import org.junit.jupiter.api.Test;

public class QuteHoverInExpressionWithForSectionTest {

	@Test
	public void undefinedVariable() throws Exception {
		String template = "{#for item in ite|ms}\r\n" + //
				"		{it.name}\r\n" + //
				"{/for item in}";
		assertHover(template);
	}

	@Test
	public void definedVariable() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{#for item in ite|ms}\r\n" + //
				"		{item.name}\r\n" + //
				"{/for}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"java.util.List<org.acme.Item>" + //
				System.lineSeparator() + //
				"```", //
				r(1, 14, 1, 19));
	}

	@Test
	public void definedItemVariable() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{#for item in items}\r\n" + //
				"		{ite|m.name}\r\n" + //
				"{/for}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"org.acme.Item" + //
				System.lineSeparator() + //
				"```", //
				r(2, 3, 2, 7));
	}

	@Test
	public void definedItemProperty() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{#for item in items}\r\n" + //
				"		{item.na|me}\r\n" + //
				"{/for}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"String org.acme.Item.name" + //
				System.lineSeparator() + //
				"```", //
				r(2, 8, 2, 12));
	}

	@Test
	public void definedItemMethod() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{#for item in items}\r\n" + //
				"		{item.get|Review2()}\r\n" + //
				"{/for}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"Review org.acme.Item.getReview2()" + //
				System.lineSeparator() + //
				"```", //
				r(2, 8, 2, 18));
	}

	@Test
	public void metadata() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{#for item in items}\r\n" + //
				"		{cou|nt}\r\n" + //
				"{/for}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"java.lang.Integer" + //
				System.lineSeparator() + //
				"```", //
				r(2, 3, 2, 8));
	}
}
