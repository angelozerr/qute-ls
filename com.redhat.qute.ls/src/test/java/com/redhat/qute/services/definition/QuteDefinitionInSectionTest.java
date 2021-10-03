package com.redhat.qute.services.definition;

import static com.redhat.qute.QuteAssert.ll;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testDefinitionFor;

import org.junit.jupiter.api.Test;

public class QuteDefinitionInSectionTest {

	@Test
	public void definitionInStartTagSection() throws Exception {
		String template = "{#ea|ch items}\r\n" + //
				"		{it.name}\r\n" + //
				"{/each}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(0, 2, 0, 6), r(2, 2, 2, 6)));

		template = "{#each| items}\r\n" + //
				"		{it.name}\r\n" + //
				"{/each}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(0, 2, 0, 6), r(2, 2, 2, 6)));
	}

	@Test
	public void definitionInEndTagSection() throws Exception {
		String template = "{#each items}\r\n" + //
				"		{it.name}\r\n" + //
				"{/ea|ch}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(2, 2, 2, 6), r(0, 2, 0, 6)));

		template = "{#each items}\r\n" + //
				"		{it.name}\r\n" + //
				"{/each|}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(2, 2, 2, 6), r(0, 2, 0, 6)));
	}
}
