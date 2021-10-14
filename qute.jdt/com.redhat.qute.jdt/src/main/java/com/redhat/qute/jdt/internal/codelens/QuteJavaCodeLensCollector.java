package com.redhat.qute.jdt.internal.codelens;

import static com.redhat.qute.jdt.internal.QuteAnnotationConstants.CHECKED_TEMPLATE_ANNOTATION;
import static com.redhat.qute.jdt.internal.QuteAnnotationConstants.OLD_CHECKED_TEMPLATE_ANNOTATION;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Range;

import com.redhat.qute.jdt.internal.ExternalDataModelTemplateSupport;
import com.redhat.qute.jdt.utils.AnnotationUtils;
import com.redhat.qute.jdt.utils.IJDTUtils;

public class QuteJavaCodeLensCollector extends ASTVisitor {

	private static final Logger LOGGER = Logger.getLogger(QuteJavaCodeLensCollector.class.getName());

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
							addTemplatePathCodeLens((MethodDeclaration) declaration, node);
						}
					}
				}
			}

		}
		return super.visit(node);
	}

	private void addTemplatePathCodeLens(MethodDeclaration methodDeclaration, TypeDeclaration typeDeclaration) {
		try {
			String className = typeRoot.getElementName();
			if (className.endsWith(".java")) {
				className = className.substring(0, className.length() - ".java".length());
			}
			String methodName = methodDeclaration.getName().getIdentifier();
			String templateFilePathWithoutExtension = ExternalDataModelTemplateSupport.getTemplatePath(className,
					methodName);

			IProject project = typeRoot.getJavaProject().getProject();
			String templateFilePath = templateFilePathWithoutExtension + ".qute.html";
			IFile templateFile = project.getFile(templateFilePath);
			if (!templateFile.exists()) {
				templateFilePath = templateFilePathWithoutExtension + ".html";
				templateFile = project.getFile(templateFilePath);
			}

			Command command = null;
			if (templateFile.exists()) {
				command = new Command(templateFilePath, "");
			} else {
				command = new Command("Click here to create Qute template '" + templateFilePathWithoutExtension + "'",
						"");
			}
			Range range = utils.toRange(typeRoot, methodDeclaration.getStartPosition(), methodDeclaration.getLength());
			CodeLens codeLens = new CodeLens(range, command, null);
			lenses.add(codeLens);
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while creating Qute CodeLens for Java file.", e);
		}
	}
}
