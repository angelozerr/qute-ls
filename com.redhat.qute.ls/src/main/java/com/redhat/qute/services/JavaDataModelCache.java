package com.redhat.qute.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Location;

import com.redhat.qute.commons.JavaClassInfo;
import com.redhat.qute.commons.JavaClassMemberInfo;
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
import com.redhat.qute.utils.StringUtils;

public class JavaDataModelCache implements QuteProjectInfoProvider {

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

	private CompletableFuture<ResolvedJavaClassInfo> getResolvedJavaClass(String className, String projectUri) {
		if (StringUtils.isEmpty(className) || StringUtils.isEmpty(projectUri)) {
			return CompletableFuture.completedFuture(null);
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

	public CompletableFuture<ResolvedJavaClassInfo> getResolvedClass(Parts parts, int partIndex, String projectUri) {
		CompletableFuture<ResolvedJavaClassInfo> future = null;
		for (int i = 0; i <= partIndex; i++) {
			Part current = ((Part) parts.getChild(i));
			switch (current.getPartKind()) {
			case Object:
				String className = ((ObjectPart) current).getClassName();
				future = getResolvedJavaClass(className, projectUri);
				break;
			case Property:
				if (future != null) {
					future = future //
							.thenCompose(resolvedClass -> {
								if (resolvedClass == null) {
									return CompletableFuture.completedFuture(null);
								}
								String property = current.getTextContent();
								JavaClassMemberInfo member = resolvedClass.findMember(property);
								if (member == null) {
									return CompletableFuture.completedFuture(null);
								}
								String memberType = member.getType();
								return getResolvedJavaClass(memberType, projectUri);
							});
				}
				break;
			}
		}
		return future != null ? future : CompletableFuture.completedFuture(null);
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
