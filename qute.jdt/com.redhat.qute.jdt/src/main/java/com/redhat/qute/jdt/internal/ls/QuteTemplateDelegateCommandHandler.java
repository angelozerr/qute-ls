package com.redhat.qute.jdt.internal.ls;

import static com.redhat.qute.jdt.internal.ls.ArgumentUtils.getFirst;
import static com.redhat.qute.jdt.internal.ls.ArgumentUtils.getString;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ls.core.internal.IDelegateCommandHandler;
import org.eclipse.lsp4j.Location;

import com.redhat.qute.commons.JavaClassInfo;
import com.redhat.qute.commons.JavaClassMemberInfo;
import com.redhat.qute.commons.QuteJavaClassMembersParams;
import com.redhat.qute.commons.QuteJavaClassesParams;
import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.jdt.JavaDataModelManager;

/**
 * JDT LS commands used by Qute template.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteTemplateDelegateCommandHandler implements IDelegateCommandHandler {

	private static final String QUTE_TEMPLATE_JAVA_CLASSES_COMMAND_ID = "qute/template/javaClasses";

	private static final String QUTE_TEMPLATE_JAVA_DEFINITION_COMMAND_ID = "qute/template/javaDefinition";

	private static final String QUTE_TEMPLATE_JAVA_CLASS_MEMBERS_COMMAND_ID = "qute/template/javaClassMembers";

	@Override
	public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor monitor) throws Exception {
		switch (commandId) {
		case QUTE_TEMPLATE_JAVA_CLASSES_COMMAND_ID:
			return getJavaClasses(arguments, commandId, monitor);
		case QUTE_TEMPLATE_JAVA_CLASS_MEMBERS_COMMAND_ID:
			return getJavaClassMembers(arguments, commandId, monitor);
		case QUTE_TEMPLATE_JAVA_DEFINITION_COMMAND_ID:
			return getJavaDefinition(arguments, commandId, monitor);
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
	private static List<JavaClassMemberInfo> getJavaClassMembers(List<Object> arguments, String commandId,
			IProgressMonitor monitor) throws JavaModelException, CoreException {
		// Create java file information parameter
		QuteJavaClassMembersParams params = createQuteJavaClassMembersParams(arguments, commandId);
		// Return file information from the parameter
		return JavaDataModelManager.getInstance().getJavaClassMembers(params, JDTUtilsLSImpl.getInstance(), monitor);
	}

	private static QuteJavaClassMembersParams createQuteJavaClassMembersParams(List<Object> arguments,
			String commandId) {
		Map<String, Object> obj = getFirst(arguments);
		if (obj == null) {
			throw new UnsupportedOperationException(String
					.format("Command '%s' must be called with one QuteJavaClassMembersParams argument!", commandId));
		}
		// Get project name from the java file URI
		String javaFileUri = getString(obj, "uri");
		if (javaFileUri == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required QuteJavaClassMembersParams.uri (java file URI)!",
					commandId));
		}
		String className = getString(obj, "className");
		if (className == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required QuteJavaClassMembersParams.className !", commandId));
		}
		QuteJavaClassMembersParams params = new QuteJavaClassMembersParams();
		params.setUri(javaFileUri);
		params.setClassName(className);
		return params;
	}

	private static List<JavaClassInfo> getJavaClasses(List<Object> arguments, String commandId,
			IProgressMonitor monitor) throws JavaModelException, CoreException {
		// Create java file information parameter
		QuteJavaClassesParams params = createQuteJavaClassParams(arguments, commandId);
		// Return file information from the parameter
		return JavaDataModelManager.getInstance().getJavaClasses(params, JDTUtilsLSImpl.getInstance(), monitor);
	}

	private static QuteJavaClassesParams createQuteJavaClassParams(List<Object> arguments, String commandId) {
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
		QuteJavaClassesParams params = new QuteJavaClassesParams();
		params.setUri(javaFileUri);
		params.setPattern(pattern);
		return params;
	}

	/**
	 * Returns the Java definition for the given class / method.
	 *
	 * @param arguments
	 * @param commandId
	 * @param monitor
	 * @return the Java definition for the given class / method.
	 * @throws CoreException
	 * @throws JavaModelException
	 */
	private static Location getJavaDefinition(List<Object> arguments, String commandId, IProgressMonitor monitor)
			throws JavaModelException, CoreException {
		// Create java definition parameter
		QuteJavaDefinitionParams params = createQuteJavaDefinitionParams(arguments, commandId);
		// Return file information from the parameter
		return JavaDataModelManager.getInstance().getJavaDefinition(params, JDTUtilsLSImpl.getInstance(), monitor);
	}

	private static QuteJavaDefinitionParams createQuteJavaDefinitionParams(List<Object> arguments, String commandId) {
		Map<String, Object> obj = getFirst(arguments);
		if (obj == null) {
			throw new UnsupportedOperationException(String
					.format("Command '%s' must be called with one QuteJavaDefinitionParams argument!", commandId));
		}
		// Get project name from the java file URI
		String javaFileUri = getString(obj, "uri");
		if (javaFileUri == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required QuteJavaDefinitionParams.uri (java file URI)!",
					commandId));
		}
		String className = getString(obj, "className");
		if (className == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required QuteJavaDefinitionParams.className !", commandId));
		}
		String method = getString(obj, "method");
		QuteJavaDefinitionParams params = new QuteJavaDefinitionParams();
		params.setUri(javaFileUri);
		params.setClassName(className);
		params.setMethod(method);
		return params;
	}

}
