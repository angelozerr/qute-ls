package com.redhat.qute.indexing;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;

import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.utils.StringUtils;

public class QuteProject {

	private final String uri;

	private final Path templateBaseDir;

	public QuteProject(ProjectInfo projectInfo) {
		this.uri = projectInfo.getUri();
		this.templateBaseDir = createPath(projectInfo.getTemplateBaseDir());
	}

	private Path createPath(String baseDir) {
		if (StringUtils.isEmpty(baseDir)) {
			return null;
		}
		if (baseDir.startsWith("file:/")) {
			String convertedUri = baseDir.replace("file:///", "file:/"); //$NON-NLS-1$//$NON-NLS-2$
			convertedUri = baseDir.replace("file://", "file:/"); //$NON-NLS-1$//$NON-NLS-2$
			return new File(URI.create(convertedUri)).toPath();
		}
		return new File(baseDir).toPath();
	}

	public Path getTemplateBaseDir() {
		return templateBaseDir;
	}

	public String getUri() {
		return uri;
	}
}
