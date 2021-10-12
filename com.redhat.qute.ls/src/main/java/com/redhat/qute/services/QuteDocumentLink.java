package com.redhat.qute.services;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.Range;

import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionKind;
import com.redhat.qute.parser.template.Template;
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
			if (child.getKind() == NodeKind.Section) {
				Section section = (Section) child;
				if (section.getSectionKind() == SectionKind.INCLUDE) {
					// #include section case:

					// {#include base.qute.html}
					// In this case 'base.qute.html' is a document link
					Parameter includedTemplateId = section.getParameterAt(0);
					if (includedTemplateId != null) {
						Range range = QutePositionUtility.createRange(includedTemplateId.getStart(),
								includedTemplateId.getEnd(), template);
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
