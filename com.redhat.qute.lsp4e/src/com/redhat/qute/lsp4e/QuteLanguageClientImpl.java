/******************************************************************************* 
 * Copyright (c) 2019 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package com.redhat.qute.lsp4e;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.lsp4e.LanguageClientImpl;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;

import com.redhat.qute.commons.JavaClassInfo;
import com.redhat.qute.commons.JavaClassMemberInfo;
import com.redhat.qute.commons.QuteJavaClassMembersParams;
import com.redhat.qute.commons.QuteJavaClassesParams;
import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.jdt.JavaDataModelManager;
import com.redhat.qute.ls.api.QuteLanguageClientAPI;
import com.redhat.qute.lsp4e.internal.JDTUtilsImpl;

/**
 * Qute language client.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteLanguageClientImpl extends LanguageClientImpl implements QuteLanguageClientAPI {

	public QuteLanguageClientImpl() {
	}

	@Override
	public CompletableFuture<List<JavaClassInfo>> getJavaClasses(QuteJavaClassesParams params) {
		return CompletableFutures.computeAsync((cancelChecker) -> {
			try {
				IProgressMonitor monitor = getProgressMonitor(cancelChecker);
				return JavaDataModelManager.getInstance().getJavaClasses(params, JDTUtilsImpl.getInstance(), monitor);
			} catch (CoreException e) {
				QuteLSPPlugin.logException(e.getLocalizedMessage(), e);
				return Collections.emptyList();
			}
		});
	}

	@Override
	public CompletableFuture<List<JavaClassMemberInfo>> getJavaClasseMembers(QuteJavaClassMembersParams params) {
		return CompletableFutures.computeAsync((cancelChecker) -> {
			IProgressMonitor monitor = getProgressMonitor(cancelChecker);

			return null;
		});
	}

	@Override
	public CompletableFuture<Location> getJavaDefinition(QuteJavaDefinitionParams params) {
		return CompletableFutures.computeAsync((cancelChecker) -> {
			try {
				IProgressMonitor monitor = getProgressMonitor(cancelChecker);
				return JavaDataModelManager.getInstance().getJavaDefinition(params, JDTUtilsImpl.getInstance(),
						monitor);
			} catch (CoreException e) {
				QuteLSPPlugin.logException(e.getLocalizedMessage(), e);
				return null;
			}
		});
	}

	private static IProgressMonitor getProgressMonitor(CancelChecker cancelChecker) {
		IProgressMonitor monitor = new NullProgressMonitor() {
			public boolean isCanceled() {
				cancelChecker.checkCanceled();
				return false;
			};
		};
		return monitor;
	}

}
