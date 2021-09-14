package com.redhat.qute.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	private static final Map<String, ResolvedJavaClassInfo> resolvedClassesCache = createResolvedClasses();

	public MockJavaDataModelCache() {
		super(createClassProvider(), createResolvedClassProvider(), createDefinitionProvider());
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

	private static QuteResolvedJavaClassProvider createResolvedClassProvider() {
		return new QuteResolvedJavaClassProvider() {

			@Override
			public CompletableFuture<ResolvedJavaClassInfo> getResolvedJavaClass(QuteResolvedJavaClassParams params) {
				return CompletableFuture.completedFuture(resolvedClassesCache.get(params.getClassName()));
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

	private static Map<String, ResolvedJavaClassInfo> createResolvedClasses() {
		Map<String, ResolvedJavaClassInfo> cache = new HashMap<>();

		ResolvedJavaClassInfo bar = createResolvedJavaClassInfo("org.acme.Bar", cache);
		registerMember("name", null, "java.lang.String", bar);
		registerMember("price", null, "java.lang.Integer", bar);

		ResolvedJavaClassInfo foo = createResolvedJavaClassInfo("org.acme.Foo", cache);
		registerMember("name", null, "java.lang.String", foo);
		registerMember("bar", null, "org.acme.Bar", foo);
		registerMember(null, "getBar2()", "org.acme.Bar", foo);

		return cache;
	}

	private static JavaClassMemberInfo registerMember(String field, String method, String type,
			ResolvedJavaClassInfo resolvedClass) {
		JavaClassMemberInfo member = new JavaClassMemberInfo();
		member.setField(field);
		member.setMethod(method);
		member.setType(type);
		resolvedClass.getMembers().add(member);
		return member;
	}

	private static ResolvedJavaClassInfo createResolvedJavaClassInfo(String className,
			Map<String, ResolvedJavaClassInfo> cache) {
		ResolvedJavaClassInfo resolvedClass = new ResolvedJavaClassInfo();
		resolvedClass.setClassName(className);
		resolvedClass.setMembers(new ArrayList<>());
		cache.put(resolvedClass.getClassName(), resolvedClass);
		return resolvedClass;
	}
}
