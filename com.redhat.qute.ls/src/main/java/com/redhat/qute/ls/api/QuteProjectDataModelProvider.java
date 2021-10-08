package com.redhat.qute.ls.api;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

import com.redhat.qute.commons.datamodel.ProjectDataModel;
import com.redhat.qute.commons.datamodel.QuteProjectDataModelParams;

public interface QuteProjectDataModelProvider {

	@JsonRequest("qute/template/projectDataModel")
	default CompletableFuture<ProjectDataModel> getProjectDataModel(QuteProjectDataModelParams params) {
		return CompletableFuture.completedFuture(null);
	}
}
