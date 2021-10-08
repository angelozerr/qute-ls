/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.commons.datamodel;

import java.util.Set;

/**
 * The MicroProfile project properties change event.
 *
 * @author Angelo ZERR
 *
 */
public class JavaDataModelChangeEvent {

	private Set<String> projectURIs;

	/**
	 * Returns the project URIs impacted by the type scope changed.
	 *
	 * @return the project URIs impacted by the type scope changed.
	 */
	public Set<String> getProjectURIs() {
		return projectURIs;
	}

	/**
	 * Set the project URIs impacted by the type scope changed.
	 *
	 * @param projectURIs the project URIs impacted by the type scope changed.
	 */
	public void setProjectURIs(Set<String> projectURIs) {
		this.projectURIs = projectURIs;
	}

}
