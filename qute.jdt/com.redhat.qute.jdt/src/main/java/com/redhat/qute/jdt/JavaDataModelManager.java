package com.redhat.qute.jdt;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
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
import org.eclipse.lsp4j.Location;

import com.redhat.qute.commons.JavaClassInfo;
import com.redhat.qute.commons.JavaClassMemberInfo;
import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.QuteJavaClassesParams;
import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.commons.QuteProjectParams;
import com.redhat.qute.commons.QuteResolvedJavaClassParams;
import com.redhat.qute.commons.ResolvedJavaClassInfo;
import com.redhat.qute.jdt.utils.IJDTUtils;
import com.redhat.qute.jdt.utils.JDTQuteUtils;
import com.redhat.qute.jdt.utils.JDTTypeUtils;

public class JavaDataModelManager {

	private static final JavaDataModelManager INSTANCE = new JavaDataModelManager();

	public static JavaDataModelManager getInstance() {
		return INSTANCE;
	}

	public ProjectInfo getProjectInfo(QuteProjectParams params, IJDTUtils utils, IProgressMonitor monitor) {
		IJavaProject javaProject = getJavaProjectFromTemplateFile(params.getTemplateFileUri(), utils);
		if (javaProject == null) {
			return null;
		}
		return new ProjectInfo(JDTQuteUtils.getProjectUri(javaProject));
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

		return utils.toLocation(type);
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
			boolean iterable = interfaces == null ? false : Stream.of(interfaces)
					.anyMatch(interfaceType -> "java.lang.Iterable".equals(interfaceType.getFullyQualifiedName()));
			if (!iterable) {
				return null;
			}
			
			String iterableOf= className.substring(index+ 1, className.length() -1);
			
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
		List<JavaClassMemberInfo> members = new ArrayList<>();
		IField[] fields = type.getFields();
		for (IField field : fields) {
			JavaClassMemberInfo info = new JavaClassMemberInfo();
			info.setClassName(field.isBinary() ? field.getClassFile().getElementName()
					: field.getCompilationUnit().getElementName());
			info.setField(field.getElementName());
			info.setType(JDTTypeUtils.getResolvedTypeName(field));
			members.add(info);
		}

		ResolvedJavaClassInfo resolvedClass = new ResolvedJavaClassInfo();
		resolvedClass.setClassName(className);
		resolvedClass.setMembers(members);
		return resolvedClass;
	}

	private static IJavaProject getJavaProjectFromProjectUri(String projectName) {
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
