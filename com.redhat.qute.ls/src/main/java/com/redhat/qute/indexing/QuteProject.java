package com.redhat.qute.indexing;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;

import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.utils.StringUtils;

public class QuteProject {

	private final String uri;

	private final Path templateBaseDir;
	
	private final QuteIndexer indexer;

	public QuteProject(ProjectInfo projectInfo) {
		this.uri = projectInfo.getUri();
		this.templateBaseDir = createPath(projectInfo.getTemplateBaseDir());
		this.indexer = new QuteIndexer(templateBaseDir);
	}

	private static Path createPath(String fileUri) {
		if (StringUtils.isEmpty(fileUri)) {
			return null;
		}
		if (fileUri.startsWith("file:/")) {
			String convertedUri = fileUri.replace("file:///", "file:/"); //$NON-NLS-1$//$NON-NLS-2$
			convertedUri = fileUri.replace("file://", "file:/"); //$NON-NLS-1$//$NON-NLS-2$
			return new File(URI.create(convertedUri)).toPath();
		}
		return new File(fileUri).toPath();
	}

	public Path getTemplateBaseDir() {
		return templateBaseDir;
	}

	public String getUri() {
		return uri;
	}
}
