package com.redhat.qute.services.definition;

import static com.redhat.qute.QuteAssert.ll;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testDefinitionFor;

import org.junit.jupiter.api.Test;

import com.redhat.qute.services.MockJavaDataModelCache;

public class QuteDefinitionInParameterDeclarationTest {

	@Test
	public void definitionInAlias() throws Exception {
		String template = "{@org.acme.Item it|em}\r\n";
		testDefinitionFor(template, "test.qute");
	}

	@Test
	public void definitionInUnExistingClass() throws Exception {
		String template = "{@org.ac|me.ItemXXXX item}\r\n";
		testDefinitionFor(template, "test.qute");
	}

	@Test
	public void definitionInExistingClass() throws Exception {
		String template = "{@org.ac|me.Item item}\r\n";
		testDefinitionFor(template, "test.qute", //
				ll("org/acme/Item.java", r(0, 2, 0, 15), MockJavaDataModelCache.JAVA_CLASS_RANGE));

		template = "{@org.acme.Item| item}\r\n";
		testDefinitionFor(template, "test.qute", //
				ll("org/acme/Item.java", r(0, 2, 0, 15), MockJavaDataModelCache.JAVA_CLASS_RANGE));
	}

}
