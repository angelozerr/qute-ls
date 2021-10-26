package com.redhat.qute.indexing;

import java.nio.file.Path;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

import com.redhat.qute.parser.template.SectionKind;

public class QuteIndex {

	private final String tag;

	private final String parameter;
	private final Position position;

	private final SectionKind kind;

	private final QuteTemplateIndex templateIndex;

	public QuteIndex(String tag, String parameter, Position position, SectionKind kind,
			QuteTemplateIndex templateIndex) {
		this.tag = tag;
		this.parameter = parameter;
		this.position = position;
		this.kind = kind;
		this.templateIndex = templateIndex;
	}

	public String getTag() {
		return tag;
	}

	public String getParameter() {
		return parameter;
	}

	public Position getPosition() {
		return position;
	}

	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.add("tag", getTag());
		b.add("parameter", getParameter());
		b.add("position", getPosition());
		b.add("kind", kind);
		b.add("templateId", getTemplateId());
		return b.toString();
	}

	public Path getTemplatePath() {
		return templateIndex.getPath();
	}

	public String getTemplateId() {
		return templateIndex.getTemplateId();
	}

	public Range getRange() {
		Position start = getPosition();
		int length = parameter != null ? parameter.length() : tag.length();
		Position end = new Position(start.getLine(), start.getCharacter() + length);
		return new Range(start, end);
	}

}
