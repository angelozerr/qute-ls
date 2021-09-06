package com.redhat.qute.jdt;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
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

import com.redhat.qute.commons.JavaClassInfo;
import com.redhat.qute.commons.QuteJavaClassParams;
import com.redhat.qute.jdt.utils.IJDTUtils;

public class JavaDataModelManager {

	private static final JavaDataModelManager INSTANCE = new JavaDataModelManager();

	public static JavaDataModelManager getInstance() {
		return INSTANCE;
	}

	public List<JavaClassInfo> getJavaClasses(QuteJavaClassParams params, IJDTUtils utils, IProgressMonitor monitor)
			throws CoreException {
		String className =  params.getPattern();
		if(StringUtils.isEmpty(className)) {
			return null;
		}
		IFile file = utils.findFile(params.getUri());
		if (file == null || file.getProject() == null) {
			// The uri doesn't belong to an Eclipse project
			return null;
		}
		// The uri belong to an Eclipse project
		if (!(JavaProject.hasJavaNature(file.getProject()))) {
			// The uri doesn't belong to a Java project
			return null;
		}

		List<JavaClassInfo> classes = new ArrayList<>();

		String projectName = file.getProject().getName();
		IJavaProject javaProject = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(projectName);

		SearchPattern pattern = SearchPattern.createPattern(className, IJavaSearchConstants.CLASS, 0,
				SearchPattern.R_CAMELCASE_MATCH);
		SearchEngine engine = new SearchEngine();
		int searchScope = IJavaSearchScope.SOURCES;
		IJavaSearchScope scope = BasicSearchEngine.createJavaSearchScope(true, new IJavaElement[] { javaProject },
				searchScope);

		engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope,
				new SearchRequestor() {

					@Override
					public void acceptSearchMatch(SearchMatch match) throws CoreException {
						// We collect only references from java code and not from JavaDoc

						// --> In this case ConfigProperties will be collected :
						// @ConfigProperties
						// class A

						// --> In this case ConfigProperties will not be collected :
						// /* Demonstrate {@link ConfigProperties} */
						// class A
						JavaClassInfo classInfo = new JavaClassInfo();
						classInfo.setClassName(match.getElement().toString());
						classes.add(classInfo);

						if (!match.isInsideDocComment()) {

						}
					}
				}, monitor);
		return classes;
	}
}
