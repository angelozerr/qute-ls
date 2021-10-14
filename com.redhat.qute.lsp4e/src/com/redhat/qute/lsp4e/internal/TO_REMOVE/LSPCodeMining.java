package com.redhat.qute.lsp4e.internal.TO_REMOVE;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.codemining.LineHeaderCodeMining;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;

public class LSPCodeMining extends LineHeaderCodeMining {

	public LSPCodeMining(CodeLens codeLens, IDocument document, QuteCodeLensProviderForJavaFile provider)
			throws BadLocationException {
		super(codeLens.getRange().getStart().getLine(), document, provider);
		setLabel(getCodeLensString(codeLens));
	}

	protected static String getCodeLensString(CodeLens codeLens) {
		Command command = codeLens.getCommand();
		if (command == null || command.getTitle().isEmpty()) {
			return null;
		}
		return command.getTitle();
	}

	/*
	 * @Override protected CompletableFuture<Void> doResolve(ITextViewer viewer,
	 * IProgressMonitor monitor) { if (this.codeLensOptions.isResolveProvider()) {
	 * return languageServer.thenCompose(ls ->
	 * ls.getTextDocumentService().resolveCodeLens(this.codeLens))
	 * .thenAccept(codeLens -> { this.codeLens = codeLens;
	 * setLabel(getCodeLensString(codeLens)); }); } return
	 * CompletableFuture.completedFuture(null); }
	 */

}