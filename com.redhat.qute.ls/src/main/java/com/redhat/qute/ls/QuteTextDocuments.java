package com.redhat.qute.ls;

import java.util.function.BiFunction;

import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.ls.api.QuteProjectInfoProvider;
import com.redhat.qute.ls.commons.ModelTextDocuments;
import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.template.Template;

public class QuteTextDocuments extends ModelTextDocuments<Template> {

	private final QuteProjectInfoProvider projectInfoProvider;
	
	public QuteTextDocuments(BiFunction<TextDocument, CancelChecker, Template> parse, QuteProjectInfoProvider projectInfoProvider) {
		super(parse);
		this.projectInfoProvider = projectInfoProvider;
	}

	@Override
	public QuteTextDocument createDocument(TextDocumentItem document) {
		QuteTextDocument doc = new QuteTextDocument(document, parse, projectInfoProvider);
		doc.setIncremental(isIncremental());
		return doc;
	}
}
