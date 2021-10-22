/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.ls;

import static com.redhat.qute.utils.VersionHelper.getVersion;
import static org.eclipse.lsp4j.jsonrpc.CompletableFutures.computeAsync;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;

import com.redhat.qute.commons.JavaClassInfo;
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
import com.redhat.qute.indexing.QuteProjectRegistry;
import com.redhat.qute.ls.api.QuteJavaClassesProvider;
import com.redhat.qute.ls.api.QuteJavaDefinitionProvider;
import com.redhat.qute.ls.api.QuteLanguageClientAPI;
import com.redhat.qute.ls.api.QuteLanguageServerAPI;
import com.redhat.qute.ls.api.QuteProjectDataModelProvider;
import com.redhat.qute.ls.api.QuteProjectInfoProvider;
import com.redhat.qute.ls.api.QuteResolvedJavaClassProvider;
import com.redhat.qute.ls.commons.ParentProcessWatcher.ProcessLanguageServer;
import com.redhat.qute.ls.commons.client.ExtendedClientCapabilities;
import com.redhat.qute.ls.commons.client.InitializationOptionsExtendedClientCapabilities;
import com.redhat.qute.services.QuteLanguageService;
import com.redhat.qute.services.datamodel.JavaDataModelCache;
import com.redhat.qute.settings.SharedSettings;
import com.redhat.qute.settings.capabilities.QuteCapabilityManager;
import com.redhat.qute.settings.capabilities.ServerCapabilitiesInitializer;

/**
 * Qute language server.
 *
 */
public class QuteLanguageServer implements LanguageServer, ProcessLanguageServer, QuteLanguageServerAPI,
		QuteProjectInfoProvider, QuteJavaClassesProvider, QuteResolvedJavaClassProvider, QuteJavaDefinitionProvider,
		QuteProjectDataModelProvider {

	private static final Logger LOGGER = Logger.getLogger(QuteLanguageServer.class.getName());

	private final JavaDataModelCache dataModelCache;

	private final QuteProjectRegistry projectRegistry;

	private final QuteLanguageService quteLanguageService;
	private final QuteTextDocumentService textDocumentService;
	private final QuteWorkspaceService workspaceService;

	private Integer parentProcessId;
	private QuteLanguageClientAPI languageClient;
	private QuteCapabilityManager capabilityManager;

	public QuteLanguageServer() {
		this.projectRegistry = new QuteProjectRegistry();
		this.dataModelCache = createDataModelCache();
		this.quteLanguageService = new QuteLanguageService(dataModelCache);
		this.textDocumentService = new QuteTextDocumentService(this, new SharedSettings());
		this.workspaceService = new QuteWorkspaceService(this);
	}

	private JavaDataModelCache createDataModelCache() {
		// return new MockJavaDataModelCache();
		return new JavaDataModelCache(this, this, this, this, this);
	}

	@Override
	public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		LOGGER.info("Initializing Qute server " + getVersion() + " with " + System.getProperty("java.home"));

		this.parentProcessId = params.getProcessId();

		ExtendedClientCapabilities extendedClientCapabilities = InitializationOptionsExtendedClientCapabilities
				.getExtendedClientCapabilities(params);
		capabilityManager.setClientCapabilities(params.getCapabilities(), extendedClientCapabilities);

		textDocumentService.updateClientCapabilities(params.getCapabilities());
		ServerCapabilities serverCapabilities = ServerCapabilitiesInitializer
				.getNonDynamicServerCapabilities(capabilityManager.getClientCapabilities());

		InitializeResult initializeResult = new InitializeResult(serverCapabilities);
		return CompletableFuture.completedFuture(initializeResult);
	}

	/*
	 * Registers all capabilities that do not support client side preferences to
	 * turn on/off
	 *
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.lsp4j.services.LanguageServer#initialized(org.eclipse.lsp4j.
	 * InitializedParams)
	 */
	@Override
	public void initialized(InitializedParams params) {
		capabilityManager.initializeCapabilities();
		getCapabilityManager().registerExecuteCommand(getWorkspaceService().getCommandIds());
	}

	@Override
	public CompletableFuture<Object> shutdown() {
		return computeAsync(cc -> new Object());
	}

	@Override
	public void exit() {
		exit(0);
	}

	@Override
	public void exit(int exitCode) {
		System.exit(exitCode);
	}

	public TextDocumentService getTextDocumentService() {
		return this.textDocumentService;
	}

	public QuteWorkspaceService getWorkspaceService() {
		return this.workspaceService;
	}

	public QuteLanguageClientAPI getLanguageClient() {
		return languageClient;
	}

	public QuteCapabilityManager getCapabilityManager() {
		return capabilityManager;
	}

	public void setClient(LanguageClient languageClient) {
		this.languageClient = (QuteLanguageClientAPI) languageClient;
		this.capabilityManager = new QuteCapabilityManager(languageClient);
	}

	@Override
	public long getParentProcessId() {
		return parentProcessId != null ? parentProcessId : 0;
	}

	public QuteLanguageService getQuarkusLanguageService() {
		return quteLanguageService;
	}

	public SharedSettings getSharedSettings() {
		return textDocumentService.getSharedSettings();
	}

	@Override
	public CompletableFuture<List<JavaClassInfo>> getJavaClasses(QuteJavaClassesParams params) {
		return getLanguageClient().getJavaClasses(params);
	}

	@Override
	public CompletableFuture<Location> getJavaDefinition(QuteJavaDefinitionParams params) {
		return getLanguageClient().getJavaDefinition(params);
	}

	@Override
	public CompletableFuture<ResolvedJavaClassInfo> getResolvedJavaClass(QuteResolvedJavaClassParams params) {
		return getLanguageClient().getResolvedJavaClass(params);
	}

	@Override
	public CompletableFuture<ProjectInfo> getProjectInfo(QuteProjectParams params) {
		return getLanguageClient().getProjectInfo(params);
	}

	@Override
	public void dataModelChanged(JavaDataModelChangeEvent event) {
		dataModelCache.dataModelChanged(event);
		textDocumentService.dataModelChanged(event);
	}

	public JavaDataModelCache getDataModelCache() {
		return dataModelCache;
	}

	public QuteProjectRegistry getProjectRegistry() {
		return projectRegistry;
	}

	@Override
	public CompletableFuture<ProjectDataModel<TemplateDataModel<ParameterDataModel>>> getProjectDataModel(
			QuteProjectDataModelParams params) {
		return getLanguageClient().getProjectDataModel(params);
	}
}
