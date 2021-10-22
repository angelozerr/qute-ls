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
package com.redhat.qute.parser.template.sections;

import java.io.File;

import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionKind;

/**
 * Include section.
 * 
 * @author Angelo ZERR
 * 
 * @see https://quarkus.io/guides/qute-reference#include_helpers
 */
public class IncludeSection extends Section {

	public static final String TAG = "include";

	private static String[] suffixes = { "", ".html", ".qute.html", ".txt", ".qute.txt" };

	public IncludeSection(int start, int end) {
		super(TAG, start, end);
	}

	@Override
	public SectionKind getSectionKind() {
		return SectionKind.INCLUDE;
	}

	/**
	 * Returns the template file defined in the first parameter of the section and
	 * null otherwise.
	 * 
	 * @return the template file defined in the first parameter of the section and
	 *         null otherwise.
	 */
	public File getLinkedTemplateFile() {
		Parameter includedTemplateId = super.getParameterAt(0);
		if (includedTemplateId == null) {
			return null;
		}

		File templateBaseDir = getOwnerTemplate().getTemplateBaseDir();
		if (templateBaseDir == null) {
			return null;
		}
		String id = includedTemplateId.getValue();
		for (String suffix : suffixes) {
			File includedTemplateFile = new File(templateBaseDir, id + suffix);
			if (includedTemplateFile.exists()) {
				// The template file exists
				return includedTemplateFile;
			}
		}
		// The template file doesn't exists, we return a file to create it if user wants
		// to do that (only available on vscode when Ctrl+Click is processed).
		return new File(templateBaseDir, id + ".qute.html");
	}

}
