package com.redhat.qute.ls.template;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.QuteProjectParams;
import com.redhat.qute.indexing.QuteProjectRegistry;
import com.redhat.qute.ls.api.QuteProjectInfoProvider;
import com.redhat.qute.ls.commons.ModelTextDocument;
import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.TemplateDataModelProvider;

public class QuteTextDocument extends ModelTextDocument<Template> {

	private CompletableFuture<ProjectInfo> projectInfoFuture;

	private final QuteProjectInfoProvider projectInfoProvider;

	private final TemplateDataModelProvider dataModelProvider;

	private QuteProjectRegistry projectRegistry;

	public QuteTextDocument(TextDocumentItem document, BiFunction<TextDocument, CancelChecker, Template> parse,
			QuteProjectInfoProvider projectInfoProvider, QuteProjectRegistry projectRegistry,
			TemplateDataModelProvider dataModelProvider) {
		super(document, parse);
		this.projectInfoProvider = projectInfoProvider;
		this.dataModelProvider = dataModelProvider;
		this.projectRegistry = projectRegistry;
	}

	@Override
	public CompletableFuture<Template> getModel() {
		return super.getModel() //
				.thenApply(template -> {
					if (template != null) {
						ProjectInfo projectInfo = getProjectInfoFuture().getNow(null);
						template.setProjectUri(projectInfo != null ? projectInfo.getUri() : null);
						template.setProjectRegistry(projectRegistry);
						template.setDataModelProvider(dataModelProvider);
					}
					return template;
				});
	}

	public CompletableFuture<ProjectInfo> getProjectInfoFuture() {
		if (projectInfoFuture == null || projectInfoFuture.isCompletedExceptionally()
				|| projectInfoFuture.isCancelled()) {
			QuteProjectParams params = new QuteProjectParams(super.getUri());
			projectInfoFuture = projectInfoProvider.getProjectInfo(params) //
					.thenApply(projectInfo -> {
						projectRegistry.registerProject(projectInfo);
						return projectInfo;
					});
		}
		return projectInfoFuture;
	}

}