package com.redhat.qute.services.hover;

import static com.redhat.qute.QuteAssert.assertHover;
import static com.redhat.qute.QuteAssert.r;

import org.junit.jupiter.api.Test;

public class QuteHoverInExpressionTest {

	@Test
	public void hoverInUndefinedVariable() throws Exception {
		String template = "{i|tem}";
		assertHover(template);
	}

	@Test
	public void hoverInDefinedVariable() throws Exception {
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
	public void hoverInDefinedProperty() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.nam|e}";
		assertHover(template, //
				"java.lang.String", r(1, 6, 1, 10));
		
		template = "{@org.acme.Item item}\r\n" + //
				"{item.name|}";
		assertHover(template, //
				"java.lang.String", r(1, 6, 1, 10));		
	}
}