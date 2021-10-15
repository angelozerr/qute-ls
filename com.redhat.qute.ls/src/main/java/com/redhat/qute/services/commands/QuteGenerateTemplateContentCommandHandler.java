package com.redhat.qute.services.commands;

import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.settings.SharedSettings;

public class QuteGenerateTemplateContentCommandHandler implements IDelegateCommandHandler {

	public static final String COMMAND_ID = "qute.command.generate.template.content";

	@Override
	public Object executeCommand(ExecuteCommandParams params, SharedSettings sharedSettings,
			CancelChecker cancelChecker) throws Exception {
		return "<html></html>";
	}

}
