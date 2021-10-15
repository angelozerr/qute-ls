package com.redhat.qute.jdt.internal.java;

import static com.redhat.qute.jdt.internal.QuteJavaConstants.CHECKED_TEMPLATE_ANNOTATION;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.OLD_CHECKED_TEMPLATE_ANNOTATION;

import java.text.MessageFormat;
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
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Range;

import com.redhat.qute.jdt.QuteCommandConstants;
import com.redhat.qute.jdt.utils.AnnotationUtils;
import com.redhat.qute.jdt.utils.IJDTUtils;
import com.redhat.qute.jdt.utils.JDTQuteUtils;

public class QuteJavaCodeLensCollector extends ASTVisitor {

	private static final Logger LOGGER = Logger.getLogger(QuteJavaCodeLensCollector.class.getName());

	private static final String QUTE_COMMAND_OPEN_URI_MESSAGE = "Open `{0}`";

	private static final String QUTE_COMMAND_GENERATE_TEMPLATE_MESSAGE = "Create `{0}`";

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
					List body = node.bodyDeclarations();
					for (Object declaration : body) {
						if (declaration instanceof MethodDeclaration) {
							addTemplatePathCodeLens((MethodDeclaration) declaration);
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
								String templateFilePathWithExtension = JDTQuteUtils.getTemplatePath(null, location);
								addTemplatePathCodeLens(node, templateFilePathWithExtension, false);
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
			String templateFilePathWithoutExtension = JDTQuteUtils.getTemplatePath(null, fieldName);
			addTemplatePathCodeLens(node, templateFilePathWithoutExtension, true);
		}
	}

	private void addTemplatePathCodeLens(MethodDeclaration methodDeclaration) {
		String className = typeRoot.getElementName();
		if (className.endsWith(".java")) {
			className = className.substring(0, className.length() - ".java".length());
		}
		String methodName = methodDeclaration.getName().getIdentifier();
		String templateFilePathWithoutExtension = JDTQuteUtils.getTemplatePath(className, methodName);
		addTemplatePathCodeLens(methodDeclaration, templateFilePathWithoutExtension, true);
	}

	private void addTemplatePathCodeLens(ASTNode node, String templateFilePathWithoutExtension,
			boolean withoutExtension) {
		try {
			IFile templateFile = null;
			String templateFilePath = templateFilePathWithoutExtension;
			IProject project = typeRoot.getJavaProject().getProject();
			if (withoutExtension) {
				templateFilePath = templateFilePathWithoutExtension + ".qute.html";
				templateFile = project.getFile(templateFilePath);
				if (!templateFile.exists()) {
					String htmlTtemplateFilePath = templateFilePathWithoutExtension + ".html";
					IFile htmlTemplateFile = project.getFile(htmlTtemplateFilePath);
					if (htmlTemplateFile.exists()) {
						templateFilePath = htmlTtemplateFilePath;
						templateFile = htmlTemplateFile;
					}
				}
			} else {
				templateFile = project.getFile(templateFilePath);
			}
			Command command = null;
			if (templateFile.exists()) {
				command = new Command(MessageFormat.format(QUTE_COMMAND_OPEN_URI_MESSAGE, templateFilePath), //
						QuteCommandConstants.QUTE_COMMAND_OPEN_URI,
						Arrays.asList(templateFile.getLocationURI().toString()));
			} else {
				command = new Command(
						MessageFormat.format(QUTE_COMMAND_GENERATE_TEMPLATE_MESSAGE, templateFilePath), //
						QuteCommandConstants.QUTE_COMMAND_GENERATE_TEMPLATE_FILE,
						Arrays.asList(templateFile.getLocationURI().toString(), templateFilePath));
			}
			Range range = utils.toRange(typeRoot, node.getStartPosition(), node.getLength());
			CodeLens codeLens = new CodeLens(range, command, null);
			lenses.add(codeLens);
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while creating Qute CodeLens for Java file.", e);
		}
	}
}
