package com.redhat.qute.settings;

public class QuteCodeLensSettings {

	private boolean enabled;

	public QuteCodeLensSettings() {
		setEnabled(true);
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

}
