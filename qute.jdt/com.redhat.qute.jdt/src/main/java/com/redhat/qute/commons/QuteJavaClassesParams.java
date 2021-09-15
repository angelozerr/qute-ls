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
package com.redhat.qute.commons;

/**
 * Qute Java classes parameters used to collect Java classes, packages.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteJavaClassesParams {

	private String projectUri;

	private String pattern;

	public QuteJavaClassesParams() {
	}

	public QuteJavaClassesParams(String pattern, String projectUri) {
		setPattern(pattern);
		setProjectUri(projectUri);
	}

	public String getProjectUri() {
		return projectUri;
	}

	public void setProjectUri(String uri) {
		this.projectUri = uri;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
}
