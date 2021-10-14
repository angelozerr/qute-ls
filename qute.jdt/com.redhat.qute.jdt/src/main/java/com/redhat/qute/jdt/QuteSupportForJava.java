/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.core.manipulation.dom.ASTResolving;
import org.eclipse.lsp4j.CodeLens;

import com.redhat.qute.commons.QuteJavaCodeLensParams;
import com.redhat.qute.jdt.internal.QuteAnnotationConstants;
import com.redhat.qute.jdt.internal.codelens.QuteJavaCodeLensCollector;
import com.redhat.qute.jdt.utils.IJDTUtils;
import com.redhat.qute.jdt.utils.JDTTypeUtils;

/**
 * Qute support for Java file.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteSupportForJava {

	private static final QuteSupportForJava INSTANCE = new QuteSupportForJava();

	public static QuteSupportForJava getInstance() {
		return INSTANCE;
	}

	public List<? extends CodeLens> codeLens(QuteJavaCodeLensParams params, IJDTUtils utils, IProgressMonitor monitor) {
		String uri = params.getUri();
		ITypeRoot typeRoot = resolveTypeRoot(uri, utils, monitor);
		if (typeRoot == null || !hasQuteSupport(typeRoot.getJavaProject())) {
			return Collections.emptyList();
		}
		List<CodeLens> lenses = new ArrayList<>();
		CompilationUnit cu = getASTRoot(typeRoot);
		cu.accept(new QuteJavaCodeLensCollector(typeRoot, lenses, utils, monitor));
		return lenses;
	}

	private static boolean hasQuteSupport(IJavaProject javaProject) {
		return JDTTypeUtils.findType(javaProject, QuteAnnotationConstants.CHECKED_TEMPLATE_ANNOTATION) != null
				|| JDTTypeUtils.findType(javaProject, QuteAnnotationConstants.OLD_CHECKED_TEMPLATE_ANNOTATION) != null;
	}

	/**
	 * Given the uri returns a {@link ITypeRoot}. May return null if it can not
	 * associate the uri with a Java file ot class file.
	 *
	 * @param uri
	 * @param utils   JDT LS utilities
	 * @param monitor the progress monitor
	 * @return compilation unit
	 */
	private static ITypeRoot resolveTypeRoot(String uri, IJDTUtils utils, IProgressMonitor monitor) {
		utils.waitForLifecycleJobs(monitor);
		final ICompilationUnit unit = utils.resolveCompilationUnit(uri);
		IClassFile classFile = null;
		if (unit == null) {
			classFile = utils.resolveClassFile(uri);
			if (classFile == null) {
				return null;
			}
		} else {
			if (!unit.getResource().exists() || monitor.isCanceled()) {
				return null;
			}
		}
		return unit != null ? unit : classFile;
	}

	private static CompilationUnit getASTRoot(ITypeRoot typeRoot) {
		return ASTResolving.createQuickFixAST((ICompilationUnit) typeRoot, null);
	}
}
