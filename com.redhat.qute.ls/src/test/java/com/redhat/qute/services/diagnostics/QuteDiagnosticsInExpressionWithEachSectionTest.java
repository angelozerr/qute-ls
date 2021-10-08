package com.redhat.qute.services.diagnostics;

import static com.redhat.qute.QuteAssert.d;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

public class QuteDiagnosticsInExpressionWithEachSectionTest {

	@Test
	public void definedVariable() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each items}\r\n" + //
				"	{it.name}    \r\n" + //
				"{/each}";
		testDiagnosticsFor(template);
	}

	@Test
	public void undefinedVariable() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each itemsXXX}\r\n" + //
				"	{it.name}    \r\n" + //
				"{/each}";
		testDiagnosticsFor(template, //
				d(2, 7, 2, 15, QuteErrorCode.UndefinedVariable, //
						"`itemsXXX` cannot be resolved to a variable.", //
						DiagnosticSeverity.Warning), //
				d(3, 2, 3, 4, QuteErrorCode.UnkwownType, //
						"`it` cannot be resolved to a type.", //
						DiagnosticSeverity.Error));
	}

	@Test
	public void unkwownProperty() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each items}\r\n" + //
				"	{it.nameXXX}    \r\n" + //
				"{/each}";
		testDiagnosticsFor(template, //
				d(3, 5, 3, 12, QuteErrorCode.UnkwownProperty,
						"`nameXXX` cannot be resolved or is not a field for `org.acme.Item` Java type.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void noIterable() throws Exception {
		String template = "{@org.acme.Item items}\r\n" + // <-- here items is not an iterable Class
				" \r\n" + //
				"{#each items}\r\n" + //
				"	{it.name}    \r\n" + //
				"{/each}";
		testDiagnosticsFor(template, //
				d(2, 7, 2, 12, QuteErrorCode.NotInstanceOfIterable,
						"`org.acme.Item` is not an instance of `java.lang.Iterable`.", DiagnosticSeverity.Error),
				d(3, 2, 3, 4, QuteErrorCode.UnkwownType, "`org.acme.Item` cannot be resolved to a type.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void metadata() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each items}\r\n" + //
				"	{count}    \r\n" + //
				"{/each}";
		testDiagnosticsFor(template);
	}
}
