package com.redhat.qute.jdt.internal.ls;

import static com.redhat.qute.jdt.internal.ls.ArgumentUtils.getFirst;
import static com.redhat.qute.jdt.internal.ls.ArgumentUtils.getString;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.Location;

import com.redhat.qute.commons.JavaClassInfo;
import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.QuteJavaClassesParams;
import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.commons.QuteProjectParams;
import com.redhat.qute.commons.QuteResolvedJavaClassParams;
import com.redhat.qute.commons.ResolvedJavaClassInfo;
import com.redhat.qute.commons.datamodel.ParameterDataModel;
import com.redhat.qute.commons.datamodel.ProjectDataModel;
import com.redhat.qute.commons.datamodel.QuteProjectDataModelParams;
import com.redhat.qute.commons.datamodel.TemplateDataModel;
import com.redhat.qute.jdt.QuteSupportForTemplate;

/**
 * JDT LS commands used by Qute template.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteSupportForTemplateDelegateCommandHandler extends AbstractQuteDelegateCommandHandler {

	private static final String PROJECT_URI_ATTR = "projectUri";

	private static final String PATTERN_ATTR = "pattern";

	private static final String METHOD_ATTR = "method";
	private static final String METHOD_PARAMETER_ATTR = "methodParameter";

	private static final String FIELD_ATTR = "field";

	private static final String TEMPLATE_FILE_URI_ATTR = "templateFileUri";

	private static final String CLASS_NAME_ATTR = "className";

	private static final String QUTE_TEMPLATE_PROJECT_COMMAND_ID = "qute/template/project";

	private static final String QUTE_TEMPLATE_PROJECT_DATA_MODEL_COMMAND_ID = "qute/template/projectDataModel";

	private static final String QUTE_TEMPLATE_JAVA_CLASSES_COMMAND_ID = "qute/template/javaClasses";

	private static final String QUTE_TEMPLATE_JAVA_DEFINITION_COMMAND_ID = "qute/template/javaDefinition";

	private static final String QUTE_TEMPLATE_RESOLVED_JAVA_CLASS_COMMAND_ID = "qute/template/resolvedJavaClass";

	@Override
	public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor monitor) throws Exception {
		switch (commandId) {
		case QUTE_TEMPLATE_PROJECT_COMMAND_ID:
			return getProjectInfo(arguments, commandId, monitor);
		case QUTE_TEMPLATE_PROJECT_DATA_MODEL_COMMAND_ID:
			return getProjectDataModel(arguments, commandId, monitor);
		case QUTE_TEMPLATE_JAVA_CLASSES_COMMAND_ID:
			return getJavaClasses(arguments, commandId, monitor);
		case QUTE_TEMPLATE_RESOLVED_JAVA_CLASS_COMMAND_ID:
			return getResolvedJavaClass(arguments, commandId, monitor);
		case QUTE_TEMPLATE_JAVA_DEFINITION_COMMAND_ID:
			return getJavaDefinition(arguments, commandId, monitor);
		}
		return null;
	}

	private static ProjectInfo getProjectInfo(List<Object> arguments, String commandId, IProgressMonitor monitor) {
		QuteProjectParams params = createQuteProjectParams(arguments, commandId);
		return QuteSupportForTemplate.getInstance().getProjectInfo(params, JDTUtilsLSImpl.getInstance(), monitor);
	}

	private static QuteProjectParams createQuteProjectParams(List<Object> arguments, String commandId) {
		Map<String, Object> obj = getFirst(arguments);
		if (obj == null) {
			throw new UnsupportedOperationException(
					String.format("Command '%s' must be called with one QuteProjectParams argument!", commandId));
		}
		// Get project name from the java file URI
		String templateFileUri = getString(obj, TEMPLATE_FILE_URI_ATTR);
		if (templateFileUri == null) {
			throw new UnsupportedOperationException(String
					.format("Command '%s' must be called with required QuteProjectParams.templateFileUri!", commandId));
		}
		return new QuteProjectParams(templateFileUri);
	}

	private static ProjectDataModel<TemplateDataModel<ParameterDataModel>> getProjectDataModel(List<Object> arguments, String commandId,
			IProgressMonitor monitor) throws CoreException {
		QuteProjectDataModelParams params = createQuteProjectDataModelParams(arguments, commandId);
		return QuteSupportForTemplate.getInstance().getProjectDataModel(params, JDTUtilsLSImpl.getInstance(), monitor);
	}

	private static QuteProjectDataModelParams createQuteProjectDataModelParams(List<Object> arguments,
			String commandId) {
		Map<String, Object> obj = getFirst(arguments);
		if (obj == null) {
			throw new UnsupportedOperationException(String
					.format("Command '%s' must be called with one QuteProjectDataModelParams argument!", commandId));
		}
		// Get project name from the java file URI
		String projectUri = getString(obj, PROJECT_URI_ATTR);
		if (projectUri == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required QuteProjectDataModelParams.projectUri!", commandId));
		}
		return new QuteProjectDataModelParams(projectUri);
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
	private static ResolvedJavaClassInfo getResolvedJavaClass(List<Object> arguments, String commandId,
			IProgressMonitor monitor) throws JavaModelException, CoreException {
		// Create java file information parameter
		QuteResolvedJavaClassParams params = createQuteResolvedJavaClassParams(arguments, commandId);
		// Return file information from the parameter
		return QuteSupportForTemplate.getInstance().getResolvedJavaClass(params, JDTUtilsLSImpl.getInstance(), monitor);
	}

	private static QuteResolvedJavaClassParams createQuteResolvedJavaClassParams(List<Object> arguments,
			String commandId) {
		Map<String, Object> obj = getFirst(arguments);
		if (obj == null) {
			throw new UnsupportedOperationException(String
					.format("Command '%s' must be called with one QuteResolvedJavaClassParams argument!", commandId));
		}
		// Get project name from the java file URI
		String projectUri = getString(obj, PROJECT_URI_ATTR);
		if (projectUri == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required QuteResolvedJavaClassParams.projectUri!", commandId));
		}
		String className = getString(obj, CLASS_NAME_ATTR);
		if (className == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required QuteResolvedJavaClassParams.className!", commandId));
		}
		return new QuteResolvedJavaClassParams(className, projectUri);
	}

	private static List<JavaClassInfo> getJavaClasses(List<Object> arguments, String commandId,
			IProgressMonitor monitor) throws JavaModelException, CoreException {
		// Create java file information parameter
		QuteJavaClassesParams params = createQuteJavaClassParams(arguments, commandId);
		// Return file information from the parameter
		return QuteSupportForTemplate.getInstance().getJavaClasses(params, JDTUtilsLSImpl.getInstance(), monitor);
	}

	private static QuteJavaClassesParams createQuteJavaClassParams(List<Object> arguments, String commandId) {
		Map<String, Object> obj = getFirst(arguments);
		if (obj == null) {
			throw new UnsupportedOperationException(
					String.format("Command '%s' must be called with one QuteJavaClassParams argument!", commandId));
		}
		// Get project name from the java file URI
		String projectUri = getString(obj, PROJECT_URI_ATTR);
		if (projectUri == null) {
			throw new UnsupportedOperationException(String
					.format("Command '%s' must be called with required QuteJavaClassParams.projectUri!", commandId));
		}
		String pattern = getString(obj, PATTERN_ATTR);
		if (pattern == null) {
			throw new UnsupportedOperationException(
					String.format("Command '%s' must be called with required QuteJavaClassParams.pattern!", commandId));
		}
		QuteJavaClassesParams params = new QuteJavaClassesParams(pattern, projectUri);
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
		return QuteSupportForTemplate.getInstance().getJavaDefinition(params, JDTUtilsLSImpl.getInstance(), monitor);
	}

	private static QuteJavaDefinitionParams createQuteJavaDefinitionParams(List<Object> arguments, String commandId) {
		Map<String, Object> obj = getFirst(arguments);
		if (obj == null) {
			throw new UnsupportedOperationException(String
					.format("Command '%s' must be called with one QuteJavaDefinitionParams argument!", commandId));
		}
		// Get project name from the java file URI
		String templateFileUri = getString(obj, PROJECT_URI_ATTR);
		if (templateFileUri == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required QuteJavaDefinitionParams.projectUri !", commandId));
		}
		String className = getString(obj, CLASS_NAME_ATTR);
		if (className == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required QuteJavaDefinitionParams.className!", commandId));
		}
		QuteJavaDefinitionParams params = new QuteJavaDefinitionParams(className, templateFileUri);
		String field = getString(obj, FIELD_ATTR);
		params.setField(field);
		String method = getString(obj, METHOD_ATTR);
		params.setMethod(method);
		String methodParameter = getString(obj, METHOD_PARAMETER_ATTR);
		params.setMethodParameter(methodParameter);
		return params;
	}

}
