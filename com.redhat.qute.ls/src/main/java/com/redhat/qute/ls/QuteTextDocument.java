package com.redhat.qute.ls;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.QuteProjectParams;
import com.redhat.qute.ls.api.QuteProjectInfoProvider;
import com.redhat.qute.ls.commons.ModelTextDocument;
import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.template.Template;

public class QuteTextDocument extends ModelTextDocument<Template> {

	private CompletableFuture<ProjectInfo> projectInfoFuture;

	private final QuteProjectInfoProvider projectInfoProvider;

	public QuteTextDocument(TextDocumentItem document, BiFunction<TextDocument, CancelChecker, Template> parse,
			QuteProjectInfoProvider projectInfoProvider) {
		super(document, parse);
		this.projectInfoProvider = projectInfoProvider;
	}

	private String getProjectUri() {
		if (projectInfoFuture == null || projectInfoFuture.isCompletedExceptionally()
				|| projectInfoFuture.isCancelled()) {
			QuteProjectParams params = new QuteProjectParams(super.getUri());
			projectInfoFuture = projectInfoProvider.getProjectInfo(params);
		}
		ProjectInfo projectInfo = projectInfoFuture.getNow(null);
		return projectInfo != null ? projectInfo.getUri() : null;
	}

	@Override
	public CompletableFuture<Template> getModel() {
		return super.getModel() //
				.thenApply(template -> {
					if (template != null) {
						template.setProjectUri(getProjectUri());
					}
					return template;
				});
	}
}