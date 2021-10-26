package com.redhat.qute.jdt.internal.java;

import static com.redhat.qute.jdt.internal.QuteJavaConstants.CHECKED_TEMPLATE_ANNOTATION;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.OLD_CHECKED_TEMPLATE_ANNOTATION;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Range;

import com.redhat.qute.commons.datamodel.GenerateTemplateInfo;
import com.redhat.qute.commons.datamodel.ParameterDataModel;
import com.redhat.qute.jdt.QuteCommandConstants;
import com.redhat.qute.jdt.utils.AnnotationUtils;
import com.redhat.qute.jdt.utils.IJDTUtils;
import com.redhat.qute.jdt.utils.JDTQuteProjectUtils;

public class QuteJavaCodeLensCollector extends ASTVisitor {

	private static final Logger LOGGER = Logger.getLogger(QuteJavaCodeLensCollector.class.getName());

	private static String[] suffixes = { ".qute.html", ".qute.json", ".qute.txt", ".qute.yaml", ".html", ".json",
			".txt", ".yaml" };

	private static final String QUTE_COMMAND_OPEN_URI_MESSAGE = "Open `{0}`";

	private static final String QUTE_COMMAND_GENERATE_TEMPLATE_MESSAGE = "Create `{0}`";

	private static final String DEFAULT_SUFFIX = ".qute.html";

	private final ITypeRoot typeRoot;
	private final List<CodeLens> lenses;
	private final IJDTUtils utils;
	private final IProgressMonitor monitor;

