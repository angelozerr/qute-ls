package com.redhat.qute.ls;

import java.util.function.BiFunction;

import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.ls.api.QuteProjectInfoProvider;
import com.redhat.qute.ls.commons.ModelTextDocuments;
import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.TemplateDataModelProvider;

public class QuteTextDocuments extends ModelTextDocuments<Template> {

	private final QuteProjectInfoProvider projectInfoProvider;

	private final TemplateDataModelProvider dataModelProvider;

	public QuteTextDocuments(BiFunction<TextDocument, CancelChecker, Template> parse,
			QuteProjectInfoProvider projectInfoProvider, TemplateDataModelProvider dataModelProvider) {
		super(parse);
		this.projectInfoProvider = projectInfoProvider;
		this.dataModelProvider = dataModelProvider;
	}

	@Override
	public QuteTextDocument createDocument(TextDocumentItem document) {
		QuteTextDocument doc = new QuteTextDocument(document, parse, projectInfoProvider, dataModelProvider);
		doc.setIncremental(isIncremental());
		return doc;
	}
}
