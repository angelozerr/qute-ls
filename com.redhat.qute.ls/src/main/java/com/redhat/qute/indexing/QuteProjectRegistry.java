package com.redhat.qute.indexing;

import java.util.HashMap;
import java.util.Map;

import com.redhat.qute.commons.ProjectInfo;

public class QuteProjectRegistry {

	private final Map<String /* project uri */, QuteProject> projects;

	public QuteProjectRegistry() {
		this.projects = new HashMap<>();
	}

	public QuteProject getProject(String projectUri) {
		return projects.get(projectUri);
	}

	public QuteProject getProject(ProjectInfo projectInfo) {
		String projectUri = projectInfo.getUri();
		QuteProject project = getProject(projectUri);
		if (project == null) {
			project = registerProjectSync(projectInfo);
		}
		return project;
	}

	private synchronized QuteProject registerProjectSync(ProjectInfo projectInfo) {
		String projectUri = projectInfo.getUri();
		QuteProject project = getProject(projectUri);
		if (project != null) {
			return project;
		}
		project = new QuteProject(projectInfo);
		projects.put(projectUri, project);
		return project;
	}

	public void onDidOpenTextDocument(TemplateProvider document) {
		String projectUri = document.getProjectUri();
		if (projectUri != null) {
			QuteProject project = getProject(projectUri);
			if (project != null) {
				project.addDocument(document);
			}
		}
	}

	public void onDidCloseTextDocument(TemplateProvider document) {
		String projectUri = document.getProjectUri();
		if (projectUri != null) {
			QuteProject project = getProject(projectUri);
			if (project != null) {
				project.removeDocument(document);
			}
		}
	}

}
