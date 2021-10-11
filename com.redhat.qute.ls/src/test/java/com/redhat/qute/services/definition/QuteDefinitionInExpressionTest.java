package com.redhat.qute.services.definition;

import static com.redhat.qute.QuteAssert.ll;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testDefinitionFor;

import org.junit.jupiter.api.Test;

import com.redhat.qute.services.datamodel.MockJavaDataModelCache;

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
	public void definitionInUndefinedProperty() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.nam|eXXX}";
		testDefinitionFor(template);
	}

	@Test
	public void definitionInDefinedProperty() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.nam|e}";
		testDefinitionFor(template, //
				ll("org/acme/Item.java", r(1, 6, 1, 10), MockJavaDataModelCache.JAVA_FIELD_RANGE));

		template = "{@org.acme.Item item}\r\n" + //
				"{item.name|}";
		testDefinitionFor(template,
				ll("org/acme/Item.java", r(1, 6, 1, 10), MockJavaDataModelCache.JAVA_FIELD_RANGE));
	}

	@Test
	public void definitionInDefinedPropertyGetter() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.revie|w2}";
		testDefinitionFor(template, //
				ll("org/acme/Item.java", r(1, 6, 1, 13), MockJavaDataModelCache.JAVA_METHOD_RANGE));

		template = "{@org.acme.Item item}\r\n" + //
				"{item.review2|}";
		testDefinitionFor(template, //
				ll("org/acme/Item.java", r(1, 6, 1, 13), MockJavaDataModelCache.JAVA_METHOD_RANGE));
	}
}
