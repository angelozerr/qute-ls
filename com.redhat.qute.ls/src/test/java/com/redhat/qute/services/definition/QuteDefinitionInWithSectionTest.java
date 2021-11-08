package com.redhat.qute.services.definition;

import static com.redhat.qute.QuteAssert.ll;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testDefinitionFor;

import org.junit.jupiter.api.Test;

public class QuteDefinitionInWithSectionTest {

	@Test
	public void definedProperty() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#with item}\r\n" + //
				"  <h1>{na|me}</h1>\r\n" + // <-- completion here
				"{/with}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(3, 3, 3, 12), r(0, 37, 0, 46)));
	}
}
