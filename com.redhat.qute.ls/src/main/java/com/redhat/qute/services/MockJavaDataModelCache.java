package com.redhat.qute.services;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Location;

import com.redhat.qute.commons.JavaClassInfo;
import com.redhat.qute.commons.JavaClassMemberInfo;
import com.redhat.qute.commons.QuteJavaClassesParams;
import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.commons.QuteResolvedJavaClassParams;
import com.redhat.qute.commons.ResolvedJavaClassInfo;
import com.redhat.qute.ls.api.QuteJavaClassesProvider;
import com.redhat.qute.ls.api.QuteJavaDefinitionProvider;
import com.redhat.qute.ls.api.QuteResolvedJavaClassProvider;

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

	private static QuteResolvedJavaClassProvider createMembersProvider() {
		return new QuteResolvedJavaClassProvider() {

			@Override
			public CompletableFuture<ResolvedJavaClassInfo> getResolvedJavaClass(QuteResolvedJavaClassParams params) {
				JavaClassMemberInfo member = new JavaClassMemberInfo();
				member.setField("name");
				member.setType("java.lang.String");
				List<JavaClassMemberInfo> members = Arrays.asList(member);
				ResolvedJavaClassInfo resolvedClass = new ResolvedJavaClassInfo();
				resolvedClass.setClassName("org.acme.Foo");
				resolvedClass.setMembers(members);
				return CompletableFuture.completedFuture(resolvedClass);
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
