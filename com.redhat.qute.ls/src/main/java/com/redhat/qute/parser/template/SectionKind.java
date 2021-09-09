package com.redhat.qute.parser.template;

public enum SectionKind {

	EACH, //
	EVAL, //
	IF, //
	ELSE, INCLUDE, INSERT, CUSTOM;

	public static SectionKind get(String tag) {
		if (tag != null) {
			try {
				return SectionKind.valueOf(tag.toUpperCase());
			} catch (Exception e) {
				return SectionKind.CUSTOM;
			}
		}
		return SectionKind.CUSTOM;
	}
}
