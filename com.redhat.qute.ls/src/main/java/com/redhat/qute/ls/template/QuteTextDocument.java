package com.redhat.qute.ls.template;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.QuteProjectParams;
import com.redhat.qute.indexing.QuteProject;
import com.redhat.qute.indexing.QuteProjectRegistry;
import com.redhat.qute.indexing.TemplateProvider;
import com.redhat.qute.ls.api.QuteProjectInfoProvider;
import com.redhat.qute.ls.commons.ModelTextDocument;
import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.TemplateDataModelProvider;

public class QuteTextDocument extends ModelTextDocument<Template> implements TemplateProvider {

	private CompletableFuture<ProjectInfo> projectInfoFuture;

	private final QuteProjectInfoProvider projectInfoProvider;

	private final TemplateDataModelProvider dataModelProvider;

	private QuteProjectRegistry projectRegistry;

	private final Path templatePath;

	private String projectUri;

	private String templateId;

	public QuteTextDocument(TextDocumentItem document, BiFunction<TextDocument, CancelChecker, Template> parse,
			QuteProjectInfoProvider projectInfoProvider, QuteProjectRegistry projectRegistry,
			TemplateDataModelProvider dataModelProvider) {
		super(document, parse);
		this.projectInfoProvider = projectInfoProvider;
		this.dataModelProvider = dataModelProvider;
		this.projectRegistry = projectRegistry;
		this.templatePath = QuteProject.createPath(document.getUri());
	}

	@Override
	public CompletableFuture<Template> getModel() {
		return super.getModel() //
				.thenApply(template -> {
					if (template != null && template.getProjectUri() == null) {
						ProjectInfo projectInfo = getProjectInfoFuture().getNow(null);
						if (projectInfo != null) {
							QuteProject project = projectRegistry.getProject(projectInfo);
							template.setProjectUri(project.getUri());
							template.setTemplateId(templateId);
						}
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
						if (projectInfo != null && this.projectUri == null) {
							QuteProject project = projectRegistry.getProject(projectInfo);
							this.projectUri = projectInfo.getUri();
							this.templateId = project.getTemplateId(templatePath);
							projectRegistry.onDidOpenTextDocument(this);
						}
						return projectInfo;
					});
		}
		return projectInfoFuture;
	}

	@Override
	public CompletableFuture<Template> getTemplate() {
		return getModel();
	}

	@Override
	public String getTemplateId() {
		if (templateId != null) {
			return templateId;
		}
		getProjectInfoFuture().getNow(null);
		return null;
	}

	@Override
	public String getProjectUri() {
		if (projectUri != null) {
			return projectUri;
		}
		getProjectInfoFuture().getNow(null);
		return null;
	}
}