package com.redhat.qute.lsp4e.internal.commands;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4e.command.LSPCommandHandler;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.swt.widgets.Display;

public class GenerateTemplateFile extends LSPCommandHandler {

	@Override
	public Object execute(ExecutionEvent event, Command command, IPath path) throws ExecutionException {
		IFile javaFile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		IProject project = javaFile.getProject();
		List<LanguageServer> servers = LanguageServiceAccessor.getLanguageServers(project,
				capabilities -> Boolean.TRUE);
		for (LanguageServer languageServer : servers) {
			try {
				ExecuteCommandParams params = new ExecuteCommandParams();
				params.setCommand("qute.command.generate.template.content");
				params.setArguments(command.getArguments());
				languageServer.getWorkspaceService().executeCommand(params) //
						.thenApply(fileContents -> {
							try {
								String templateFilePath = command.getArguments().get(1).toString();
								IFile templateFile = project.getFile(templateFilePath);
								templateFile.create(new ByteArrayInputStream(((String) fileContents).getBytes()), true,
										null);
								Display.getDefault().asyncExec(new Runnable() {
									@Override
									public void run() {
										OpenUri.openFile(templateFile);
									}
								});
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							return null;
						});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
