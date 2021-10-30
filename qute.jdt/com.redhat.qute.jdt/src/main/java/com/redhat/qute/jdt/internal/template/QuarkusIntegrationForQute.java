package com.redhat.qute.jdt.internal.template;

import static com.redhat.qute.jdt.internal.QuteJavaConstants.CHECKED_TEMPLATE_ANNOTATION;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.OLD_CHECKED_TEMPLATE_ANNOTATION;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.TEMPLATE_CLASS;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.TEMPLATE_EXTENSION_ANNOTATION;
import static com.redhat.qute.jdt.internal.template.CheckedTemplateSupport.collectTemplateDataModelForCheckedTemplate;
import static com.redhat.qute.jdt.internal.template.TemplateFieldSupport.collectTemplateDataModelForTemplateField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IField;
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

import com.redhat.qute.commons.datamodel.ParameterDataModel;
import com.redhat.qute.commons.datamodel.ProjectDataModel;
import com.redhat.qute.commons.datamodel.TemplateDataModel;
import com.redhat.qute.jdt.utils.AnnotationUtils;
import com.redhat.qute.jdt.utils.JDTTypeUtils;

/**
 * Support for Quarkus integration for Qute which collect parameters information
 * (a name and a Java type) for Qute template. This collect uses several
 * strategies :
 * 
 * <ul>
 * <li>@CheckedTemplate support: collect parameters for Qute Template by
 * searching @CheckedTemplate annotation.</li>
 * <li>Template field support: collect parameters for Qute Template by searching
 * Template instance declared as field in Java class.</li>
 * <li>Template extension support: see
 * https://quarkus.io/guides/qute-reference#template_extension_methods</li>
 * </ul>
 * 
 * @author Angelo ZERR
 * 
 * @see https://quarkus.io/guides/qute-reference#quarkus_integration
 * @see https://quarkus.io/guides/qute-reference#typesafe_templates
 * @see https://quarkus.io/guides/qute-reference#template_extension_methods
 */
public class QuarkusIntegrationForQute {

	private static final Logger LOGGER = Logger.getLogger(QuarkusIntegrationForQute.class.getName());

	private static final List<String> javaPrimitiveTypes = Arrays.asList("boolean", "byte", "double", "float", "int",
			"long");

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

		SearchEngine engine = new SearchEngine();
		SearchPattern searchPattern = createQuteSearchPattern(javaProject);
		engine.search(searchPattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() },
				createQuteSearchScope(javaProject), new SearchRequestor() {

					@Override
					public void acceptSearchMatch(SearchMatch match) throws CoreException {
						if (match.getElement() instanceof IType) {
							IType type = (IType) match.getElement();
							if (AnnotationUtils.hasAnnotation(type, CHECKED_TEMPLATE_ANNOTATION)
									|| AnnotationUtils.hasAnnotation(type, OLD_CHECKED_TEMPLATE_ANNOTATION)) {
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
								collectTemplateDataModelForCheckedTemplate(type, templates, monitor);
							} else if (AnnotationUtils.hasAnnotation(type, TEMPLATE_EXTENSION_ANNOTATION)) {
								// See https://quarkus.io/guides/qute-reference#template_extension_methods
								// TODO
							}
						} else if (match.getElement() instanceof IField) {
							IField field = (IField) match.getElement();
							collectTemplateDataModelForTemplateField(field, templates, monitor);
						} else if (match.getElement() instanceof IMethod) {
							IMethod method = (IMethod) match.getElement();
							//collect(field, templates, monitor);
						}
					}

				}, monitor);

		return templates;
	}

	private static IJavaSearchScope createQuteSearchScope(IJavaProject javaProject) throws JavaModelException {
		IJavaProject[] projects = new IJavaProject[] { javaProject };
		int scope = IJavaSearchScope.SOURCES;
		return SearchEngine.createJavaSearchScope(projects, scope);
	}

	private static SearchPattern createQuteSearchPattern(IJavaProject javaProject) {

		// Pattern for io.quarkus.qute.CheckedTemplate
		SearchPattern checkedTemplatePattern = SearchPattern.createPattern(CHECKED_TEMPLATE_ANNOTATION,
				IJavaSearchConstants.ANNOTATION_TYPE, IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE,
				SearchPattern.R_EXACT_MATCH);
		// Pattern for (old annotation) io.quarkus.qute.api.CheckedTemplate
		SearchPattern oldCheckedTemplatePattern = SearchPattern.createPattern(OLD_CHECKED_TEMPLATE_ANNOTATION,
				IJavaSearchConstants.ANNOTATION_TYPE, IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE,
				SearchPattern.R_EXACT_MATCH);

		// Pattern to retrieve Template field
		SearchPattern templateFieldPattern = null;
		IType templateJavaType = JDTTypeUtils.findType(javaProject, TEMPLATE_CLASS);
		if (templateJavaType != null) {
			templateFieldPattern = SearchPattern.createPattern(templateJavaType, IJavaSearchConstants.REFERENCES);
		}

		// Pattern for io.quarkus.qute.TemplateExtension
		SearchPattern templateExtensionPattern = SearchPattern.createPattern(TEMPLATE_EXTENSION_ANNOTATION,
				IJavaSearchConstants.ANNOTATION_TYPE, IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE,
				SearchPattern.R_EXACT_MATCH);

		SearchPattern pattern = SearchPattern.createOrPattern(checkedTemplatePattern, oldCheckedTemplatePattern);
		if (templateFieldPattern != null) {
			pattern = SearchPattern.createOrPattern(pattern, templateFieldPattern);
		}
		return SearchPattern.createOrPattern(pattern, templateExtensionPattern);
	}

	public static String resolveSignature(ILocalVariable methodParameter, IType type) {
		return resolveSignature(methodParameter.getTypeSignature(), type);
	}

	private static String resolveSignature(String signature, IType type) {
		int start = signature.indexOf('<');
		if (start == -1) {
			// No generic
			return resolveSignature2(signature, type);
		}

		String mainSignature = resolveSignature2(signature, type);
		int end = signature.indexOf('>', start);
		String genericSignature = resolveSignature2(signature.substring(start + 1, end), type);
		return mainSignature + "<" + genericSignature + ">";
	}

	private static String resolveSignature2(String signature, IType type) {
		String resolvedSignatureWithoutPackage = Signature.toString(signature);
		if (javaPrimitiveTypes.contains(resolvedSignatureWithoutPackage)) {
			return resolvedSignatureWithoutPackage;
		}

		try {
			String[][] resolvedSignature = type.resolveType(resolvedSignatureWithoutPackage);
			if (resolvedSignature != null) {
				return resolvedSignature[0][0] + "." + resolvedSignature[0][1];
			}
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while resolving signature '" + resolvedSignatureWithoutPackage + "'.", e);
		}
		return resolvedSignatureWithoutPackage;
	}
}
