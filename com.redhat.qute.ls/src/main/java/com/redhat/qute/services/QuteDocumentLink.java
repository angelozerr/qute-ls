package com.redhat.qute.services;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.Range;

import com.redhat.qute.parser.Node;
import com.redhat.qute.parser.NodeKind;
import com.redhat.qute.parser.Parameter;
import com.redhat.qute.parser.Section;
import com.redhat.qute.parser.SectionKind;
import com.redhat.qute.parser.Template;
import com.redhat.qute.utils.QutePositionUtility;

public class QuteDocumentLink {

	public List<DocumentLink> findDocumentLinks(Template template) {
		List<DocumentLink> links = new ArrayList<>();
		findDocumentLinks(template, template, links);
		return links;
	}

	private void findDocumentLinks(Node node, Template template, List<DocumentLink> links) {
		List<Node> children = node.getChildren();
		for (Node child : children) {
			if(child.getKind() == NodeKind.Section) {
				Section section = (Section) child;
				if (section.getSectionKind() == SectionKind.INCLUDE) {
					Parameter includedTemplateId = section.getParameterAt(0);
					if (includedTemplateId != null) {
							Range range = QutePositionUtility.createRange(includedTemplateId.getStart(), includedTemplateId.getEnd(), template);
						if (range != null) {
							String target = includedTemplateId.getValue();
							links.add(new DocumentLink(range, target));
						}
					}
				}
			}
		}
	}
}
