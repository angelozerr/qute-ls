package com.redhat.qute.services;

import java.util.Arrays;
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

public class MockJavaDataModelCache extends JavaDataModelCache {

	public MockJavaDataModelCache() {
		super(createClassProvider(), createMembersProvider(), createDefinitionProvider());
	}

	private static QuteJavaClassesProvider createClassProvider() {
		return new QuteJavaClassesProvider() {

			@Override
			public CompletableFuture<List<JavaClassInfo>> getJavaClasses(QuteJavaClassesParams params) {
				JavaClassInfo a = new JavaClassInfo();
				a.setClassName("org.acme.Foo");
				return CompletableFuture.completedFuture(Arrays.asList(a));
			}
		};
	}

	private static QuteJavaClassMembersProvider createMembersProvider() {
		return new QuteJavaClassMembersProvider() {

			@Override
			public CompletableFuture<List<JavaClassMemberInfo>> getJavaClassMembers(QuteJavaClassMembersParams params) {
				JavaClassMemberInfo info = new JavaClassMemberInfo();
				info.setField("name");
				info.setType("java.lang.String");
				return CompletableFuture.completedFuture(Arrays.asList(info));
			}
		};
	}

	private static QuteJavaDefinitionProvider createDefinitionProvider() {
		return new QuteJavaDefinitionProvider() {
			@Override
			public CompletableFuture<Location> getJavaDefinition(QuteJavaDefinitionParams params) {
				return QuteJavaDefinitionProvider.super.getJavaDefinition(params);
			}
		};
	}
}
