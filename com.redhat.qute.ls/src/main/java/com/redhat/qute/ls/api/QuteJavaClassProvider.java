package com.redhat.qute.ls.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

import com.redhat.qute.commons.JavaClassInfo;
import com.redhat.qute.commons.QuteJavaClassParams;

public interface QuteJavaClassProvider {

	@JsonRequest("qute/template/javaClasses")
	default CompletableFuture<List<JavaClassInfo>> getJavaClasses(QuteJavaClassParams params) {
		return CompletableFuture.completedFuture(null);
	}

}
