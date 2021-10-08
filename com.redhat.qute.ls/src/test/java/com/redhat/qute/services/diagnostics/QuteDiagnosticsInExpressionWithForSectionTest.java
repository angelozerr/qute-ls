package com.redhat.qute.services.diagnostics;

import static com.redhat.qute.QuteAssert.d;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

public class QuteDiagnosticsInExpressionWithForSectionTest {

	@Test
	public void definedVariable() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{item.name}    \r\n" + //
				"{/for}}";
		testDiagnosticsFor(template);
	}

	@Test
	public void unkwownProperty() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{item.nameXXX}    \r\n" + //
				"{/for}}";
		testDiagnosticsFor(template, //
				d(3, 7, 3, 14, QuteErrorCode.UnkwownProperty,
						"`nameXXX` cannot be resolved or is not a field for `org.acme.Item` Java type.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void noIterable() throws Exception {
		String template = "{@org.acme.Item items}\r\n" + // <-- here items is not an iterable Class
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{item.name}    \r\n" + //
				"{/for}}";
		testDiagnosticsFor(template, //
				d(2, 14, 2, 19, QuteErrorCode.NotInstanceOfIterable,
						"`org.acme.Item` is not an instance of `java.lang.Iterable`.", DiagnosticSeverity.Error),
				d(3, 2, 3, 6, QuteErrorCode.UnkwownType, "`org.acme.Item` cannot be resolved to a type.",
						DiagnosticSeverity.Error));
	}
}
