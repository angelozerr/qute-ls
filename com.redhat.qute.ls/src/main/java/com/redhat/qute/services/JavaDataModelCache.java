package com.redhat.qute.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Location;

import com.redhat.qute.commons.JavaClassInfo;
import com.redhat.qute.commons.JavaMemberInfo;
import com.redhat.qute.commons.JavaDataModelChangeEvent;
import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.QuteJavaClassesParams;
import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.commons.QuteProjectParams;
import com.redhat.qute.commons.QuteResolvedJavaClassParams;
import com.redhat.qute.commons.ResolvedJavaClassInfo;
import com.redhat.qute.ls.api.QuteJavaClassesProvider;
import com.redhat.qute.ls.api.QuteJavaDefinitionProvider;
import com.redhat.qute.ls.api.QuteProjectInfoProvider;
import com.redhat.qute.ls.api.QuteResolvedJavaClassProvider;
import com.redhat.qute.parser.expression.ObjectPart;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.parser.template.JavaTypeInfoProvider;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.utils.StringUtils;

public class JavaDataModelCache implements QuteProjectInfoProvider {

	private static CompletableFuture<ResolvedJavaClassInfo> NULL_FUTURE = CompletableFuture.completedFuture(null);

	private static class ProjectContainer {

		private final Map<String, CompletableFuture<ResolvedJavaClassInfo>> classes;

		public ProjectContainer() {
			classes = new HashMap<>();
		}

		public CompletableFuture<ResolvedJavaClassInfo> getResolvedJavaClass(String className) {
			return classes.get(className);
		}

		public void registerResolvedJavaClass(String className, CompletableFuture<ResolvedJavaClassInfo> future) {
			classes.put(className, future);
		}

	}

	private final Map<String /* project uri */, ProjectContainer> projects;

	private final QuteProjectInfoProvider projectInfoProvider;

	private final QuteJavaClassesProvider classProvider;

	private final QuteResolvedJavaClassProvider resolvedClassProvider;

	private final QuteJavaDefinitionProvider definitionProvider;

	public JavaDataModelCache(QuteProjectInfoProvider projectInfoProvider, QuteJavaClassesProvider classProvider,
			QuteResolvedJavaClassProvider resolvedClassProvider, QuteJavaDefinitionProvider definitionProvider) {
		this.projects = new HashMap<>();
		this.projectInfoProvider = projectInfoProvider;
		this.classProvider = classProvider;
		this.resolvedClassProvider = resolvedClassProvider;
		this.definitionProvider = definitionProvider;
	}

	protected CompletableFuture<List<JavaClassInfo>> getJavaClasses(QuteJavaClassesParams params) {
		return classProvider.getJavaClasses(params);
	}

	protected CompletableFuture<ResolvedJavaClassInfo> getResolvedJavaClass(QuteResolvedJavaClassParams params) {
		return resolvedClassProvider.getResolvedJavaClass(params);
	}

	protected CompletableFuture<Location> getJavaDefinition(QuteJavaDefinitionParams params) {
		return definitionProvider.getJavaDefinition(params);
	}

	public CompletableFuture<ResolvedJavaClassInfo> resolveJavaType(String className, String projectUri) {
		if (StringUtils.isEmpty(className) || StringUtils.isEmpty(projectUri)) {
			return NULL_FUTURE;
		}
		ProjectContainer container = getProjectContainer(projectUri);
		CompletableFuture<ResolvedJavaClassInfo> future = container.getResolvedJavaClass(className);
		if (future == null || future.isCancelled() || future.isCompletedExceptionally()) {
			QuteResolvedJavaClassParams params = new QuteResolvedJavaClassParams(className, projectUri);
			future = getResolvedJavaClass(params);
			container.registerResolvedJavaClass(className, future);
		}
		return future;
	}

	private ProjectContainer getProjectContainer(String projectUri) {
		ProjectContainer container = projects.get(projectUri);
		if (container == null) {
			container = new ProjectContainer();
			projects.put(projectUri, container);
		}
		return container;
	}

	public CompletableFuture<ResolvedJavaClassInfo> resolveJavaType(Parts parts, int partIndex, String projectUri) {
		CompletableFuture<ResolvedJavaClassInfo> future = null;
		for (int i = 0; i < partIndex + 1; i++) {
			Part current = ((Part) parts.getChild(i));
			switch (current.getPartKind()) {
			case Object:
				ObjectPart objectPart = (ObjectPart) current;
				future = getResolvedClass(objectPart, projectUri);
				break;
			case Property:
				if (future != null) {
					future = future //
							.thenCompose(resolvedClass -> {
								if (resolvedClass == null) {
									return NULL_FUTURE;
								}
								String property = current.getPartName();
								JavaMemberInfo member = resolvedClass.findMember(property);
								if (member == null) {
									return NULL_FUTURE;
								}
								String memberType = member.getMemberType();
								return resolveJavaType(memberType, projectUri);
							});
				}
				break;
			default:
			}
		}
		return future != null ? future : NULL_FUTURE;
	}

	public CompletableFuture<ResolvedJavaClassInfo> resolveJavaType(Part part, String projectUri) {
		Parts parts = part.getParent();
		int partIndex = parts.getPartIndex(part);
		return resolveJavaType(parts, partIndex, projectUri);
	}

	private CompletableFuture<ResolvedJavaClassInfo> getResolvedClass(ObjectPart objectPart, String projectUri) {
		CompletableFuture<ResolvedJavaClassInfo> future;
		JavaTypeInfoProvider javaTypeInfo = objectPart.resolveJavaType();
		if (javaTypeInfo == null) {
			return NULL_FUTURE;
		}
		String className = javaTypeInfo.getClassName();
		if (StringUtils.isEmpty(className)) {
			return NULL_FUTURE;
		}

		future = resolveJavaType(className, projectUri);

		Node node = javaTypeInfo.getNode();
		if (node.getKind() == NodeKind.Section) {
			Section section = (Section) node;
			if (section.isIterable()) {
				future = future //
						.thenCompose(resolvedClass -> {
							if (resolvedClass == null) {
								return NULL_FUTURE;
							}
							if (!resolvedClass.isIterable()) {
								return CompletableFuture.completedFuture(resolvedClass);
							}
							String iterClassName = resolvedClass.getIterableOf();
							return resolveJavaType(iterClassName, projectUri);
						});
			}
		}
		return future;
	}

	public void dataModelChanged(JavaDataModelChangeEvent event) {
		Set<String> projectUris = event.getProjectURIs();
		for (String projectUri : projectUris) {
			projects.remove(projectUri);
		}
	}

	@Override
	public CompletableFuture<ProjectInfo> getProjectInfo(QuteProjectParams params) {
		return projectInfoProvider.getProjectInfo(params);
	}

}
