package com.redhat.qute.commons.datamodel;

import java.util.List;

public class ProjectDataModel<T extends TemplateDataModel<?>> {

	private List<T> templates;

	public List<T> getTemplates() {
		return templates;
	}

	public void setTemplates(List<T> templates) {
		this.templates = templates;
	}
}
