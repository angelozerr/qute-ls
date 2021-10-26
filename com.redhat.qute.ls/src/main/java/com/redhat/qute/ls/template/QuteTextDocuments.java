package com.redhat.qute.ls.template;

import java.util.function.BiFunction;

import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.indexing.QuteProjectRegistry;
import com.redhat.qute.ls.api.QuteProjectInfoProvider;
import com.redhat.qute.ls.commons.ModelTextDocument;
import com.redhat.qute.ls.commons.ModelTextDocuments;
import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.TemplateDataModelProvider;

public class QuteTextDocuments extends ModelTextDocuments<Template> {

	private final QuteProjectInfoProvider projectInfoProvider;

	private final QuteProjectRegistry projectRegistry;

	private final TemplateDataModelProvider dataModelProvider;

	public QuteTextDocuments(BiFunction<TextDocument, CancelChecker, Template> parse,
			QuteProjectInfoProvider projectInfoProvider, QuteProjectRegistry projectRegistry,
			TemplateDataModelProvider dataModelProvider) {
		super(parse);
		this.projectInfoProvider = projectInfoProvider;
		this.projectRegistry = projectRegistry;
		this.dataModelProvider = dataModelProvider;
	}

	@Override
	public QuteTextDocument createDocument(TextDocumentItem document) {
		QuteTextDocument doc = new QuteTextDocument(document, parse, projectInfoProvider, projectRegistry,
				dataModelProvider);
		doc.setIncremental(isIncremental());
		return doc;
	}
	
	@Override
	public ModelTextDocument<Template> onDidOpenTextDocument(DidOpenTextDocumentParams params) {		
		QuteTextDocument document = (QuteTextDocument) super.onDidOpenTextDocument(params);
		projectRegistry.onDidOpenTextDocument(document);
		return document;
	}
	
	@Override
	public ModelTextDocument<Template> onDidCloseTextDocument(DidCloseTextDocumentParams params) {
		QuteTextDocument document = (QuteTextDocument) super.onDidCloseTextDocument(params);
		projectRegistry.onDidCloseTextDocument(document);
		return document;
	}
}
