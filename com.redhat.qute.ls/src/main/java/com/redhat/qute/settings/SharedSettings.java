/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.settings;

/**
 * Qute shared settings.
 * 
 * @author Angelo ZERR
 *
 */
public class SharedSettings {
	private final QuteCompletionSettings completionSettings;
	private final QuteFormattingSettings formattingSettings;
	private final QuteValidationSettings validationSettings;

	public SharedSettings() {
		this.completionSettings = new QuteCompletionSettings();
		this.formattingSettings = new QuteFormattingSettings();
		this.validationSettings = new QuteValidationSettings();
	}

	/**
	 * Returns the completion settings.
	 * 
	 * @return the completion settings.
	 */
	public QuteCompletionSettings getCompletionSettings() {
		return completionSettings;
	}

	/**
	 * Returns the formatting settings.
	 * 
	 * @return the formatting settings.
	 */
	public QuteFormattingSettings getFormattingSettings() {
		return formattingSettings;
	}

	/**
	 * Returns the validation settings.
	 * 
	 * @return the validation settings.
	 */
	public QuteValidationSettings getValidationSettings() {
		return validationSettings;
	}

}
