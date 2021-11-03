/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt.internal.template;

import static com.redhat.qute.jdt.internal.QuteJavaConstants.TEMPLATE_EXTENSION_ANNOTATION_NAMESPACE;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import com.redhat.qute.commons.ValueResolver;
import com.redhat.qute.jdt.QuteSupportForTemplate;
import com.redhat.qute.jdt.internal.resolver.ITypeResolver;
import com.redhat.qute.jdt.utils.AnnotationUtils;

/**
 * Template extension support.
 * 
 * @author Angelo ZERR
 * 
 * @see https://quarkus.io/guides/qute-reference#template_extension_methods
 *
 */
class TemplateExtensionSupport {

	public static void collectResolversForTemplateExtension(IType type, IAnnotation templateExtension,
			List<ValueResolver> resolvers, IProgressMonitor monitor) {
		try {
			ITypeResolver typeResolver = QuteSupportForTemplate.createTypeResolver(type);
			IMethod[] methods = type.getMethods();
			for (IMethod method : methods) {
				collectResolversForTemplateExtension(method, templateExtension, resolvers, typeResolver);
			}
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void collectResolversForTemplateExtension(IMethod method, IAnnotation templateExtension,
			List<ValueResolver> resolvers, IProgressMonitor monitor) {
		ITypeResolver typeResolver = QuteSupportForTemplate.createTypeResolver(method);
		collectResolversForTemplateExtension(method, templateExtension, resolvers, typeResolver);
	}

	private static void collectResolversForTemplateExtension(IMethod method, IAnnotation templateExtension,
			List<ValueResolver> resolvers, ITypeResolver typeResolver) {
		ValueResolver resolver = new ValueResolver();
		resolver.setSourceType(method.getDeclaringType().getFullyQualifiedName());
		resolver.setSignature(typeResolver.resolveMethodSignature(method));
		try {
			resolver.setNamespace(AnnotationUtils.getAnnotationMemberValue(templateExtension, TEMPLATE_EXTENSION_ANNOTATION_NAMESPACE));
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		resolvers.add(resolver);
	}
}
