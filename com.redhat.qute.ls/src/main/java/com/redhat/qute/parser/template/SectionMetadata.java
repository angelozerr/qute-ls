package com.redhat.qute.parser.template;

public class SectionMetadata implements JavaTypeInfoProvider{
	

	private final String name;
	private final String type;
	private final String description;

	public SectionMetadata(String name, String type) {
		this(name, type, name);
	}

	public SectionMetadata(String name, String type, String description) {
		this.name = name;
		this.type = type;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String getJavaType() {
		return getType();
	}

	@Override
	public Node getJavaTypeOwnerNode() {
		return null;
	}

}
