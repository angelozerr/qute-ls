package com.redhat.qute.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.commons.datamodel.ParameterDataModel;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.settings.QuteCodeLensSettings;

class QuteCodeLens {

	private final JavaDataModelCache javaCache;

	public QuteCodeLens(JavaDataModelCache javaCache) {
		this.javaCache = javaCache;
	}

	public CompletableFuture<List<? extends CodeLens>> getCodelens(Template template, QuteCodeLensSettings settings,
			CancelChecker cancelChecker) {
		return javaCache.getTemplateDataModel(template) //
				.thenApply(dataModel -> {
					if (dataModel == null || dataModel.getSourceType() == null) {
						return Collections.emptyList();
					}
					cancelChecker.checkCanceled();
					List<CodeLens> lenses = new ArrayList<>();

					// Method which is bind with the template

					String title = dataModel.getSourceType();
					Range range = new Range(new Position(0, 0), new Position(0, 0));
					Command command = new Command(title, "");
					CodeLens codeLens = new CodeLens(range, command, null);
					lenses.add(codeLens);

					// Parameters of the template
					List<ParameterDataModel> parameters = dataModel.getParameters();
					if (parameters != null) {
						for (ParameterDataModel parameter : parameters) {

							String parameterTitle = parameter.getKey() + " : " + parameter.getSourceType();
							Command parameterCommand = new Command(parameterTitle, "");
							CodeLens parameterCodeLens = new CodeLens(range, parameterCommand, null);
							lenses.add(parameterCodeLens);

						}
					}
					return lenses;
				});
	}

}
