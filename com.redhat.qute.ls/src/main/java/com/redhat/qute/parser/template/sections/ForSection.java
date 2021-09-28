package com.redhat.qute.parser.template.sections;

import com.redhat.qute.parser.template.SectionKind;

public class ForSection extends LoopSection {

	public static final String TAG = "for";

	public ForSection(int start, int end) {
		super(TAG, start, end);
	}

	@Override
	public SectionKind getSectionKind() {
		return SectionKind.FOR;
	}

}
