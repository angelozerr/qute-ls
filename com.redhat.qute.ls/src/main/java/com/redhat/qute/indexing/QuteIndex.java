package com.redhat.qute.indexing;

import org.eclipse.lsp4j.Position;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

import com.redhat.qute.parser.template.SectionKind;

public class QuteIndex {

	private final String tag;

	private final String parameter;
	private final Position position;

	private final SectionKind kind;

	public QuteIndex(String tag, String parameter, Position position, SectionKind kind) {
		this.tag = tag;
		this.parameter = parameter;
		this.position = position;
		this.kind = kind;
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
		b.add("tag", tag);
		b.add("parameter", parameter);
		b.add("position", position);
		b.add("kind", kind);
		return b.toString();
	}

}
