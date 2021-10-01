package com.redhat.qute.ls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.Location;

import com.redhat.qute.commons.JavaClassInfo;
import com.redhat.qute.commons.JavaClassMemberInfo;
import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.QuteJavaClassesParams;
import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.commons.QuteProjectParams;
import com.redhat.qute.commons.QuteResolvedJavaClassParams;
import com.redhat.qute.commons.ResolvedJavaClassInfo;
import com.redhat.qute.services.JavaDataModelCache;

public class MockJavaDataModelCache extends JavaDataModelCache {

	private final Map<String, ResolvedJavaClassInfo> resolvedClassesCache;

	private final Map<String, ResolvedJavaClassInfo> resolvedClassesCache2;

	public MockJavaDataModelCache() {
		super(null, null, null, null);
		this.resolvedClassesCache = createResolvedClasses();
		resolvedClassesCache2 = new HashMap<>();
	}

	@Override
	protected CompletableFuture<List<JavaClassInfo>> getJavaClasses(QuteJavaClassesParams params) {
		return CompletableFuture.completedFuture(resolvedClassesCache.values() //
				.stream() //
				.collect(Collectors.toList()));
	}

	@Override
	protected CompletableFuture<ResolvedJavaClassInfo> getResolvedJavaClass(QuteResolvedJavaClassParams params) {
		ResolvedJavaClassInfo javaClassInfo = resolvedClassesCache2.get(params.getClassName());
		if (javaClassInfo != null) {
			return CompletableFuture.completedFuture(javaClassInfo);
		}
		return CompletableFuture.supplyAsync(() -> {
			try {
				Thread.sleep(2000);
			} catch (Exception e) {

			}
			ResolvedJavaClassInfo javaClassInfo2 = resolvedClassesCache.get(params.getClassName());
			if (javaClassInfo2 != null) {
				resolvedClassesCache2.put(params.getClassName(), javaClassInfo2);
			}
			return resolvedClassesCache2.get(params.getClassName());
		});
		// return
		// CompletableFuture.completedFuture(resolvedClassesCache.get(params.getClassName()));
	}

	@Override
	protected CompletableFuture<Location> getJavaDefinition(QuteJavaDefinitionParams params) {
		return super.getJavaDefinition(params);
	}

	protected Map<String, ResolvedJavaClassInfo> createResolvedClasses() {
		Map<String, ResolvedJavaClassInfo> cache = new HashMap<>();

		createResolvedJavaClassInfo("org.acme", cache).setUri(null);

		ResolvedJavaClassInfo review = createResolvedJavaClassInfo("org.acme.Review", cache);
		registerMember("name", null, "java.lang.String", review);
		registerMember("average", null, "java.lang.Integer", review);

		ResolvedJavaClassInfo item = createResolvedJavaClassInfo("org.acme.Item", cache);
		registerMember("name", null, "java.lang.String", item);
		registerMember("price", null, "java.math.BigInteger", item);
		registerMember("review", null, "org.acme.Review", item);
		registerMember(null, "getReview()", "org.acme.Review", item);

		createResolvedJavaClassInfo("java.util.List<org.acme.Item>", "java.util.List", "org.acme.Item", cache);
		ResolvedJavaClassInfo list = createResolvedJavaClassInfo("java.util.List", cache);
		registerMember("get", null, "java.lang.String", list);

		return cache;
	}

	protected static JavaClassMemberInfo registerMember(String field, String method, String type,
			ResolvedJavaClassInfo resolvedClass) {
		JavaClassMemberInfo member = new JavaClassMemberInfo();
		member.setField(field);
		member.setMethod(method);
		member.setType(type);
		resolvedClass.getMembers().add(member);
		return member;
	}

	protected static ResolvedJavaClassInfo createResolvedJavaClassInfo(String className,
			Map<String, ResolvedJavaClassInfo> cache) {
		return createResolvedJavaClassInfo(className, null, null, cache);
	}

	protected static ResolvedJavaClassInfo createResolvedJavaClassInfo(String className, String iterableType,
			String iterableOf, Map<String, ResolvedJavaClassInfo> cache) {
		ResolvedJavaClassInfo resolvedClass = new ResolvedJavaClassInfo();
		resolvedClass.setUri(className + ".java");
		resolvedClass.setClassName(className);
		resolvedClass.setIterableType(iterableType);
		resolvedClass.setIterableOf(iterableOf);
		resolvedClass.setMembers(new ArrayList<>());
		cache.put(resolvedClass.getClassName(), resolvedClass);
		return resolvedClass;
	}

	@Override
	public CompletableFuture<ProjectInfo> getProjectInfo(QuteProjectParams params) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				Thread.sleep(5000);
			} catch (Exception e) {

			}
			return new ProjectInfo("test-qute");
		});
	}
}
