package com.redhat.qute.indexing;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.lsp4j.Position;

import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionKind;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.utils.StringUtils;

public class QuteProject {

	private final String uri;

	private final Path templateBaseDir;

	private final QuteIndexer indexer;

	private final Map<String /* template id */, TemplateProvider> openedDocuments;

	public QuteProject(ProjectInfo projectInfo) {
		this.uri = projectInfo.getUri();
		this.templateBaseDir = createPath(projectInfo.getTemplateBaseDir());
		this.indexer = new QuteIndexer(this);
		this.openedDocuments = new HashMap<>();
	}

	public static Path createPath(String fileUri) {
		if (StringUtils.isEmpty(fileUri)) {
			return null;
		}
		if (fileUri.startsWith("file:/")) {
			String convertedUri = fileUri.replace("file:///", "file:/"); //$NON-NLS-1$//$NON-NLS-2$
			convertedUri = convertedUri.replace("file://", "file:/"); //$NON-NLS-1$//$NON-NLS-2$
			return new File(URI.create(convertedUri)).toPath();
		}
		return new File(fileUri).toPath();
	}

	public Path getTemplateBaseDir() {
		return templateBaseDir;
	}

	public String getTemplateId(Path templatePath) {
		return templateBaseDir.relativize(templatePath).toString().replace('\\', '/');
	}

	public String getUri() {
		return uri;
	}

	public int findNbreferencesOfInsertTag(String templateId, String tag) {
		indexer.scanAsync();
		List<QuteIndex> indexes = indexer.find(null, tag, null);
		return indexes.size();
	}

	public List<QuteIndex> findInsertTagParameter(String templateId, String insertParamater) {
		TemplateProvider provider = openedDocuments.get(templateId);
		if (provider != null) {
			Template template = provider.getTemplate().getNow(null);
			if (template != null) {
				List<QuteIndex> indexes = new ArrayList<>();
				collectInsert(insertParamater, template, template, indexes);
				return indexes;
			}
			return Collections.emptyList();
		}
		indexer.scanAsync();
		return indexer.find(templateId, "insert", insertParamater);
	}

	public void addDocument(TemplateProvider document) {
		openedDocuments.put(document.getTemplateId(), document);
	}

	public void removeDocument(TemplateProvider document) {
		openedDocuments.remove(document.getTemplateId());
		indexer.scanAsync(true);
	}

	private void collectInsert(String insertParamater, Node parent, Template template, List<QuteIndex> indexes) {
		if (parent.getKind() == NodeKind.Section) {
			Section section = (Section) parent;
			if (section.getSectionKind() == SectionKind.INSERT) {
				Parameter parameter = section.getParameterAt(0);
				if (parameter != null) {
					try {
						if (insertParamater == null || insertParamater.equals(parameter.getValue())) {
							Position position = template.positionAt(parameter.getStart());
							Path path = QuteProject.createPath(template.getUri());
							QuteTemplateIndex templateIndex = new QuteTemplateIndex(path, template.getTemplateId());
							QuteIndex index = new QuteIndex("insert", parameter.getValue(), position,
									SectionKind.INSERT, templateIndex);
							indexes.add(index);
						}
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}
			}

		}
		List<Node> children = parent.getChildren();
		for (Node node : children) {
			collectInsert(insertParamater, node, template, indexes);
		}
	}

}
