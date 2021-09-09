package com.redhat.qute.jdt.internal.ls;

import static com.redhat.qute.jdt.internal.ls.ArgumentUtils.getFirst;
import static com.redhat.qute.jdt.internal.ls.ArgumentUtils.getString;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ls.core.internal.IDelegateCommandHandler;

import com.redhat.qute.commons.JavaClassInfo;
import com.redhat.qute.commons.QuteJavaClassParams;
import com.redhat.qute.jdt.JavaDataModelManager;

/**
 * JDT LS commands used by Qute template.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteTemplateDelegateCommandHandler implements IDelegateCommandHandler {

	private static final String QUTE_TEMPLATE_JAVA_CLASSES_COMMAND_ID = "qute/template/javaClasses";

	@Override
	public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor monitor) throws Exception {
		switch (commandId) {
		case QUTE_TEMPLATE_JAVA_CLASSES_COMMAND_ID:
			return getJavaClasses(arguments, commandId, monitor);
		}
		return null;
	}

	/**
	 * Returns the file information (package name, etc) for the given Java file.
	 *
	 * @param arguments
	 * @param commandId
	 * @param monitor
	 * @return the file information (package name, etc) for the given Java file.
	 * @throws CoreException
	 * @throws JavaModelException
	 */
	private static List<JavaClassInfo> getJavaClasses(List<Object> arguments, String commandId,
			IProgressMonitor monitor) throws JavaModelException, CoreException {
		// Create java file information parameter
		QuteJavaClassParams params = createQuteJavaClassParams(arguments, commandId);
		// Return file information from the parameter
		return JavaDataModelManager.getInstance().getJavaClasses(params, JDTUtilsLSImpl.getInstance(), monitor);
	}

	private static QuteJavaClassParams createQuteJavaClassParams(List<Object> arguments, String commandId) {
		Map<String, Object> obj = getFirst(arguments);
		if (obj == null) {
			throw new UnsupportedOperationException(
					String.format("Command '%s' must be called with one QuteJavaClassParams argument!", commandId));
		}
		// Get project name from the java file URI
		String javaFileUri = getString(obj, "uri");
		if (javaFileUri == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required QuteJavaClassParams.uri (java file URI)!", commandId));
		}
		String pattern = getString(obj, "pattern");
		if (pattern == null) {
			throw new UnsupportedOperationException(String
					.format("Command '%s' must be called with required QuteJavaClassParams.pattern !", commandId));
		}
		QuteJavaClassParams params = new QuteJavaClassParams();
		params.setUri(javaFileUri);
		params.setPattern(pattern);
		return params;
	}
}
