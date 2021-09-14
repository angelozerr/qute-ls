package com.redhat.qute.jdt;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
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
import com.redhat.qute.commons.QuteJavaClassMembersParams;
import com.redhat.qute.commons.QuteJavaClassesParams;
import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.jdt.utils.IJDTUtils;
import com.redhat.qute.jdt.utils.JDTTypeUtils;

public class JavaDataModelManager {

	private static final JavaDataModelManager INSTANCE = new JavaDataModelManager();

	public static JavaDataModelManager getInstance() {
		return INSTANCE;
	}

	public List<JavaClassInfo> getJavaClasses(QuteJavaClassesParams params, IJDTUtils utils, IProgressMonitor monitor)
			throws CoreException {
		String fileUri = params.getUri();
		IJavaProject javaProject = getJavaProject(fileUri, utils);
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
			throws JavaModelException {
		String fileUri = params.getUri();
		IJavaProject javaProject = getJavaProject(fileUri, utils);
		if (javaProject == null) {
			return null;
		}
		String className = params.getClassName();
		IType type = javaProject.findType(className, monitor);
		if (type == null) {
			return null;
		}
		return utils.toLocation(type);
	}

	public List<JavaClassMemberInfo> getJavaClassMembers(QuteJavaClassMembersParams params, IJDTUtils utils,
			IProgressMonitor monitor) throws JavaModelException {
		String fileUri = params.getUri();
		IJavaProject javaProject = getJavaProject(fileUri, utils);
		if (javaProject == null) {
			return null;
		}
		String className = params.getClassName();
		IType type = javaProject.findType(className, monitor);
		if (type == null) {
			return null;
		}
		List<JavaClassMemberInfo> members = new ArrayList<>();
		IField[] fields = type.getFields();
		for (IField field : fields) {
			JavaClassMemberInfo info = new JavaClassMemberInfo();
			info.setField(field.getElementName());
			info.setType(JDTTypeUtils.getResolvedTypeName(field));
			members.add(info);
		}
		return members;
	}

	private IJavaProject getJavaProject(String fileUri, IJDTUtils utils) {
		IFile file = utils.findFile(fileUri);
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

}
