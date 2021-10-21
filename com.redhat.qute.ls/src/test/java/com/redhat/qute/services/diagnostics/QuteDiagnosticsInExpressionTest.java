package com.redhat.qute.services.diagnostics;

import static com.redhat.qute.QuteAssert.d;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

public class QuteDiagnosticsInExpressionTest {

	@Test
	public void undefinedVariable() throws Exception {
		String template = "{item}";
		testDiagnosticsFor(template, //
				d(0, 1, 0, 5, QuteErrorCode.UndefinedVariable, "`item` cannot be resolved to a variable.",
						DiagnosticSeverity.Warning));
	}

	@Test
	public void definedVariableWithParameterDeclaration() {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item}";
		testDiagnosticsFor(template);
	}

	@Test
	public void unkwownProperty() {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.XXXX}";
		testDiagnosticsFor(template, //
				d(1, 6, 1, 10, QuteErrorCode.UnkwownProperty,
						"`XXXX` cannot be resolved or is not a field for `org.acme.Item` Java type.",
						DiagnosticSeverity.Error));

		template = "{@org.acme.Item item}\r\n" + //
				"{item.XXXX.YYYY}";
		testDiagnosticsFor(template, //
				d(1, 6, 1, 10, QuteErrorCode.UnkwownProperty,
						"`XXXX` cannot be resolved or is not a field for `org.acme.Item` Java type.",
						DiagnosticSeverity.Error));

		template = "{@org.acme.Item item}\r\n" + //
				"{item.name.YYYY}";
		testDiagnosticsFor(template, //
				d(1, 11, 1, 15, QuteErrorCode.UnkwownProperty,
						"`YYYY` cannot be resolved or is not a field for `java.lang.String` Java type.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void kwownProperty() {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.name}";
		testDiagnosticsFor(template);

		template = "{@org.acme.Item item}\r\n" + //
				"{item.name.UTF16}";
		testDiagnosticsFor(template);
	}

	@Test
	public void unkwownMethod() {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.XXXX()}";
		testDiagnosticsFor(template, //
				d(1, 6, 1, 10, QuteErrorCode.UnkwownMethod,
						"`XXXX` cannot be resolved or is not a method for `org.acme.Item` Java type.",
						DiagnosticSeverity.Error));

		template = "{@org.acme.Item item}\r\n" + //
				"{item.XXXX().YYYY}";
		testDiagnosticsFor(template, //
				d(1, 6, 1, 10, QuteErrorCode.UnkwownMethod,
						"`XXXX` cannot be resolved or is not a method for `org.acme.Item` Java type.",
						DiagnosticSeverity.Error));

		template = "{@org.acme.Item item}\r\n" + //
				"{item.name.YYYY()}";
		testDiagnosticsFor(template, //
				d(1, 11, 1, 15, QuteErrorCode.UnkwownMethod,
						"`YYYY` cannot be resolved or is not a method for `java.lang.String` Java type.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void kwownMethod() {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.getReview2()}";
		testDiagnosticsFor(template);

		template = "{@org.acme.Item item}\r\n" + //
				"{item.getReview2().average}";
		testDiagnosticsFor(template);

		template = "{@org.acme.Item item}\r\n" + //
				"{item.getReview2}";
		testDiagnosticsFor(template);

		template = "{@org.acme.Item item}\r\n" + //
				"{item.review2}";
		testDiagnosticsFor(template);

		template = "{@org.acme.Item item}\r\n" + //
				"{item.review2.average}";
		testDiagnosticsFor(template);
	}

	@Test
	public void kwownMethodForIterable() {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.size}";
		testDiagnosticsFor(template);

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.size()}";
		testDiagnosticsFor(template);
	}

	@Test
	public void unkwownMethodForIterable() {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.sizeXXX}";
		testDiagnosticsFor(template, //
				d(1, 7, 1, 14, QuteErrorCode.UnkwownProperty,
						"`sizeXXX` cannot be resolved or is not a field for `java.util.List` Java type.",
						DiagnosticSeverity.Error));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.sizeXXX()}";
		testDiagnosticsFor(template, //
				d(1, 7, 1, 14, QuteErrorCode.UnkwownMethod,
						"`sizeXXX` cannot be resolved or is not a method for `java.util.List` Java type.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void unkwownMethodForPrimitive() {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.size.XXX}";
		testDiagnosticsFor(template, d(1, 12, 1, 15, QuteErrorCode.UnkwownProperty,
				"`XXX` cannot be resolved or is not a field for `int` Java type.", DiagnosticSeverity.Error));
	}
}
