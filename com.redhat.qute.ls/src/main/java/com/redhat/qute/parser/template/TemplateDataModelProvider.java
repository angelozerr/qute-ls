package com.redhat.qute.parser.template;

import java.util.concurrent.CompletableFuture;

import com.redhat.qute.commons.datamodel.TemplateDataModel;

public interface TemplateDataModelProvider {

	CompletableFuture<TemplateDataModel> getTemplateDataModel(Template template);

}
