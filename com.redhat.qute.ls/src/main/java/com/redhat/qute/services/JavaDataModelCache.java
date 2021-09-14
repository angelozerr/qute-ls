package com.redhat.qute.services;

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
import com.redhat.qute.parser.expression.MemberPart;
import com.redhat.qute.parser.expression.ObjectPart;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.parser.expression.Parts.PartKind;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.utils.StringUtils;

public class JavaDataModelCache {

	private final QuteJavaClassesProvider classProvider;

	private final QuteResolvedJavaClassProvider resolvedClassProvider;

	private final QuteJavaDefinitionProvider definitionProvider;

	public JavaDataModelCache(QuteJavaClassesProvider classProvider, QuteResolvedJavaClassProvider resolvedClassProvider,
			QuteJavaDefinitionProvider definitionProvider) {
		this.classProvider = classProvider;
		this.resolvedClassProvider = resolvedClassProvider;
		this.definitionProvider = definitionProvider;
	}

	public CompletableFuture<List<JavaClassInfo>> getJavaClasses(QuteJavaClassesParams params) {
		return classProvider.getJavaClasses(params);
	}

	private CompletableFuture<JavaClassInfo> getResolvedClass(String className, String templateFileUri) {
		QuteJavaClassesParams params = new QuteJavaClassesParams(className, templateFileUri);
		return getJavaClasses(params) //
				.thenApply(classes -> {
					return classes != null && !classes.isEmpty() ? classes.get(0) : null;
				});
	}

	public CompletableFuture<ResolvedJavaClassInfo> getResolvedJavaClass(QuteResolvedJavaClassParams params) {
		return resolvedClassProvider.getResolvedJavaClass(params);
	}

	public CompletableFuture<Location> getJavaDefinition(QuteJavaDefinitionParams params) {
		return definitionProvider.getJavaDefinition(params);
	}

	public JavaClassInfo resolveObjectPart(ObjectPart part, Template template) {
		Parts parts = (Parts) part.getParent();
		int partIndex = parts.getChildren().indexOf(part);
		for (int i = 0; i < partIndex; i++) {

		}
		return null;
	}

	public CompletableFuture<JavaClassMemberInfo> resolveMemberPart(MemberPart part, Template template) {
		String field = part.getPartKind() == PartKind.Property ? part.getTextContent() : null;
		String method = part.getPartKind() == PartKind.Method ? part.getTextContent() : null;
		if (field == null && method == null) {
			return CompletableFuture.completedFuture(null);
		}
		
		Parts parts = (Parts) part.getParent();
		int partIndex = parts.getChildren().indexOf(part);
		String className = null;
		
		CompletableFuture<JavaClassMemberInfo> future = null;
		for (int i = 0; i < partIndex; i++) {
			Part current = ((Part) parts.getChild(i));
			if (current.getPartKind() == PartKind.Object) {
				className = ((ObjectPart) current).getClassName();
			} else {
				if (StringUtils.isEmpty(className)) {
					break;
				}				
				className = null;
			}
		}
		if (StringUtils.isEmpty(className)) {
			return CompletableFuture.completedFuture(null);
		}
		if (future == null) {
			//return getJavaClassMembers(null)
		}
		
		return CompletableFuture.completedFuture(null);
	}
}
