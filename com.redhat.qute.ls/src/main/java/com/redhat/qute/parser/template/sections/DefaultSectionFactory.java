package com.redhat.qute.parser.template.sections;

import java.util.HashMap;
import java.util.Map;

import com.redhat.qute.parser.template.Section;

public class DefaultSectionFactory implements SectionFactory {

	private static final Map<String, SectionFactory> factoryByTag;

	static {
		factoryByTag = new HashMap<>();
		factoryByTag.put(EachSection.TAG, (tag, start, end) -> new EachSection(start, end));
		factoryByTag.put(ForSection.TAG, (tag, start, end) -> new ForSection(start, end));
		factoryByTag.put(IfSection.TAG, (tag, start, end) -> new IfSection(start, end));
	}

	@Override
	public Section createSection(String tag, int start, int end) {
		SectionFactory factory = factoryByTag.get(tag);
		return factory != null ? factory.createSection(tag, start, end) : new Section(tag, start, end);
	}
}