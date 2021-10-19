/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.search.BasicSearchEngine;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.lsp4j.Location;

import com.redhat.qute.commons.JavaClassInfo;
import com.redhat.qute.commons.JavaFieldInfo;
import com.redhat.qute.commons.JavaMethodInfo;
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
import com.redhat.qute.jdt.internal.template.QuarkusIntegrationForQute;
import com.redhat.qute.jdt.utils.IJDTUtils;
import com.redhat.qute.jdt.utils.JDTMethodUtils;
import com.redhat.qute.jdt.utils.JDTQuteUtils;
import com.redhat.qute.jdt.utils.JDTTypeUtils;

/**
 * Qute support for Template file.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteSupportForTemplate {

	private static final Logger LOGGER = Logger.getLogger(QuteSupportForTemplate.class.getName());

	private static final QuteSupportForTemplate INSTANCE = new QuteSupportForTemplate();

	public static QuteSupportForTemplate getInstance() {
		return INSTANCE;
	}

	public ProjectInfo getProjectInfo(QuteProjectParams params, IJDTUtils utils, IProgressMonitor monitor) {
		IJavaProject javaProject = getJavaProjectFromTemplateFile(params.getTemplateFileUri(), utils);
		if (javaProject == null) {
			return null;
		}
		return new ProjectInfo(JDTQuteUtils.getProjectUri(javaProject));
	}

	public ProjectDataModel<TemplateDataModel<ParameterDataModel>> getProjectDataModel(
			QuteProjectDataModelParams params, IJDTUtils instance, IProgressMonitor monitor) throws CoreException {
		String projectUri = params.getProjectUri();
		IJavaProject javaProject = getJavaProjectFromProjectUri(projectUri);
		if (javaProject == null) {
			return null;
		}
		return QuarkusIntegrationForQute.getProjectDataModel(javaProject, monitor);
	}

	public List<JavaClassInfo> getJavaClasses(QuteJavaClassesParams params, IJDTUtils utils, IProgressMonitor monitor)
			throws CoreException {
		String projectUri = params.getProjectUri();
		IJavaProject javaProject = getJavaProjectFromProjectUri(projectUri);
		if (javaProject == null) {
			return null;
		}

		String className = params.getPattern() != null ? params.getPattern() + "*" : "*";
		if (StringUtils.isEmpty(className)) {
			// return null;
		}

		List<JavaClassInfo> classes = new ArrayList<>();
		SearchPattern pattern = SearchPattern.createPattern(className, IJavaSearchConstants.CLASS, 0,
				SearchPattern.R_CAMELCASE_MATCH);
		pattern = SearchPattern.createOrPattern(pattern, SearchPattern.createPattern(className,
				IJavaSearchConstants.PACKAGE, 0, SearchPattern.R_CAMELCASE_MATCH));
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
							JavaClassInfo classInfo = new JavaClassInfo();
							classInfo.setClassName(type.getFullyQualifiedName('.'));
							classInfo.setUri(utils.toUri(type.getTypeRoot()));
							classes.add(classInfo);
						} else if (match.getElement() instanceof IPackageFragment) {
							IPackageFragment packageFragment = (IPackageFragment) match.getElement();
							JavaClassInfo classInfo = new JavaClassInfo();
							classInfo.setClassName(packageFragment.getElementName());
							classes.add(classInfo);
						}
					}
				}, monitor);
		return classes;
	}

	public Location getJavaDefinition(QuteJavaDefinitionParams params, IJDTUtils utils, IProgressMonitor monitor)
			throws CoreException {
		String projectUri = params.getProjectUri();
		IJavaProject javaProject = getJavaProjectFromProjectUri(projectUri);
		if (javaProject == null) {
			return null;
		}
		String className = params.getClassName();
		IType type = javaProject.findType(className, monitor);
		if (type == null) {
			return null;
		}

		String fieldName = params.getField();
		if (fieldName != null) {
			IField field = type.getField(fieldName);
			return field != null && field.exists() ? utils.toLocation(field) : null;
		}

		String sourceMethod = params.getMethod();
		if (sourceMethod != null) {
			IMethod method = findMethod(type, sourceMethod);
			String sourceMethodParameter = params.getMethodParameter();
			if (sourceMethodParameter != null) {
				ILocalVariable[] parameters = method.getParameters();
				for (ILocalVariable parameter : parameters) {
					if (sourceMethodParameter.equals(parameter.getElementName())) {
						return utils.toLocation(parameter);
					}
				}
				return null;
			}
			return method != null && method.exists() ? utils.toLocation(method) : null;
		}

		return utils.toLocation(type);
	}

	private IMethod findMethod(IType type, String sourceMethod) throws JavaModelException {
		// For the moment we search method only by name
		// FIXME:use method signature to retrieve the proper method (see findMethodOLD)
		IMethod[] methods = type.getMethods();
		for (IMethod method : methods) {
			if (sourceMethod.equals(method.getElementName())) {
				return method;
			}
		}
		return null;
	}

	private IMethod findMethodOLD(IType type, String sourceMethod) throws JavaModelException {
		int startBracketIndex = sourceMethod.indexOf('(');
		String methodName = sourceMethod.substring(0, startBracketIndex);
		// Method signature has been generated with JDT API, so we are sure that we have
		// a ')' character.
		int endBracketIndex = sourceMethod.indexOf(')');
		String methodSignature = sourceMethod.substring(startBracketIndex, endBracketIndex + 1);
		String[] paramTypes = methodSignature.isEmpty() ? CharOperation.NO_STRINGS
				: Signature.getParameterTypes(methodSignature);

		// try findMethod for non constructor. If result is null, findMethod for
		// constructor
		IMethod method = JavaModelUtil.findMethod(methodName, paramTypes, false, type);
		if (method == null) {
			method = JavaModelUtil.findMethod(methodName, paramTypes, true, type);
		}
		return method;
	}

	public ResolvedJavaClassInfo getResolvedJavaClass(QuteResolvedJavaClassParams params, IJDTUtils utils,
			IProgressMonitor monitor) throws CoreException {
		String projectUri = params.getProjectUri();
		IJavaProject javaProject = getJavaProjectFromProjectUri(projectUri);
		if (javaProject == null) {
			return null;
		}
		String className = params.getClassName();
		int index = className.indexOf('<');
		if (index != -1) {
			// ex : java.util.List<org.acme.Item>
			String iterableClassName = className.substring(0, index);
			IType iterableType = javaProject.findType(iterableClassName, monitor);
			if (iterableType == null) {
				return null;
			}

			IType[] interfaces = findImplementedInterfaces(iterableType, monitor);
			boolean iterable = interfaces == null ? false
					: Stream.of(interfaces).anyMatch(
							interfaceType -> "java.lang.Iterable".equals(interfaceType.getFullyQualifiedName()));
			if (!iterable) {
				return null;
			}

			String iterableOf = className.substring(index + 1, className.length() - 1);

			ResolvedJavaClassInfo resolvedClass = new ResolvedJavaClassInfo();
			resolvedClass.setClassName(className);
			resolvedClass.setIterableType(iterableClassName);
			resolvedClass.setIterableOf(iterableOf);
			return resolvedClass;

		}
		IType type = javaProject.findType(className, monitor);
		if (type == null) {
			return null;
		}

		// Collect fields
		List<JavaFieldInfo> fieldsInfo = new ArrayList<>();
		IField[] fields = type.getFields();
		for (IField field : fields) {
			if (isFieldValid(field)) {
				// Only public fields are available
				JavaFieldInfo info = new JavaFieldInfo();
				info.setName(JDTTypeUtils.getSourceField(field));
				info.setType(JDTTypeUtils.getResolvedTypeName(field));
				fieldsInfo.add(info);
			}
		}

		// Collect methods
		List<JavaMethodInfo> methodsInfo = new ArrayList<>();
		IMethod[] methods = type.getMethods();
		for (IMethod method : methods) {
			if (isMethodValid(method)) {
				try {
					JavaMethodInfo info = new JavaMethodInfo();
					info.setSignature(JDTMethodUtils.getMethodSignature(method));
					methodsInfo.add(info);
				} catch (Exception e) {
					LOGGER.log(Level.SEVERE,
							"Error while getting method signature of '" + method.getElementName() + "'.", e);
				}
			}
		}

		// Collect type extensions
		List<String> extendedTypes = null;
		if (type.isInterface()) {
			IType[] interfaces = findImplementedInterfaces(type, monitor);
			if (interfaces != null && interfaces.length > 0) {
				extendedTypes = Stream.of(interfaces) //
						.map(interfaceType -> interfaceType.getFullyQualifiedName()) //
						.collect(Collectors.toList());
			}
		} else {
			ITypeHierarchy typeHierarchy = type.newSupertypeHierarchy(monitor);
			IType[] rootClasses = typeHierarchy.getAllClasses();
			extendedTypes = Stream.of(rootClasses) //
					.map(interfaceType -> interfaceType.getFullyQualifiedName()) //
					.collect(Collectors.toList());

		}

		if (extendedTypes != null) {
			extendedTypes.remove(className);
		}

		ResolvedJavaClassInfo resolvedClass = new ResolvedJavaClassInfo();
		resolvedClass.setClassName(className);
		resolvedClass.setFields(fieldsInfo);
		resolvedClass.setMethods(methodsInfo);
		resolvedClass.setExtendedTypes(extendedTypes);
		return resolvedClass;
	}

	private static boolean isFieldValid(IField field) throws JavaModelException {
		return Flags.isPublic(field.getFlags());
	}

	private static boolean isMethodValid(IMethod method) {
		try {
			if (method.isConstructor()) {
				return false;
			}
			if (!Flags.isPublic(method.getFlags())) {
				return false;
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while checking if '" + method.getElementName() + "' is valid.", e);
			return false;
		}
		return true;
	}

	private static IJavaProject getJavaProjectFromProjectUri(String projectName) {
		if (projectName == null) {
			return null;
		}
		return JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(projectName);
	}

	private static IJavaProject getJavaProjectFromTemplateFile(String templateFileUri, IJDTUtils utils) {
		templateFileUri = templateFileUri.replace("vscode-notebook-cell", "file");
		IFile file = utils.findFile(templateFileUri);
		if (file == null || file.getProject() == null) {
			// The uri doesn't belong to an Eclipse project
			return null;
		}
		// The uri belong to an Eclipse project
		if (!(JavaProject.hasJavaNature(file.getProject()))) {
			// The uri doesn't belong to a Java project
			return null;
		}

		String projectName = file.getProject().getName();
		return JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(projectName);
	}

	private static IType[] findImplementedInterfaces(IType type, IProgressMonitor progressMonitor)
			throws CoreException {
		ITypeHierarchy typeHierarchy = type.newSupertypeHierarchy(progressMonitor);
		return typeHierarchy.getRootInterfaces();
	}

}
