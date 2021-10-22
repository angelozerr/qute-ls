package com.redhat.qute.indexing;

import java.util.HashMap;
import java.util.Map;

import com.redhat.qute.commons.ProjectInfo;

public class QuteProjectRegistry {

	private final Map<String /* */, QuteProject> projects;

	public QuteProjectRegistry() {
		this.projects = new HashMap<>();
	}

	public QuteProject getProject(String projectUri) {
		return projects.get(projectUri);
	}

	public String registerProject(ProjectInfo projectInfo) {
		String projectUri = projectInfo.getUri();
		QuteProject project = getProject(projectUri);
		if (project == null) {
			registerProjectSync(projectInfo);
		}
		return projectUri;
	}

	public synchronized QuteProject registerProjectSync(ProjectInfo projectInfo) {
		String projectUri = projectInfo.getUri();
		QuteProject project = getProject(projectUri);
		if (project != null) {
			return project;
		}
		project = new QuteProject(projectInfo);
		projects.put(projectUri, project);
		return project;
	}

}
