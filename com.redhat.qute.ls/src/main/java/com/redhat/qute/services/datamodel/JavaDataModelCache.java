package com.redhat.qute.services.datamodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Location;

import com.redhat.qute.commons.JavaClassInfo;
import com.redhat.qute.commons.JavaMemberInfo;
import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.QuteJavaClassesParams;
import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.commons.QuteProjectParams;
import com.redhat.qute.commons.QuteResolvedJavaClassParams;
import com.redhat.qute.commons.ResolvedJavaClassInfo;
import com.redhat.qute.commons.datamodel.JavaDataModelChangeEvent;
import com.redhat.qute.commons.datamodel.ParameterDataModel;
import com.redhat.qute.commons.datamodel.ProjectDataModel;
import com.redhat.qute.commons.datamodel.QuteProjectDataModelParams;
import com.redhat.qute.commons.datamodel.TemplateDataModel;
import com.redhat.qute.ls.api.QuteJavaClassesProvider;
import com.redhat.qute.ls.api.QuteJavaDefinitionProvider;
import com.redhat.qute.ls.api.QuteProjectDataModelProvider;
import com.redhat.qute.ls.api.QuteProjectInfoProvider;
import com.redhat.qute.ls.api.QuteResolvedJavaClassProvider;
import com.redhat.qute.parser.expression.ObjectPart;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.parser.template.JavaTypeInfoProvider;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.TemplateDataModelProvider;
import com.redhat.qute.utils.StringUtils;

public class JavaDataModelCache implements QuteProjectInfoProvider, TemplateDataModelProvider {

	private static final CompletableFuture<ResolvedJavaClassInfo> NULL_FUTURE = CompletableFuture.completedFuture(null);

	private static final Map<String, CompletableFuture<ResolvedJavaClassInfo>> javaPrimitiveTypes;

	static {
		javaPrimitiveTypes = new HashMap<>();
		registerPrimitiveType("boolean");
		registerPrimitiveType("byte");
		registerPrimitiveType("double");
		registerPrimitiveType("float");
		registerPrimitiveType("int");
		registerPrimitiveType("long");
	}

	private static void registerPrimitiveType(String type) {
		ResolvedJavaClassInfo classInfo = new ResolvedJavaClassInfo();
		classInfo.setClassName(type);
		javaPrimitiveTypes.put(type, CompletableFuture.completedFuture(classInfo));

	}

	private class ProjectContainer {

		private String projectUri;

		private final Map<String /* Full qualified name of Java class */, CompletableFuture<ResolvedJavaClassInfo>> classes;

		private CompletableFuture<ExtendedProjectDataModel> future;

		public ProjectContainer(String projectUri) {
			classes = new HashMap<>();
			this.projectUri = projectUri;
		}

		public CompletableFuture<ResolvedJavaClassInfo> getResolvedJavaClass(String className) {
			return classes.get(className);
		}

		public void registerResolvedJavaClass(String className, CompletableFuture<ResolvedJavaClassInfo> future) {
			classes.put(className, future);
		}

		public CompletableFuture<ExtendedProjectDataModel> getDataModel() {
			if (future == null || future.isCancelled() || future.isCompletedExceptionally()) {
				QuteProjectDataModelParams params = new QuteProjectDataModelParams();
				params.setProjectUri(projectUri);
				future = getProjectDataModel(params) //
						.thenApply(project -> {
							if (project == null) {
								return null;
							}
							return new ExtendedProjectDataModel(project);
						});
			}
			return future;
		}

	}

	private final Map<String /* project uri */, ProjectContainer> projects;

	private final QuteProjectInfoProvider projectInfoProvider;

	private final QuteJavaClassesProvider classProvider;

	private final QuteResolvedJavaClassProvider resolvedClassProvider;

	private final QuteJavaDefinitionProvider definitionProvider;

	private final QuteProjectDataModelProvider dataModelProvider;

