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

	@Test
	public void definitionInExistingClassInsideList() throws Exception {
		String template = "{@java.util.List<org.ac|me.Item> item}\r\n";
		testDefinitionFor(template, "test.qute", //
				ll("org/acme/Item.java", r(0, 17, 0, 30), MockJavaDataModelCache.JAVA_CLASS_RANGE));

		template = "{@java.util.List<|org.acme.Item> item}\r\n";
		testDefinitionFor(template, "test.qute", //
				ll("org/acme/Item.java", r(0, 17, 0, 30), MockJavaDataModelCache.JAVA_CLASS_RANGE));

		template = "{@java.util.List<org.acme.Item|> item}\r\n";
		testDefinitionFor(template, "test.qute", //
				ll("org/acme/Item.java", r(0, 17, 0, 30), MockJavaDataModelCache.JAVA_CLASS_RANGE));
	}

	@Test
	public void definitionInClassList() throws Exception {
		String template = "{@java.ut|il.List<org.acme.Item> item}\r\n";
		testDefinitionFor(template, "test.qute", //
				ll("java/util/List.java", r(0, 2, 0, 16), MockJavaDataModelCache.JAVA_CLASS_RANGE));

		template = "{@|java.util.List<org.acme.Item> item}\r\n";
		testDefinitionFor(template, "test.qute", //
				ll("java/util/List.java", r(0, 2, 0, 16), MockJavaDataModelCache.JAVA_CLASS_RANGE));

		template = "{@java.util.List|<org.acme.Item> item}\r\n";
		testDefinitionFor(template, "test.qute", //
				ll("java/util/List.java", r(0, 2, 0, 16), MockJavaDataModelCache.JAVA_CLASS_RANGE));
	}
}
