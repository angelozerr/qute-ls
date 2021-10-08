package com.redhat.qute.services.hover;

import static com.redhat.qute.QuteAssert.assertHover;
import static com.redhat.qute.QuteAssert.r;

import org.junit.jupiter.api.Test;

public class QuteHoverInExpressionTest {

	@Test
	public void undefinedVariable() throws Exception {
		String template = "{i|tem}";
		assertHover(template);
	}

	@Test
	public void definedVariable() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{i|tem}";
		assertHover(template, //
				"org.acme.Item", r(1, 1, 1, 5));

		template = "{@org.acme.Item item}\r\n" + //
				"{item|}";
		assertHover(template, //
				"org.acme.Item", r(1, 1, 1, 5));
	}

	@Test
	public void undefinedProperty() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.nam|eXXX}";
		assertHover(template);
	}

	@Test
	public void definedProperty() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.nam|e}";
		assertHover(template, //
				"java.lang.String", r(1, 6, 1, 10));

		template = "{@org.acme.Item item}\r\n" + //
				"{item.name|}";
		assertHover(template, //
				"java.lang.String", r(1, 6, 1, 10));
	}

	@Test
	public void undefinedMethod() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.nam|eXXX()}";
		assertHover(template);
	}

	@Test
	public void definedMethod() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.review|2}";
		assertHover(template, //
				"org.acme.Review", r(1, 6, 1, 13));

		template = "{@org.acme.Item item}\r\n" + //
				"{item.getReview|2()}";
		assertHover(template, //
				"org.acme.Review", r(1, 6, 1, 16));
	}
}
