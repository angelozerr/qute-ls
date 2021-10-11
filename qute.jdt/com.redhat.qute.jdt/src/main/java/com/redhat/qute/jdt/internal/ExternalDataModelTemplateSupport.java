package com.redhat.qute.jdt.internal;

import static com.redhat.qute.jdt.internal.QuteAnnotationConstants.CHECKED_TEMPLATE_ANNOTATION;
import static com.redhat.qute.jdt.internal.QuteAnnotationConstants.TEMPLATE_EXTENSION_ANNOTATION;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.search.BasicSearchEngine;

import com.redhat.qute.commons.datamodel.ParameterDataModel;
import com.redhat.qute.commons.datamodel.ProjectDataModel;
import com.redhat.qute.commons.datamodel.TemplateDataModel;
import com.redhat.qute.jdt.utils.AnnotationUtils;

/**
 * External data model support:
 * <ul>
 * <li>CheckedTemplate support: see
 * https://quarkus.io/guides/qute-reference#typesafe_templates</li>
 * <li>
 * <li>Template extension support: see
 * https://quarkus.io/guides/qute-reference#template_extension_methods</li>
 * </ul>
 * 
 * @author Angelo ZERR
 * 
 * @see https://quarkus.io/guides/qute-reference#typesafe_templates
 * @see https://quarkus.io/guides/qute-reference#template_extension_methods
 */
public class ExternalDataModelTemplateSupport {

	private static final Logger LOGGER = Logger.getLogger(ExternalDataModelTemplateSupport.class.getName());

	public static ProjectDataModel<TemplateDataModel<ParameterDataModel>> getProjectDataModel(IJavaProject javaProject,
			IProgressMonitor monitor) throws CoreException {
		ProjectDataModel<TemplateDataModel<ParameterDataModel>> project = new ProjectDataModel<TemplateDataModel<ParameterDataModel>>();
		List<TemplateDataModel<ParameterDataModel>> templates = collectTemplatesDataModel(javaProject, monitor);
		project.setTemplates(templates);
		return project;
	}

	private static List<TemplateDataModel<ParameterDataModel>> collectTemplatesDataModel(IJavaProject javaProject,
			IProgressMonitor monitor) throws CoreException {
		List<TemplateDataModel<ParameterDataModel>> templates = new ArrayList<>();

		// Scan Java sources to get all classed annotated with @CheckedTemplate
		SearchPattern pattern = SearchPattern.createPattern(CHECKED_TEMPLATE_ANNOTATION,
				IJavaSearchConstants.ANNOTATION_TYPE, IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE,
				SearchPattern.R_EXACT_MATCH);
		SearchEngine engine = new SearchEngine();
		int searchScope = IJavaSearchScope.SOURCES;
		IJavaSearchScope scope = BasicSearchEngine.createJavaSearchScope(true, new IJavaElement[] { javaProject },
				searchScope);

		engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope,
				new SearchRequestor() {

					@Override
					public void acceptSearchMatch(SearchMatch match) throws CoreException {
						if (match.getElement() instanceof IType) {
							IType type = (IType) match.getElement();

							if (AnnotationUtils.hasAnnotation(type, CHECKED_TEMPLATE_ANNOTATION)) {
								// See https://quarkus.io/guides/qute-reference#typesafe_templates

								// The Java type class is annotated with @CheckedTemplate
								// Example:
								//
								// @CheckedTemplate
								// public static class Templates {
								// public static native TemplateInstance book(Book book);
								// public static native TemplateInstance books(List<Book> books);
								// }

								// Collect for each methods (book, books) a template data model
								collectTemplateDataModelForCheckedTemplate(type, templates);
							} else if (AnnotationUtils.hasAnnotation(type, TEMPLATE_EXTENSION_ANNOTATION)) {
								// See https://quarkus.io/guides/qute-reference#template_extension_methods
								// TODO
							}
						}
					}
				}, monitor);

		return templates;
	}

	private static void collectTemplateDataModelForCheckedTemplate(IType type,
			List<TemplateDataModel<ParameterDataModel>> templates) throws JavaModelException {
		String className = type.getCompilationUnit() != null ? type.getCompilationUnit().getElementName()
				: type.getClassFile().getElementName();
		if (className.endsWith(".java")) {
			className = className.substring(0, className.length() - ".java".length());
		}
		// Loop for each methods (book, book) and create a template data model per
		// method.
		IMethod[] methods = type.getMethods();
		for (IMethod method : methods) {
			TemplateDataModel<ParameterDataModel> template = createTemplateDataModel(method, className, type);
			templates.add(template);
		}
	}

	private static TemplateDataModel<ParameterDataModel> createTemplateDataModel(IMethod method, String className,
			IType type) {
		String methodName = method.getElementName();
		// src/main/resources/templates/${className}/${methodName}.qute.html
		String templateUri = new StringBuilder("src/main/resources/templates/") //
				.append(className) //
				.append('/') //
				.append(methodName) //
				.toString();

		// Create template data model with:
		// - template uri : Qute template file which must be bind with data model.
		// - source type : the Java class which defines Templates
		// -
		TemplateDataModel<ParameterDataModel> template = new TemplateDataModel<ParameterDataModel>();
		template.setParameters(new ArrayList<>());
		template.setTemplateUri(templateUri);
		template.setSourceType(type.getFullyQualifiedName());
		template.setSourceMethod(methodName);

		try {
			for (ILocalVariable methodParameter : method.getParameters()) {
				ParameterDataModel parameter = createParameterDataModel(methodParameter, type);
				template.getParameters().add(parameter);
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,
					"Error while getting method template parameter of '" + method.getElementName() + "'.", e);
		}
		return template;
	}

	private static ParameterDataModel createParameterDataModel(ILocalVariable methodParameter, IType type)
			throws JavaModelException {
		String parameterName = methodParameter.getElementName();
		String[][] parameterType = type.resolveType(Signature.toString(methodParameter.getTypeSignature()));
		String[][] genericType = null;
		String signature = methodParameter.getTypeSignature();
		int start = signature.indexOf('<');
		if (start != -1) {
			int end = signature.indexOf('>', start);
			String generic = signature.substring(start + 1, end);
			genericType = type.resolveType(Signature.toString(generic));
		}
		ParameterDataModel parameter = new ParameterDataModel();
		parameter.setKey(parameterName);
		parameter.setSourceType(parameterType[0][0] + "." + parameterType[0][1]);
		if (genericType != null) {
			parameter
					.setSourceType(parameter.getSourceType() + "<" + genericType[0][0] + "." + genericType[0][1] + ">");
		}
		return parameter;
	}

}