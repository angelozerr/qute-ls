/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.diagnostics;

import static com.redhat.qute.QuteAssert.d;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

/**
 * Test with expressions.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteDiagnosticsInExpressionTest {

	@Test
	public void booleanLiteral() throws Exception {
		String template = "{true}";

		testDiagnosticsFor(template);

		template = "{trueX}";
		testDiagnosticsFor(template, //
				d(0, 1, 0, 6, QuteErrorCode.UndefinedVariable, "`trueX` cannot be resolved to a variable.",
						DiagnosticSeverity.Warning));

		template = "{false}";
		testDiagnosticsFor(template);

		template = "{falseX}";
		testDiagnosticsFor(template, //
				d(0, 1, 0, 7, QuteErrorCode.UndefinedVariable, "`falseX` cannot be resolved to a variable.",
						DiagnosticSeverity.Warning));
	}

	@Test
	public void nullLiteral() throws Exception {
		String template = "{null}";
		testDiagnosticsFor(template);

		template = "{nullX}";
		testDiagnosticsFor(template, //
				d(0, 1, 0, 6, QuteErrorCode.UndefinedVariable, "`nullX` cannot be resolved to a variable.",
						DiagnosticSeverity.Warning));
	}

	@Test
	public void stringLiteral() throws Exception {
		String template = "{\"abcd\"}";
		testDiagnosticsFor(template);
	}

	@Test
	public void integerLiteral() throws Exception {
		String template = "{123}";
		testDiagnosticsFor(template);

		template = "{123X}";
		testDiagnosticsFor(template, //
				d(0, 1, 0, 5, QuteErrorCode.UndefinedVariable, "`123X` cannot be resolved to a variable.",
						DiagnosticSeverity.Warning));
	}

	@Test
	public void longLiteral() throws Exception {
		String template = "{123L}";
		testDiagnosticsFor(template);

		template = "{123LX}";
		testDiagnosticsFor(template, //
				d(0, 1, 0, 6, QuteErrorCode.UndefinedVariable, "`123LX` cannot be resolved to a variable.",
						DiagnosticSeverity.Warning));
	}

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
