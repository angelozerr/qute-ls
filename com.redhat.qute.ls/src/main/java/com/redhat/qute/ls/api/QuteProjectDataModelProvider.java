package com.redhat.qute.ls.api;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

import com.redhat.qute.commons.datamodel.ProjectDataModelInfo;
import com.redhat.qute.commons.datamodel.QuteProjectDataModelParams;

public interface QuteProjectDataModelProvider {

	@JsonRequest("qute/template/project/dataModel")
	default CompletableFuture<ProjectDataModelInfo> getProjectDataModel(QuteProjectDataModelParams params) {
		return CompletableFuture.completedFuture(null);
	}
}
