package com.redhat.qute.ls.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

import com.redhat.qute.commons.JavaClassMemberInfo;
import com.redhat.qute.commons.QuteJavaClassMembersParams;

public interface QuteJavaClassMembersProvider {

	@JsonRequest("qute/template/javaClassMembers")
	default CompletableFuture<List<JavaClassMemberInfo>> getJavaClasseMembers(QuteJavaClassMembersParams params) {
		return CompletableFuture.completedFuture(null);
	}

}
