package com.redhat.qute.jdt.internal.ls;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ls.core.internal.IDelegateCommandHandler;

/**
 * JDT LS commands used by Qute template.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteTemplateDelegateCommandHandler implements IDelegateCommandHandler {

	private static final String QUTE_TEMPLATE_COMPLETION_COMMAND_ID = "qute/template/completion";

	@Override
	public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor monitor) throws Exception {
		return null;
	}

}
