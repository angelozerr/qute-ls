package com.redhat.qute.jdt.internal.ls;

import static com.redhat.qute.jdt.internal.ls.ArgumentUtils.getFirst;
import static com.redhat.qute.jdt.internal.ls.ArgumentUtils.getString;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.CodeLens;

import com.redhat.qute.commons.QuteJavaCodeLensParams;
import com.redhat.qute.jdt.QuteSupportForJava;

/**
 * JDT LS commands used by Java files.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteSupportForJavaDelegateCommandHandler extends AbstractQuteDelegateCommandHandler {

	private static final String PROJECT_URI_ATTR = "projectUri";

	private static final String PATTERN_ATTR = "pattern";

	private static final String METHOD_ATTR = "method";
	private static final String METHOD_PARAMETER_ATTR = "methodParameter";

	private static final String FIELD_ATTR = "field";

	private static final String TEMPLATE_FILE_URI_ATTR = "templateFileUri";

	private static final String CLASS_NAME_ATTR = "className";

	private static final String QUTE_JAVA_CODELENS_COMMAND_ID = "qute/java/codeLens";

	@Override
	public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor monitor) throws Exception {
		switch (commandId) {
		case QUTE_JAVA_CODELENS_COMMAND_ID:
			return getCodeLensForJava(arguments, commandId, monitor);
		default:
			return null;
		}
	}

	/**
	 * Returns the code lenses for the given Java file.
	 *
	 * @param arguments
	 * @param commandId
	 * @param monitor
	 * @return the code lenses for the given Java file.
	 * @throws CoreException
	 * @throws JavaModelException
	 */
	private static List<? extends CodeLens> getCodeLensForJava(List<Object> arguments, String commandId,
			IProgressMonitor monitor) throws JavaModelException, CoreException {
		QuteJavaCodeLensParams params = createQuteJavaCodeLensParams(arguments, commandId);
		// Return code lenses from the lens parameter
		return QuteSupportForJava.getInstance().codeLens(params, JDTUtilsLSImpl.getInstance(), monitor);
	}

	/**
	 * Create java code lens parameter from the given arguments map.
	 *
	 * @param arguments
	 * @param commandId
	 *
	 * @return java code lens parameter
	 */
	private static QuteJavaCodeLensParams createQuteJavaCodeLensParams(List<Object> arguments, String commandId) {
		Map<String, Object> obj = getFirst(arguments);
		if (obj == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with one MicroProfileJavaCodeLensParams argument!", commandId));
		}
		String javaFileUri = getString(obj, "uri");
		if (javaFileUri == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required MicroProfileJavaCodeLensParams.uri (java URI)!",
					commandId));
		}
		return new QuteJavaCodeLensParams(javaFileUri);
	}
}
