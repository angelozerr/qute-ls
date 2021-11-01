package com.redhat.qute.commons.datamodel;

import java.util.List;

import com.redhat.qute.commons.ValueResolver;

public class ProjectDataModel<T extends TemplateDataModel<?>> {

	private List<T> templates;

	private List<ValueResolver> valueResolvers;

	public List<T> getTemplates() {
		return templates;
	}

	public void setTemplates(List<T> templates) {
		this.templates = templates;
	}

	public List<ValueResolver> getValueResolvers() {
		return valueResolvers;
	}

	public void setValueResolvers(List<ValueResolver> valueResolvers) {
		this.valueResolvers = valueResolvers;
	}
}
