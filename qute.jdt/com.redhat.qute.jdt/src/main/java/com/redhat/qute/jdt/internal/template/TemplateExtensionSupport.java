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

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import com.redhat.qute.commons.ValueResolver;
import com.redhat.qute.jdt.internal.resolver.ClassFileTypeResolver;
import com.redhat.qute.jdt.internal.resolver.CompilationUnitTypeResolver;
import com.redhat.qute.jdt.internal.resolver.ITypeResolver;

/**
 * Template extension support.
 * 
 * @author Angelo ZERR
 * 
 * @see https://quarkus.io/guides/qute-reference#template_extension_methods
 *
 */
class TemplateExtensionSupport {

	public static void collectTemplateDataModelForTemplateExtension(IType type, List<ValueResolver> resolvers,
			IProgressMonitor monitor) {
		try {
			ITypeResolver typeResolver = !type.isBinary()
					? new CompilationUnitTypeResolver(
							(ICompilationUnit) type.getAncestor(IJavaElement.COMPILATION_UNIT))
					: new ClassFileTypeResolver((IClassFile) type.getAncestor(IJavaElement.CLASS_FILE));

			IMethod[] methods = type.getMethods();
			for (IMethod method : methods) {
				ValueResolver resolver = new ValueResolver();
				resolver.setSignature(typeResolver.resolveMethodSignature(method));
				resolvers.add(resolver);
			}
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
