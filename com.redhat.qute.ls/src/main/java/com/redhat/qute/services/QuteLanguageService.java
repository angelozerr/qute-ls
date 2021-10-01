/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.commons.ResolvedJavaClassInfo;
import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.settings.QuteCompletionSettings;
import com.redhat.qute.settings.QuteFormattingSettings;
import com.redhat.qute.settings.QuteValidationSettings;

/**
 * The Qute language service.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteLanguageService {

	private final QuteCompletions completions;
	private final QuteHighlighting highlighting;
	private final QuteDefinition definition;
	private final QuteDocumentLink documentLink;
	private final QuteSymbolsProvider symbolsProvider;
	private final QuteDiagnostics diagnostics;

	public QuteLanguageService(JavaDataModelCache javaCache) {
		this.completions = new QuteCompletions(javaCache);
		this.highlighting = new QuteHighlighting();
		this.definition = new QuteDefinition(javaCache);
		this.documentLink = new QuteDocumentLink();
		this.symbolsProvider = new QuteSymbolsProvider();
		this.diagnostics = new QuteDiagnostics(javaCache);
	}

	/**
	 * Returns completion list for the given position
	 * 
	 * @param template           the Qute template
	 * @param position           the position where completion was triggered
	 * @param completionSettings the completion settings.
	 * @param formattingSettings the formatting settings.
	 * @param cancelChecker      the cancel checker
	 * @return completion list for the given position
	 */
	public CompletableFuture<CompletionList> doComplete(Template template, Position position,
			QuteCompletionSettings completionSettings, QuteFormattingSettings formattingSettings,
			CancelChecker cancelChecker) {
		return completions.doComplete(template, position, completionSettings, formattingSettings, cancelChecker);
	}

	public List<DocumentHighlight> findDocumentHighlights(Template template, Position position,
			CancelChecker cancelChecker) {
		return highlighting.findDocumentHighlights(template, position, cancelChecker);
	}

	public CompletableFuture<List<? extends LocationLink>> findDefinition(Template template, Position position,
			CancelChecker cancelChecker) {
		return definition.findDefinition(template, position, cancelChecker);
	}

	public List<DocumentLink> findDocumentLinks(Template template) {
		return documentLink.findDocumentLinks(template);
	}

	public List<DocumentSymbol> findDocumentSymbols(Template template, CancelChecker cancelChecker) {
		return symbolsProvider.findDocumentSymbols(template, cancelChecker);
	}

	public List<SymbolInformation> findSymbolInformations(Template template, CancelChecker cancelChecker) {
		return symbolsProvider.findSymbolInformations(template, cancelChecker);
	}

	/**
	 * Validate the given Qute <code>template</code>.
	 * 
	 * @param template           the Qute template.
	 * @param document
	 * @param validationSettings the validation settings.
	 * @param cancelChecker      the cancel checker.
	 * @return the result of the validation.
	 */
	public List<Diagnostic> doDiagnostics(Template template, TextDocument document,
			QuteValidationSettings validationSettings,
			List<CompletableFuture<ResolvedJavaClassInfo>> resolvingJavaTypeFutures, CancelChecker cancelChecker) {
		return diagnostics.doDiagnostics(template, document, validationSettings, resolvingJavaTypeFutures,
				cancelChecker);
	}

}
