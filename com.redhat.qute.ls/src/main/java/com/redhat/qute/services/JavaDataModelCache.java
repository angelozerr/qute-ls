package com.redhat.qute.services;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Location;

import com.redhat.qute.commons.JavaClassInfo;
import com.redhat.qute.commons.JavaClassMemberInfo;
import com.redhat.qute.commons.QuteJavaClassMembersParams;
import com.redhat.qute.commons.QuteJavaClassesParams;
import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.ls.api.QuteJavaClassMembersProvider;
import com.redhat.qute.ls.api.QuteJavaClassesProvider;
import com.redhat.qute.ls.api.QuteJavaDefinitionProvider;

public class JavaDataModelCache {

	private final QuteJavaClassesProvider classProvider;

	private final QuteJavaClassMembersProvider membersProvider;

	private final QuteJavaDefinitionProvider definitionProvider;

	public JavaDataModelCache(QuteJavaClassesProvider classProvider, QuteJavaClassMembersProvider membersProvider,
			QuteJavaDefinitionProvider definitionProvider) {
		this.classProvider = classProvider;
		this.membersProvider = membersProvider;
		this.definitionProvider = definitionProvider;
	}

	public CompletableFuture<List<JavaClassInfo>> getJavaClasses(QuteJavaClassesParams params) {
		return classProvider.getJavaClasses(params);
	}

	public CompletableFuture<List<JavaClassMemberInfo>> getJavaClassMembers(QuteJavaClassMembersParams params) {
		return membersProvider.getJavaClassMembers(params);
	}

	public CompletableFuture<Location> getJavaDefinition(QuteJavaDefinitionParams params) {
		return definitionProvider.getJavaDefinition(params);
	}
}
