package com.redhat.qute.indexing;

import java.util.concurrent.CompletableFuture;

import com.redhat.qute.parser.template.Template;

public interface TemplateProvider {

	CompletableFuture<Template> getTemplate();
	
	String getProjectUri();
	
	String getTemplateId();
	
}
