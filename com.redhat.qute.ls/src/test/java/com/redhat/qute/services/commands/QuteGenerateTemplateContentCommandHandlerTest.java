package com.redhat.qute.services.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.lsp4j.ExecuteCommandParams;
import org.junit.jupiter.api.Test;

import com.redhat.qute.commons.datamodel.GenerateTemplateInfo;
import com.redhat.qute.commons.datamodel.ParameterDataModel;
import com.redhat.qute.services.datamodel.MockJavaDataModelCache;
import com.redhat.qute.settings.SharedSettings;

public class QuteGenerateTemplateContentCommandHandlerTest {

	@Test
	public void generateItem() throws InterruptedException, ExecutionException, Exception {
		QuteGenerateTemplateContentCommandHandler command = new QuteGenerateTemplateContentCommandHandler(
				new MockJavaDataModelCache());
		ExecuteCommandParams params = new ExecuteCommandParams("", Arrays.asList(createItemInfo()));
		String result = (String) command.executeCommand(params, new SharedSettings(), //
				() -> {
				}).get();
		System.err.println(result);
	}

	
	@Test
	public void generateListItems() throws InterruptedException, ExecutionException, Exception {
		QuteGenerateTemplateContentCommandHandler command = new QuteGenerateTemplateContentCommandHandler(
				new MockJavaDataModelCache());
		ExecuteCommandParams params = new ExecuteCommandParams("", Arrays.asList(createListItemsInfo()));
		String result = (String) command.executeCommand(params, new SharedSettings(), //
				() -> {
				}).get();
		System.err.println(result);
	}

	private GenerateTemplateInfo createListItemsInfo() {
		GenerateTemplateInfo info = new GenerateTemplateInfo();
		info.setProjectUri("project");
		List<ParameterDataModel> parameters = new ArrayList<>();
		ParameterDataModel parameter = new ParameterDataModel();
		parameter.setKey("items");
		parameter.setSourceType("java.util.List<org.acme.Item>");
		parameters.add(parameter);
		info.setParameters(parameters);
		return info;
	}
	
	private GenerateTemplateInfo createItemInfo() {
		GenerateTemplateInfo info = new GenerateTemplateInfo();
		info.setProjectUri("project");
		List<ParameterDataModel> parameters = new ArrayList<>();
		ParameterDataModel parameter = new ParameterDataModel();
		parameter.setKey("item");
		parameter.setSourceType("org.acme.Item");
		parameters.add(parameter);
		info.setParameters(parameters);
		return info;
	}
}
