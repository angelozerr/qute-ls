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

	public JavaDataModelCache(QuteJavaClassesProvider classProvider,
			QuteResolvedJavaClassProvider resolvedClassProvider, QuteJavaDefinitionProvider definitionProvider) {
		this.classProvider = classProvider;
		this.resolvedClassProvider = resolvedClassProvider;
		this.definitionProvider = definitionProvider;
	}

	public CompletableFuture<List<JavaClassInfo>> getJavaClasses(QuteJavaClassesParams params) {
		return classProvider.getJavaClasses(params);
	}

	private CompletableFuture<ResolvedJavaClassInfo> getResolvedJavaClass(String className, String templateFileUri) {
		if (StringUtils.isEmpty(className)) {
			return CompletableFuture.completedFuture(null);
		}
		QuteResolvedJavaClassParams params = new QuteResolvedJavaClassParams(className, templateFileUri);
		return getResolvedJavaClass(params);
	}

	public CompletableFuture<ResolvedJavaClassInfo> getResolvedJavaClass(QuteResolvedJavaClassParams params) {
		return resolvedClassProvider.getResolvedJavaClass(params);
	}

	public CompletableFuture<Location> getJavaDefinition(QuteJavaDefinitionParams params) {
		return definitionProvider.getJavaDefinition(params);
	}

	public CompletableFuture<ResolvedJavaClassInfo> getResolvedClass(Parts parts, int partIndex, Template template) {
		CompletableFuture<ResolvedJavaClassInfo> future = null;
		for (int i = 0; i <= partIndex; i++) {
			Part current = ((Part) parts.getChild(i));
			switch (current.getPartKind()) {
			case Object:
				String className = ((ObjectPart) current).getClassName();
				future = getResolvedJavaClass(className, template.getUri());
				break;
			case Property:
				if (future != null) {
					future = future //
							.thenCompose(resolvedClass -> {
								if (resolvedClass == null) {
									return null;
								}
								String property = current.getTextContent();
								JavaClassMemberInfo member = resolvedClass.findMember(property);
								if (member == null) {
									return null;
								}
								String memberType = member.getType();
								return getResolvedJavaClass(memberType, template.getUri());
							});
				}
				break;
			}
		}
		return future != null ? future : CompletableFuture.completedFuture(null);
	}

}
