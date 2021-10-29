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
 * 
 * @author azerr
 *
 */
public class QuteDiagnosticsInExpressionWithLetSectionTest {

	@Test
	public void definedVariable() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"\r\n" + //
				"{#set name=item.name age=10 long=10L negDouble=-10D isActive=true simpleQuote='abcd' doubleQuote=\"efgh\"}\r\n"
				+ //
				"  {true}\r\n" + //
				"  {name}\r\n" + //
				"  {negDouble}\r\n" + //
				"  {isActive}\r\n" + //
				"  {simpleQuote}\r\n" + //
				"  {doubleQuote}\r\n" + //
				"{/set}\r\n" + //
				"";
		testDiagnosticsFor(template);
	}

	@Test
	public void undefinedVariable() throws Exception {
		String template = "{#set name=item.name age=10 long=10L negDouble=-10D isActive=true simpleQuote='abcd' doubleQuote=\"efgh\"}\r\n"
				+ //
				"  {true}\r\n" + //
				"  {name}\r\n" + //
				"  {negDouble}\r\n" + //
				"  {isActive}\r\n" + //
				"  {simpleQuote}\r\n" + //
				"  {doubleQuote}\r\n" + //
				"{/set}\r\n" + //
				"";
		testDiagnosticsFor(template, //
				d(0, 11, 0, 15, QuteErrorCode.UndefinedVariable, "`item` cannot be resolved to a variable.",
						DiagnosticSeverity.Warning), //
				d(2, 3, 2, 7, QuteErrorCode.UnkwownType, "`name` cannot be resolved to a type.",
						DiagnosticSeverity.Error));
	}

}