	public QuteJavaCodeLensCollector(ITypeRoot typeRoot, List<CodeLens> lenses, IJDTUtils utils,
			IProgressMonitor monitor) {
		this.typeRoot = typeRoot;
		this.lenses = lenses;
		this.utils = utils;
		this.monitor = monitor;
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		Type type = node.getType();
		if (type.isSimpleType()) {
			if ("Template".equals(((SimpleType) type).getName().toString())) {
				addTemplatePathCodeLens(node);
			}
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		List modifiers = node.modifiers();
		for (Object modifier : modifiers) {
			if (modifier instanceof MarkerAnnotation) {
				MarkerAnnotation annotation = (MarkerAnnotation) modifier;
				if (AnnotationUtils.isMatchAnnotation(annotation, CHECKED_TEMPLATE_ANNOTATION)
						|| AnnotationUtils.isMatchAnnotation(annotation, OLD_CHECKED_TEMPLATE_ANNOTATION)) {
					// @CheckedTemplate
					// public static class Templates {
					// public static native TemplateInstance book(Book book);
					List body = node.bodyDeclarations();
					for (Object declaration : body) {
						if (declaration instanceof MethodDeclaration) {
							addTemplatePathCodeLens((MethodDeclaration) declaration, node);
						}
					}
				}
			}

		}
		return super.visit(node);
	}

	private void addTemplatePathCodeLens(FieldDeclaration node) {
		List modifiers = node.modifiers();
		if (modifiers != null) {
			for (Object modifier : modifiers) {
				if (modifier instanceof SingleMemberAnnotation) {
					SingleMemberAnnotation annotation = (SingleMemberAnnotation) modifier;
					if ("Location".equals(annotation.getTypeName().getFullyQualifiedName())) {
						Expression expression = annotation.getValue();
						if (expression != null && expression instanceof StringLiteral) {
							String location = ((StringLiteral) expression).getLiteralValue();
							if (StringUtils.isNotBlank(location)) {
								String templateFilePathWithExtension = JDTQuteProjectUtils.getTemplatePath(null,
										location);
								addTemplatePathCodeLens(node, (TypeDeclaration) node.getParent(),
										templateFilePathWithExtension, false);
							}
							return;
						}
					}
				}
			}
		}

		List fragments = node.fragments();
		if (fragments != null && !fragments.isEmpty()) {
			VariableDeclaration variable = (VariableDeclaration) fragments.get(0);
			String fieldName = variable.getName().toString();
			String templateFilePathWithoutExtension = JDTQuteProjectUtils.getTemplatePath(null, fieldName);
			addTemplatePathCodeLens(node, (TypeDeclaration) node.getParent(), templateFilePathWithoutExtension, true);
		}
	}

	private void addTemplatePathCodeLens(MethodDeclaration methodDeclaration, TypeDeclaration type) {
		String className = typeRoot.getElementName();
		if (className.endsWith(".java")) {
			className = className.substring(0, className.length() - ".java".length());
		}
		String methodName = methodDeclaration.getName().getIdentifier();
		String templateFilePathWithoutExtension = JDTQuteProjectUtils.getTemplatePath(className, methodName);
		addTemplatePathCodeLens(methodDeclaration, type, templateFilePathWithoutExtension, true);
	}

	private void addTemplatePathCodeLens(ASTNode node, TypeDeclaration type, String templateFilePathWithoutExtension,
			boolean withoutExtension) {
		try {
			IFile templateFile = null;
			String templateFilePath = templateFilePathWithoutExtension;
			IProject project = typeRoot.getJavaProject().getProject();
			if (withoutExtension) {
				templateFile = getTemplateFile(project, templateFilePathWithoutExtension);
				templateFilePath = templateFile.getLocation().makeRelativeTo(project.getLocation()).toString();
			} else {
				templateFile = project.getFile(templateFilePath);
			}
			Command command = null;
			if (templateFile.exists()) {
				command = new Command(MessageFormat.format(QUTE_COMMAND_OPEN_URI_MESSAGE, templateFilePath), //
						QuteCommandConstants.QUTE_COMMAND_OPEN_URI,
						Arrays.asList(templateFile.getLocationURI().toString()));
			} else {
				List<ParameterDataModel> parameters = createParameters(node);
				GenerateTemplateInfo info = new GenerateTemplateInfo();
				info.setParameters(parameters);
				info.setProjectUri(JDTQuteProjectUtils.getProjectUri(typeRoot.getJavaProject()));
				info.setTemplateFileUri(templateFile.getLocationURI().toString());
				info.setTemplateFilePath(templateFilePath);
				command = new Command(MessageFormat.format(QUTE_COMMAND_GENERATE_TEMPLATE_MESSAGE, templateFilePath), //
						QuteCommandConstants.QUTE_COMMAND_GENERATE_TEMPLATE_FILE, Arrays.asList(info));
			}
			Range range = utils.toRange(typeRoot, node.getStartPosition(), node.getLength());
			CodeLens codeLens = new CodeLens(range, command, null);
			lenses.add(codeLens);
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while creating Qute CodeLens for Java file.", e);
		}
	}

	private IFile getTemplateFile(IProject project, String templateFilePathWithoutExtension) {
		for (String suffix : suffixes) {
			IFile templateFile = project.getFile(templateFilePathWithoutExtension + suffix);
			if (templateFile.exists()) {
				return templateFile;
			}
		}
		return project.getFile(templateFilePathWithoutExtension + DEFAULT_SUFFIX);
	}

	private List<ParameterDataModel> createParameters(ASTNode node) {
		if (node.getNodeType() == ASTNode.FIELD_DECLARATION) {
			return create1((FieldDeclaration) node);
		}
		return create2((MethodDeclaration) node);
	}

	private List<ParameterDataModel> create2(MethodDeclaration method) {
		List<ParameterDataModel> parameters = new ArrayList<>();
		List methodParameters = method.parameters();
		for (Object methodParameter : methodParameters) {
			SingleVariableDeclaration variable = (SingleVariableDeclaration) methodParameter;
			String parameterName = variable.getName().getFullyQualifiedName();
			Type parameterType = variable.getType();
			ITypeBinding binding = parameterType.resolveBinding();
			ParameterDataModel parameter = new ParameterDataModel();
			parameter.setKey(parameterName);
			parameter.setSourceType(binding.getQualifiedName());
			parameters.add(parameter);
		}
		return parameters;
	}

	private List<ParameterDataModel> create1(FieldDeclaration node) {
		List<ParameterDataModel> parameters = new ArrayList<>();
		return parameters;
	}

}
