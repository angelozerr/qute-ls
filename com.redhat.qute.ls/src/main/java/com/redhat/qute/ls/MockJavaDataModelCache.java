package com.redhat.qute.ls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.lsp4j.Location;

import com.redhat.qute.commons.JavaClassInfo;
import com.redhat.qute.commons.JavaFieldInfo;
import com.redhat.qute.commons.JavaMemberInfo;
import com.redhat.qute.commons.JavaMethodInfo;
import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.QuteJavaClassesParams;
import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.commons.QuteProjectParams;
import com.redhat.qute.commons.QuteResolvedJavaClassParams;
import com.redhat.qute.commons.ResolvedJavaClassInfo;
import com.redhat.qute.commons.datamodel.ParameterDataModel;
import com.redhat.qute.commons.datamodel.ProjectDataModel;
import com.redhat.qute.commons.datamodel.QuteProjectDataModelParams;
import com.redhat.qute.commons.datamodel.TemplateDataModel;
import com.redhat.qute.services.datamodel.JavaDataModelCache;

public class MockJavaDataModelCache extends JavaDataModelCache {

	private final Map<String, ResolvedJavaClassInfo> resolvedClassesCache;

	private final Map<String, ResolvedJavaClassInfo> resolvedClassesCache2;

	public MockJavaDataModelCache() {
		super(null, null, null, null, null);
		this.resolvedClassesCache = createResolvedClasses();
		resolvedClassesCache2 = new HashMap<>();
	}

	@Override
	public CompletableFuture<List<JavaClassInfo>> getJavaClasses(QuteJavaClassesParams params) {
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
	public CompletableFuture<Location> getJavaDefinition(QuteJavaDefinitionParams params) {
		return super.getJavaDefinition(params);
	}

	protected Map<String, ResolvedJavaClassInfo> createResolvedClasses() {
		Map<String, ResolvedJavaClassInfo> cache = new HashMap<>();

		createResolvedJavaClassInfo("org.acme", cache).setUri(null);

		ResolvedJavaClassInfo string = createResolvedJavaClassInfo("java.lang.String", cache);
		registerField("UTF16", "byte", string);

		ResolvedJavaClassInfo collection = createResolvedJavaClassInfo("java.util.Collection", cache);
		registerField("get2", "java.lang.String", collection);

		ResolvedJavaClassInfo list = createResolvedJavaClassInfo("java.util.List", cache, collection);
		registerField("get", "java.lang.String", list);

		ResolvedJavaClassInfo review = createResolvedJavaClassInfo("org.acme.Review", cache);
		registerField("name", "java.lang.String", review);
		registerField("average", "java.lang.Integer", review);

		ResolvedJavaClassInfo item = createResolvedJavaClassInfo("org.acme.Item", cache);
		registerField("name", "java.lang.String", item);
		registerField("price", "java.math.BigInteger", item);
		registerField("review", "org.acme.Review", item);
		registerMethod("getReview() : org.acme.Review", item);
		registerMethod("getReviews() : java.util.List<org.acme.Review>", item);
		createResolvedJavaClassInfo("java.util.List<org.acme.Review>", "java.util.List", "org.acme.Review", cache);

		createResolvedJavaClassInfo("java.util.List<org.acme.Item>", "java.util.List", "org.acme.Item", cache);

		return cache;
	}

	protected static JavaMemberInfo registerField(String fieldName, String fieldType,
			ResolvedJavaClassInfo resolvedClass) {
		JavaFieldInfo member = new JavaFieldInfo();
		member.setName(fieldName);
		member.setType(fieldType);
		resolvedClass.getFields().add(member);
		return member;
	}

	protected static JavaMemberInfo registerMethod(String methodSignature, ResolvedJavaClassInfo resolvedClass) {
		JavaMethodInfo member = new JavaMethodInfo();
		member.setSignature(methodSignature);
		resolvedClass.getMethods().add(member);
		return member;
	}

	protected static ResolvedJavaClassInfo createResolvedJavaClassInfo(String className,
			Map<String, ResolvedJavaClassInfo> cache, ResolvedJavaClassInfo... extendedTypes) {
		return createResolvedJavaClassInfo(className, null, null, cache, extendedTypes);
	}

	protected static ResolvedJavaClassInfo createResolvedJavaClassInfo(String className, String iterableType,
			String iterableOf, Map<String, ResolvedJavaClassInfo> cache, ResolvedJavaClassInfo... extendedTypes) {
		ResolvedJavaClassInfo resolvedClass = new ResolvedJavaClassInfo();
		resolvedClass.setUri(className + ".java");
		resolvedClass.setClassName(className);
		resolvedClass.setIterableType(iterableType);
		resolvedClass.setIterableOf(iterableOf);
		resolvedClass.setFields(new ArrayList<>());
		resolvedClass.setMethods(new ArrayList<>());
		if (extendedTypes != null) {
			resolvedClass.setExtendedTypes(Stream.of(extendedTypes)//
					.map(c -> c.getClassName()) //
					.collect(Collectors.toList()));
		}
		cache.put(resolvedClass.getClassName(), resolvedClass);
		return resolvedClass;
	}

	@Override
	public CompletableFuture<ProjectInfo> getProjectInfo(QuteProjectParams params) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				// Thread.sleep(5000);
			} catch (Exception e) {

			}
			return new ProjectInfo("test-qute", "templates");
		});
	}

	protected CompletableFuture<ProjectDataModel<TemplateDataModel<ParameterDataModel>>> getProjectDataModel(
			QuteProjectDataModelParams params) {
		return CompletableFuture.completedFuture(null);
	}
}
