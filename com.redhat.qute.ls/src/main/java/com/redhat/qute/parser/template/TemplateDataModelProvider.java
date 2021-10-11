package com.redhat.qute.parser.template;

import java.util.concurrent.CompletableFuture;

import com.redhat.qute.services.datamodel.ExtendedTemplateDataModel;

public interface TemplateDataModelProvider {

	CompletableFuture<ExtendedTemplateDataModel> getTemplateDataModel(Template template);

}
