package com.redhat.qute.services.definition;

import static com.redhat.qute.QuteAssert.ll;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testDefinitionFor;

import org.junit.jupiter.api.Test;

public class QuteDefinitionInExpressionTest {

	@Test
	public void definitionInUndefinedVariable() throws Exception {
		String template = "{i|tem}";
		testDefinitionFor(template);
	}

	@Test
	public void definitionInDefinedVariable() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{i|tem}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(1, 1, 1, 5), r(0, 16, 0, 20)));

		template = "{@org.acme.Item item}\r\n" + //
				"{item|}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(1, 1, 1, 5), r(0, 16, 0, 20)));
	}

	@Test
	public void definitionInDefinedProperty() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.nam|e}";
		testDefinitionFor(template, //
				ll("org/acme/Item.java",  r(1, 6, 1, 10), r(0, 0, 0, 0)));

		template = "{@org.acme.Item item}\r\n" + //
				"{item.name|}";
		testDefinitionFor(template, //
				ll("org/acme/Item.java",  r(1, 6, 1, 10), r(0, 0, 0, 0)));
	}
}
