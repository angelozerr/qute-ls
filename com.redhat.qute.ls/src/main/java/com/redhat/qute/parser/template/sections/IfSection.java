package com.redhat.qute.parser.template.sections;

import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionKind;

public class IfSection extends Section {

	public static final String TAG = "if";
	
	public IfSection(int start, int end) {
		super(TAG, start, end);
	}
	
	@Override
	public SectionKind getSectionKind() {
		return SectionKind.IF;
	}

}
