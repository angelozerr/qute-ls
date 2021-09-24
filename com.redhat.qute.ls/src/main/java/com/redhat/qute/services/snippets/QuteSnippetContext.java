package com.redhat.qute.services.snippets;

import java.util.Map;

import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.services.completions.CompletionRequest;

public abstract class QuteSnippetContext implements IQuteSnippetContext {

	public static final QuteSnippetContext IN_TEXT = new QuteSnippetContext() {

		@Override
		public boolean isMatch(CompletionRequest request, Map<String, String> model) {
			Node node = request.getNode();
			if (node.getKind() == NodeKind.Template || node.getKind() == NodeKind.Text) {
				return true;
			}
			if (node.getKind() == NodeKind.Section) {
				Section section = (Section) node;
				return section.isInStartTagName(request.getOffset());
			}
			return false;				
		}

	};

}
