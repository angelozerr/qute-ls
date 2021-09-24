package com.redhat.qute.parser.template;

public class SectionMetadata {

	private final String name;
	private final String description;

	public SectionMetadata(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

}
