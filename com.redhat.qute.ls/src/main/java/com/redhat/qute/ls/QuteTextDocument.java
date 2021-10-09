package com.redhat.qute.ls;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.QuteProjectParams;
import com.redhat.qute.ls.api.QuteProjectDataModelProvider;
import com.redhat.qute.ls.api.QuteProjectInfoProvider;
import com.redhat.qute.ls.commons.ModelTextDocument;
import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.TemplateDataModelProvider;

public class QuteTextDocument extends ModelTextDocument<Template> {

	private CompletableFuture<ProjectInfo> projectInfoFuture;

	private final QuteProjectInfoProvider projectInfoProvider;

	private final TemplateDataModelProvider dataModelProvider;
	
	public QuteTextDocument(TextDocumentItem document, BiFunction<TextDocument, CancelChecker, Template> parse,
			QuteProjectInfoProvider projectInfoProvider, TemplateDataModelProvider dataModelProvider) {
		super(document, parse);
		this.projectInfoProvider = projectInfoProvider;
		this.dataModelProvider = dataModelProvider;
	}

	private String getProjectUri() {
		ProjectInfo projectInfo = getProjectInfoFuture().getNow(null);
		return projectInfo != null ? projectInfo.getUri() : null;
	}

	public CompletableFuture<ProjectInfo> getProjectInfoFuture() {
		if (projectInfoFuture == null || projectInfoFuture.isCompletedExceptionally()
				|| projectInfoFuture.isCancelled()) {
			QuteProjectParams params = new QuteProjectParams(super.getUri());
			projectInfoFuture = projectInfoProvider.getProjectInfo(params);
		}
		return projectInfoFuture;
	}

	@Override
	public CompletableFuture<Template> getModel() {
		return super.getModel() //
				.thenApply(template -> {
					if (template != null) {
						template.setProjectUri(getProjectUri());
						template.setDataModelProvider(dataModelProvider);
					}
					return template;
				});
	}
}