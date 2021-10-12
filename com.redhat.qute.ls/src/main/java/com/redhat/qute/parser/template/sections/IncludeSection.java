package com.redhat.qute.parser.template.sections;

import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionKind;

public class IncludeSection extends Section {

	public static final String TAG = "include";

	public IncludeSection(int start, int end) {
		super(TAG, start, end);
	}

	@Override
	public SectionKind getSectionKind() {
		return SectionKind.INCLUDE;
	}

}
