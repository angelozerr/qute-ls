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
package com.redhat.qute.services;

import static com.redhat.qute.QuteAssert.cl;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCodeLensFor;

import org.junit.jupiter.api.Test;

/**
 * Tests for Qute code lens.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteCodeLensTest {

	@Test
	public void noGrammarWithAssociationSupportInDTD() throws Exception {
		String value = "";
		//testCodeLensFor(value, "test.dtd", //
		//		cl(r(0, 0, 0, 0), "test.dtd (xml-model)", OPEN_URI));
	}
}
