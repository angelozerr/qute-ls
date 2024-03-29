package com.redhat.qute.services.commands;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.commons.ResolvedJavaClassInfo;
import com.redhat.qute.commons.datamodel.GenerateTemplateInfo;
import com.redhat.qute.commons.datamodel.ParameterDataModel;
import com.redhat.qute.services.datamodel.JavaDataModelCache;
import com.redhat.qute.settings.SharedSettings;
import com.redhat.qute.utils.IOUtils;

import io.quarkus.qute.Engine;
import io.quarkus.qute.ReflectionValueResolver;
import io.quarkus.qute.Template;

public class QuteGenerateTemplateContentCommandHandler implements IDelegateCommandHandler {

	public static final String COMMAND_ID = "qute.command.generate.template.content";

	private final JavaDataModelCache cache;

	private Engine engine;

	public QuteGenerateTemplateContentCommandHandler(JavaDataModelCache cache) {
		this.cache = cache;
		this.engine = createQuteEngine();
	}

	@Override
	public CompletableFuture<Object> executeCommand(ExecuteCommandParams params, SharedSettings sharedSettings,
			CancelChecker cancelChecker) throws Exception {
		GenerateTemplateInfo info = ArgumentsUtils.getArgAt(params, 0, GenerateTemplateInfo.class);
		String projectUri = info.getProjectUri();
		List<ParameterDataModel> parameters = info.getParameters();

		List<ResolvedParameterDataModel> resolvedParameters = new ArrayList<>(parameters.size());
		List<CompletableFuture<ResolvedJavaClassInfo>> resolvingJavaTypeFutures = new ArrayList<>();
		for (ParameterDataModel parameter : parameters) {
			CompletableFuture<ResolvedJavaClassInfo> future = cache.resolveJavaType(parameter.getSourceType(),
					projectUri);
			resolvedParameters.add(new ResolvedParameterDataModel(parameter, future));
			resolvingJavaTypeFutures.add(future);
		}

		CompletableFuture<Void> allFutures = CompletableFuture
				.allOf(resolvingJavaTypeFutures.toArray(new CompletableFuture[resolvingJavaTypeFutures.size()]));
		return allFutures.thenApply(Void -> {

			InputStream in = QuteGenerateTemplateContentCommandHandler.class.getResourceAsStream("generate.qute.html");
			String templateContent = IOUtils.convertStreamToString(in);
			Template template = getEngine().parse(templateContent);
			Object result = template.data("classes", resolvedParameters).render();
			return result;

		});
		/*
		 * .exceptionally(e -> { return e.getMessage(); });
		 */
	}

	private Engine getEngine() {
		if (engine == null) {
			engine = createQuteEngine();
		}
		return engine;
	}

	private synchronized Engine createQuteEngine() {
		if (engine != null) {
			return engine;
		}
		return Engine.builder() //
				.addDefaults()//
				.addValueResolver(new ReflectionValueResolver())//
				.build();
	}

}
