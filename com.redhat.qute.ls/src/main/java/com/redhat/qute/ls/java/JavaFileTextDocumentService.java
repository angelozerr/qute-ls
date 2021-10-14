package com.redhat.qute.ls.java;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;

import com.redhat.qute.commons.QuteJavaCodeLensParams;
import com.redhat.qute.ls.AbstractTextDocumentService;
import com.redhat.qute.ls.QuteLanguageServer;
import com.redhat.qute.settings.SharedSettings;

public class JavaFileTextDocumentService extends AbstractTextDocumentService {

	public JavaFileTextDocumentService(QuteLanguageServer quteLanguageServer, SharedSettings sharedSettings) {
		super(quteLanguageServer, sharedSettings);
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {

	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {

	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {

	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {

	}

	public void updateClientCapabilities(ClientCapabilities capabilities) {

	}

	@Override
	public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
		QuteJavaCodeLensParams javaParams = new QuteJavaCodeLensParams(
				params.getTextDocument().getUri());		
		return quteLanguageServer.getLanguageClient().getJavaCodelens(javaParams);
	}
}
