/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.settings;

/**
 * Qute validation settings.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteValidationSettings {

	public static final QuteValidationSettings DEFAULT;

	static {
		DEFAULT = new QuteValidationSettings();
		DEFAULT.updateDefault();
	}

	private transient boolean updated;

	private boolean enabled;

	public QuteValidationSettings() {
		setEnabled(true);
	}

	/**
	 * Returns true if the validation is enabled and false otherwise.
	 * 
	 * @return true if the validation is enabled and false otherwise.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Set true if the validation is enabled and false otherwise.
	 * 
	 * @param enabled true if the validation is enabled and false otherwise.
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Update each kind of validation settings with default value if not defined.
	 */
	private void updateDefault() {
		if (updated) {
			return;
		}
		updated = true;
	}

	/**
	 * Update the the validation settings with the given new validation settings.
	 * 
	 * @param newValidation the new validation settings.
	 */
	public void update(QuteValidationSettings newValidation) {
		this.setEnabled(newValidation.isEnabled());
	}
}