package com.redhat.qute.services;

import org.eclipse.lsp4j.Position;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.settings.QuteCompletionSettings;
import com.redhat.qute.settings.QuteFormattingSettings;

public class CompletionRequest {

	private final Template template;
	private final int offset;
	private final Node node;
	private final QuteCompletionSettings completionSettings;

	public CompletionRequest(Template template, Position position, QuteCompletionSettings completionSettings,
			QuteFormattingSettings formattingSettings) throws BadLocationException {
		this.template = template;
		this.completionSettings = completionSettings;
		this.offset = template.offsetAt(position);
		this.node = template.findNodeAt(offset);
	}

	public Template getTemplate() {
		return template;
	}

	public int getOffset() {
		return offset;
	}

	public Node getNode() {
		return node;
	}

	public boolean canSupportMarkupKind(String kind) {
		return completionSettings.canSupportMarkupKind(kind);
	}

	public boolean isCompletionSnippetsSupported() {
		return completionSettings.isCompletionSnippetsSupported();
	}

}
