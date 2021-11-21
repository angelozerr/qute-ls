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
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;

import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.QuteJavaTypesParams;
import com.redhat.qute.commons.QuteJavaCodeLensParams;
import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.commons.QuteProjectParams;
import com.redhat.qute.commons.QuteResolvedJavaTypeParams;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelProject;
import com.redhat.qute.commons.datamodel.QuteDataModelProjectParams;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.jdt.IJavaDataModelChangedListener;
import com.redhat.qute.jdt.QutePlugin;
import com.redhat.qute.jdt.QuteSupportForJava;
import com.redhat.qute.jdt.QuteSupportForTemplate;
import com.redhat.qute.ls.api.QuteLanguageClientAPI;
import com.redhat.qute.ls.api.QuteLanguageServerAPI;
import com.redhat.qute.lsp4e.internal.JDTUtilsImpl;

/**
 * Qute language client.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteLanguageClientImpl extends LanguageClientImpl implements QuteLanguageClientAPI {

	private static IJavaDataModelChangedListener SINGLETON_LISTENER;

	private IJavaDataModelChangedListener listener = event -> {
		((QuteLanguageServerAPI) getLanguageServer()).dataModelChanged(event);
	};

	public QuteLanguageClientImpl() {
		// FIXME : how to remove the listener????
		// The listener should be removed when language server is shutdown, how to
		// manage that????
		if (SINGLETON_LISTENER != null) {
			QutePlugin.getDefault().removeJavaDataModelChangedListener(SINGLETON_LISTENER);
		}
		SINGLETON_LISTENER = listener;
		QutePlugin.getDefault().addJavaDataModelChangedListener(listener);
	}

	@Override
	public CompletableFuture<ProjectInfo> getProjectInfo(QuteProjectParams params) {
		return CompletableFutures.computeAsync((cancelChecker) -> {
			IProgressMonitor monitor = getProgressMonitor(cancelChecker);
			return QuteSupportForTemplate.getInstance().getProjectInfo(params, JDTUtilsImpl.getInstance(), monitor);
		});
	}

	@Override
	public CompletableFuture<DataModelProject<DataModelTemplate<DataModelParameter>>> getDataModelProject(
			QuteDataModelProjectParams params) {
		return CompletableFutures.computeAsync((cancelChecker) -> {
			try {
				IProgressMonitor monitor = getProgressMonitor(cancelChecker);
				return QuteSupportForTemplate.getInstance().getDataModelProject(params, JDTUtilsImpl.getInstance(),
						monitor);
			} catch (CoreException e) {
				QuteLSPPlugin.logException(e.getLocalizedMessage(), e);
				return null;
			}
		});
	}

	@Override
	public CompletableFuture<List<JavaTypeInfo>> getJavaTypes(QuteJavaTypesParams params) {
		return CompletableFutures.computeAsync((cancelChecker) -> {
			try {
				IProgressMonitor monitor = getProgressMonitor(cancelChecker);
				return QuteSupportForTemplate.getInstance().getJavaTypes(params, JDTUtilsImpl.getInstance(), monitor);
			} catch (CoreException e) {
				QuteLSPPlugin.logException(e.getLocalizedMessage(), e);
				return Collections.emptyList();
			}
		});
	}

	@Override
	public CompletableFuture<ResolvedJavaTypeInfo> getResolvedJavaType(QuteResolvedJavaTypeParams params) {
		return CompletableFutures.computeAsync((cancelChecker) -> {
			try {
				IProgressMonitor monitor = getProgressMonitor(cancelChecker);
				return QuteSupportForTemplate.getInstance().getResolvedJavaType(params, JDTUtilsImpl.getInstance(),
						monitor);
			} catch (CoreException e) {
				QuteLSPPlugin.logException(e.getLocalizedMessage(), e);
				return null;
			}
		});
	}

	@Override
	public CompletableFuture<Location> getJavaDefinition(QuteJavaDefinitionParams params) {
		return CompletableFutures.computeAsync((cancelChecker) -> {
			try {
				IProgressMonitor monitor = getProgressMonitor(cancelChecker);
				return QuteSupportForTemplate.getInstance().getJavaDefinition(params, JDTUtilsImpl.getInstance(),
						monitor);
			} catch (CoreException e) {
				QuteLSPPlugin.logException(e.getLocalizedMessage(), e);
				return null;
			}
		});
	}

	@Override
	public CompletableFuture<List<? extends CodeLens>> getJavaCodelens(QuteJavaCodeLensParams javaParams) {
		return CompletableFutures.computeAsync((cancelChecker) -> {
			//try {
				IProgressMonitor monitor = getProgressMonitor(cancelChecker);
				return QuteSupportForJava.getInstance().codeLens(javaParams, JDTUtilsImpl.getInstance(), monitor);
			/*} catch (CoreException e) {
				QuteLSPPlugin.logException(e.getLocalizedMessage(), e);
				return null;
			}*/
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