	public JavaDataModelCache(QuteProjectInfoProvider projectInfoProvider, QuteJavaClassesProvider classProvider,
			QuteResolvedJavaClassProvider resolvedClassProvider, QuteJavaDefinitionProvider definitionProvider,
			QuteProjectDataModelProvider dataModelProvider) {
		this.projects = new HashMap<>();
		this.projectInfoProvider = projectInfoProvider;
		this.classProvider = classProvider;
		this.resolvedClassProvider = resolvedClassProvider;
		this.definitionProvider = definitionProvider;
		this.dataModelProvider = dataModelProvider;
	}

	public CompletableFuture<List<JavaClassInfo>> getJavaClasses(QuteJavaClassesParams params) {
		return classProvider.getJavaClasses(params);
	}

	protected CompletableFuture<ResolvedJavaClassInfo> getResolvedJavaClass(QuteResolvedJavaClassParams params) {
		return resolvedClassProvider.getResolvedJavaClass(params);
	}

	public CompletableFuture<Location> getJavaDefinition(QuteJavaDefinitionParams params) {
		return definitionProvider.getJavaDefinition(params);
	}

	public CompletableFuture<ResolvedJavaClassInfo> resolveJavaType(String className, String projectUri) {
		CompletableFuture<ResolvedJavaClassInfo> primitiveType = javaPrimitiveTypes.get(className);
		if (primitiveType != null) {
			// It's a primitive type like boolean, double, float, etc
			return primitiveType;
		}
		if (StringUtils.isEmpty(className) || StringUtils.isEmpty(projectUri)) {
			return NULL_FUTURE;
		}
		ProjectContainer container = getProjectContainer(projectUri);
		CompletableFuture<ResolvedJavaClassInfo> future = container.getResolvedJavaClass(className);
		if (future == null || future.isCancelled() || future.isCompletedExceptionally()) {
			QuteResolvedJavaClassParams params = new QuteResolvedJavaClassParams(className, projectUri);
			future = getResolvedJavaClass(params) //
					.thenCompose(c -> {
						// Update members with the resolved class
						c.getFields().forEach(f -> {
							f.setResolvedClass(c);
						});
						c.getMethods().forEach(m -> {
							m.setResolvedClass(c);
						});
						if (c != null && c.getExtendedTypes() != null) {
							// Load extended types
							List<CompletableFuture<ResolvedJavaClassInfo>> resolvingExtendedFutures = new ArrayList<>();
							for (String extendedType : c.getExtendedTypes()) {
								CompletableFuture<ResolvedJavaClassInfo> extendedFuture = resolveJavaType(extendedType,
										projectUri);
								if (!extendedFuture.isDone()) {
									resolvingExtendedFutures.add(extendedFuture);
								}
							}
							if (!resolvingExtendedFutures.isEmpty()) {
								CompletableFuture<Void> allFutures = CompletableFuture.allOf(resolvingExtendedFutures
										.toArray(new CompletableFuture[resolvingExtendedFutures.size()]));
								return allFutures //
										.thenApply(a -> c);
							}
						}
						return CompletableFuture.completedFuture(c);
					});
			container.registerResolvedJavaClass(className, future);
		}
		return future;
	}

	private ProjectContainer getProjectContainer(String projectUri) {
		ProjectContainer container = projects.get(projectUri);
		if (container == null) {
			container = new ProjectContainer(projectUri);
			projects.put(projectUri, container);
		}
		return container;
	}

