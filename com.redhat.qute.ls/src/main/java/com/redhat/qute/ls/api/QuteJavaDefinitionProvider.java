package com.redhat.qute.ls.api;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

import com.redhat.qute.commons.QuteJavaDefinitionParams;

public interface QuteJavaDefinitionProvider {

	@JsonRequest("qute/template/javaDefinition")
	default CompletableFuture<Location> getJavaDefinition(QuteJavaDefinitionParams params) {
		return CompletableFuture.completedFuture(null);
	}

}
