package com.redhat.qute.lsp4e.internal.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.command.LSPCommandHandler;
import org.eclipse.lsp4j.Command;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.redhat.qute.lsp4e.QuteLSPPlugin;

public class OpenUri extends LSPCommandHandler {

	@Override
	public Object execute(ExecutionEvent event, Command command, IPath path) throws ExecutionException {
		String templateFileUri = command.getArguments().get(0).toString();
		IResource resource = LSPEclipseUtils.findResourceFor(templateFileUri);
		if (resource != null && resource.getType() == IResource.FILE) {
			IFile file = (IFile) resource;
			openFile(file);
		}
		return null;
	}

	public static void openFile(IFile file) {
		IWorkbenchWindow workbench= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (workbench == null) {
			workbench = PlatformUI.getWorkbench().getWorkbenchWindows()[0]; 
		}
		IWorkbenchPage page = workbench.getActivePage();
		try {
			// Open the Qute template file with Generic editor.
			IDE.openEditor(page, file, "org.eclipse.ui.genericeditor.GenericEditor");
		} catch (PartInitException e) {
			QuteLSPPlugin.logException("Error while opening Qute template file", e);
		}
	}

}