	private CompletableFuture<ResolvedJavaClassInfo> resolveJavaType(Parts parts, int partIndex, String projectUri,
			boolean nullIfDontMatchWithIterable) {
		CompletableFuture<ResolvedJavaClassInfo> future = null;
		for (int i = 0; i < partIndex + 1; i++) {
			Part current = ((Part) parts.getChild(i));
			switch (current.getPartKind()) {
			case Object:
				ObjectPart objectPart = (ObjectPart) current;
				future = resolveJavaType(objectPart, projectUri, nullIfDontMatchWithIterable);
				break;
			case Property:
			case Method:
				if (future != null) {
					future = future //
							.thenCompose(resolvedClass -> {
								if (resolvedClass == null) {
									return NULL_FUTURE;
								}
								String property = current.getPartName();
								JavaMemberInfo member = findMember(property, resolvedClass, projectUri);
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
		return resolveJavaType(part, projectUri, true);
	}

	public CompletableFuture<ResolvedJavaClassInfo> resolveJavaType(Part part, String projectUri,
			boolean nullIfDontMatchWithIterable) {
		Parts parts = part.getParent();
		int partIndex = parts.getPartIndex(part);
		return resolveJavaType(parts, partIndex, projectUri, nullIfDontMatchWithIterable);
	}

	private CompletableFuture<ResolvedJavaClassInfo> resolveJavaType(ObjectPart objectPart, String projectUri,
			boolean nullIfDontMatchWithIterable) {
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
		if (node != null && node.getKind() == NodeKind.Section) {
			Section section = (Section) node;
			if (section.isIterable()) {
				future = future //
						.thenCompose(resolvedClass -> {
							if (resolvedClass == null) {
								return NULL_FUTURE;
							}
							if (!resolvedClass.isIterable() && nullIfDontMatchWithIterable) {
								// case when iterable section is associated with a Java class which is not
								// iterable, the class is not valid
								// Ex:
								// {@org.acme.Item items}
								// {#for item in items}
								// {item.|}
								return NULL_FUTURE;
							}
							// valid case
							// Ex:
							// {@java.util.List<org.acme.Item> items}
							// {#for item in items}
							// {item.|}

							// Here
							// - resolvedClass = java.util.List<org.acme.Item>
							// - iterClassName = org.acme.Item

							// Resolve org.acme.Item
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

	@Override
	public CompletableFuture<ExtendedTemplateDataModel> getTemplateDataModel(Template template) {
		String projectUri = template.getProjectUri();
		return getProjectContainer(projectUri).getDataModel() //
				.thenApply(dataModel -> {
					if (dataModel == null || dataModel.getTemplates() == null) {
						return null;
					}
					String templateUri = template.getUri();
					int dotIndex = templateUri.lastIndexOf('.');
					if (dotIndex != -1) {
						templateUri = templateUri.substring(0, dotIndex);
					}
					if (templateUri.endsWith(".qute")) {
						templateUri = templateUri.substring(0, templateUri.length() - ".qute".length());
					}
					final String uri = templateUri;
					Optional<ExtendedTemplateDataModel> dataModelForTemplate = dataModel.getTemplates().stream() //
							.filter(t -> uri.endsWith(t.getTemplateUri())) //
							.findFirst();
					return dataModelForTemplate.isPresent() ? dataModelForTemplate.get() : null;
				});
	}

	protected CompletableFuture<ProjectDataModel<TemplateDataModel<ParameterDataModel>>> getProjectDataModel(
			QuteProjectDataModelParams params) {
		return dataModelProvider.getProjectDataModel(params);
	}

	public JavaMemberInfo findMember(String property, ResolvedJavaClassInfo resolvedType, String projectUri) {
		JavaMemberInfo memberInfo = resolvedType.findMember(property);
		if (memberInfo != null) {
			return memberInfo;
		}
		if (resolvedType.getExtendedTypes() == null) {
			return null;
		}
		for (String extendedType : resolvedType.getExtendedTypes()) {
			ResolvedJavaClassInfo resolvedExtendedType = resolveJavaType(extendedType, projectUri).getNow(null);
			if (resolvedExtendedType != null) {
				memberInfo = resolvedExtendedType.findMember(property);
				if (memberInfo != null) {
					return memberInfo;
				}
			}
		}
		return null;
	}
}
