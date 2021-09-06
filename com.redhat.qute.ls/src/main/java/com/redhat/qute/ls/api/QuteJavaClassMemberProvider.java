package com.redhat.qute.ls.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

import com.redhat.qute.commons.JavaClassMemberInfo;
import com.redhat.qute.commons.QuteJavaClassMemberParams;

public interface QuteJavaClassMemberProvider {

	@JsonRequest("qute/template/javaClassMembers")
	default CompletableFuture<List<JavaClassMemberInfo>> getJavaClasseMembers(QuteJavaClassMemberParams params) {
		return CompletableFuture.completedFuture(null);
	}

}
