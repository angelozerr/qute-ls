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
 * Project information where a Qute template belongs to.
 * 
 * @author Angelo ZERR
 *
 */
public class ProjectInfo {

	private String uri;

	private String templateBaseDir;

	public ProjectInfo() {
	}

	public ProjectInfo(String uri, String templateBaseDir) {
		setUri(uri);
		setTemplateBaseDir(templateBaseDir);
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getTemplateBaseDir() {
		return templateBaseDir;
	}

	public void setTemplateBaseDir(String templateBaseDir) {
		this.templateBaseDir = templateBaseDir;
	}
}
