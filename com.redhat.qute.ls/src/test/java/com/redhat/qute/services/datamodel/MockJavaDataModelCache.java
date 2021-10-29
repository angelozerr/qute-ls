package com.redhat.qute.services.datamodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

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

public class MockJavaDataModelCache extends JavaDataModelCache {

	public static final Range JAVA_CLASS_RANGE = new Range(new Position(0, 0), new Position(0, 0));

	public static final Range JAVA_FIELD_RANGE = new Range(new Position(1, 1), new Position(1, 1));

	public static final Range JAVA_METHOD_RANGE = new Range(new Position(2, 2), new Position(2, 2));

	private final Map<String, ResolvedJavaClassInfo> resolvedClassesCache;

	public MockJavaDataModelCache() {
		super(null, null, null, null, null);
		this.resolvedClassesCache = createResolvedClasses();
	}

	@Override
	public CompletableFuture<List<JavaClassInfo>> getJavaClasses(QuteJavaClassesParams params) {
		return CompletableFuture.completedFuture(resolvedClassesCache.values() //
				.stream() //
				.collect(Collectors.toList()));
	}

	@Override
	protected CompletableFuture<ResolvedJavaClassInfo> getResolvedJavaClass(QuteResolvedJavaClassParams params) {
		return CompletableFuture.completedFuture(resolvedClassesCache.get(params.getClassName()));
	}

	@Override
	public CompletableFuture<Location> getJavaDefinition(QuteJavaDefinitionParams params) {
		String className = params.getClassName();
		ResolvedJavaClassInfo classInfo = resolvedClassesCache.get(className);
		if (classInfo != null) {
			Range definitionRange = null;
			String fieldName = params.getField();
			if (fieldName != null) {
				// Definition for field
				JavaFieldInfo fieldInfo = classInfo.findField(fieldName);
				if (fieldInfo != null) {
					definitionRange = JAVA_FIELD_RANGE;
				}
			} else {
				// Definition for method
				String methodName = params.getMethod();
				if (methodName != null) {
					JavaMethodInfo methodInfo = classInfo.findMethod(methodName);
					if (methodInfo != null) {
						definitionRange = JAVA_METHOD_RANGE;
					}
				} else {
					// Definition for class
					definitionRange = JAVA_CLASS_RANGE;
				}
			}

			if (definitionRange != null) {
				String javeFileUri = className.replaceAll("[.]", "/") + ".java";
				Location location = new Location(javeFileUri, definitionRange);
				return CompletableFuture.completedFuture(location);
			}
		}
		return CompletableFuture.completedFuture(null);
	}

	protected Map<String, ResolvedJavaClassInfo> createResolvedClasses() {
		Map<String, ResolvedJavaClassInfo> cache = new HashMap<>();

		createResolvedJavaClassInfo("org.acme", cache).setUri(null);

		ResolvedJavaClassInfo string = createResolvedJavaClassInfo("java.lang.String", cache);
		registerField("UTF16", "byte", string);
		createResolvedJavaClassInfo("java.lang.Boolean", cache);
		createResolvedJavaClassInfo("java.lang.Integer", cache);
		createResolvedJavaClassInfo("java.lang.Double", cache);
		createResolvedJavaClassInfo("java.lang.Long", cache);
		createResolvedJavaClassInfo("java.lang.Float", cache);
		createResolvedJavaClassInfo("java.math.BigInteger", cache);

		ResolvedJavaClassInfo review = createResolvedJavaClassInfo("org.acme.Review", cache);
		registerField("name", "java.lang.String", review);
		registerField("average", "java.lang.Integer", review);

		ResolvedJavaClassInfo item = createResolvedJavaClassInfo("org.acme.Item", cache);
		registerField("name", "java.lang.String", item);
		registerField("price", "java.math.BigInteger", item);
		registerField("review", "org.acme.Review", item);
		registerMethod("getReview2() : org.acme.Review", item);
		registerMethod("getReviews() : java.util.List<org.acme.Review>", item);
		createResolvedJavaClassInfo("java.util.List<org.acme.Review>", "java.util.List", "org.acme.Review", cache);

		createResolvedJavaClassInfo("java.util.List<org.acme.Item>", "java.util.List", "org.acme.Item", cache);
		ResolvedJavaClassInfo list = createResolvedJavaClassInfo("java.util.List", cache);
		list.setExtendedTypes(Arrays.asList("java.lang.Iterable"));
		registerMethod("size() : int", list);

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
		resolvedClass.setFields(new ArrayList<>());
		resolvedClass.setMethods(new ArrayList<>());
		cache.put(resolvedClass.getClassName(), resolvedClass);
		return resolvedClass;
	}

	@Override
	public CompletableFuture<ProjectInfo> getProjectInfo(QuteProjectParams params) {
		return CompletableFuture.completedFuture(new ProjectInfo("test-qute", "templates"));
	}

	@Override
	protected CompletableFuture<ProjectDataModel<TemplateDataModel<ParameterDataModel>>> getProjectDataModel(
			QuteProjectDataModelParams params) {
		return CompletableFuture.completedFuture(null);
	}
}
