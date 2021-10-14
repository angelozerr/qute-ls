package com.redhat.qute.lsp4e.internal.TO_REMOVE;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.codemining.AbstractCodeMiningProvider;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;

import com.redhat.qute.commons.QuteJavaCodeLensParams;
import com.redhat.qute.jdt.QuteSupportForJava;
import com.redhat.qute.lsp4e.QuteLSPPlugin;
import com.redhat.qute.lsp4e.internal.JDTUtilsImpl;

public class QuteCodeLensProviderForJavaFile extends AbstractCodeMiningProvider {

	private CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(IDocument document) {
		return CompletableFutures.computeAsync((cancelChecker) -> {
			IProgressMonitor monitor = getProgressMonitor(cancelChecker);
			URI uri = LSPEclipseUtils.toUri(document);
			QuteJavaCodeLensParams params = new QuteJavaCodeLensParams(uri.toString());
			List<? extends CodeLens> codeLenses = QuteSupportForJava.getInstance().codeLens(params, JDTUtilsImpl.getInstance(), monitor);
			final List<LSPCodeMining> codeLensResults = Collections.synchronizedList(new ArrayList<>());
			for (CodeLens codeLens : codeLenses) {
				if (codeLens != null) {
					try {
						codeLensResults
								.add(new LSPCodeMining(codeLens, document, QuteCodeLensProviderForJavaFile.this));
					} catch (BadLocationException e) {
						QuteLSPPlugin.logException("Error while computing Qute CodeLens for Java file", e);
					}
				}
			}
			return codeLensResults;
		});
	}

	@Override
	public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer,
			IProgressMonitor monitor) {
		return provideCodeMinings(viewer.getDocument());
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