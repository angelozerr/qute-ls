/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
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
package com.redhat.qute.ls;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightParams;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.DocumentLinkParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import com.redhat.qute.settings.SharedSettings;

/**
 * Abstract class for text document service. As MicroProfile LS manages
 * application.properties and java file, we need to implement completion, hover
 * with empty result.
 *
 * @author Angelo ZERR
 *
 */
public abstract class AbstractTextDocumentService implements TextDocumentService {

	protected final QuteLanguageServer quteLanguageServer;

	protected final SharedSettings sharedSettings;

	private boolean hierarchicalDocumentSymbolSupport;

	private boolean definitionLinkSupport;

	public AbstractTextDocumentService(QuteLanguageServer quteLanguageServer, SharedSettings sharedSettings) {
		this.quteLanguageServer = quteLanguageServer;
		this.sharedSettings = sharedSettings;
	}

	/**
	 * Update shared settings from the client capabilities.
	 *
	 * @param capabilities the client capabilities
	 */
	public void updateClientCapabilities(ClientCapabilities capabilities) {
		TextDocumentClientCapabilities textDocumentClientCapabilities = capabilities.getTextDocument();
		if (textDocumentClientCapabilities != null) {
			sharedSettings.getHoverSettings().setCapabilities(textDocumentClientCapabilities.getHover());
			sharedSettings.getCompletionSettings().setCapabilities(textDocumentClientCapabilities.getCompletion());
			hierarchicalDocumentSymbolSupport = textDocumentClientCapabilities.getDocumentSymbol() != null
					&& textDocumentClientCapabilities.getDocumentSymbol().getHierarchicalDocumentSymbolSupport() != null
					&& textDocumentClientCapabilities.getDocumentSymbol().getHierarchicalDocumentSymbolSupport();
			definitionLinkSupport = textDocumentClientCapabilities.getDefinition() != null
					&& textDocumentClientCapabilities.getDefinition().getLinkSupport() != null
					&& textDocumentClientCapabilities.getDefinition().getLinkSupport();
		}
	}

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams position) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<List<DocumentLink>> documentLink(DocumentLinkParams params) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Hover> hover(HoverParams params) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(
			DocumentSymbolParams params) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(
			DefinitionParams params) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> rangeFormatting(DocumentRangeFormattingParams params) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(DocumentHighlightParams params) {
		return CompletableFuture.completedFuture(null);
	}

	public boolean isHierarchicalDocumentSymbolSupport() {
		return hierarchicalDocumentSymbolSupport;
	}

	public boolean isDefinitionLinkSupport() {
		return definitionLinkSupport;
	}

	public SharedSettings getSharedSettings() {
		return sharedSettings;
	}
}
