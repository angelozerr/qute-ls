package com.redhat.qute.parser.template.sections;

import com.redhat.qute.parser.template.SectionKind;
import com.redhat.qute.parser.template.Template;

public class EachSection extends LoopSection {

	public static final String TAG = "each";

	public EachSection(int start, int end) {
		super(TAG, start, end);
	}
	
	@Override
	public SectionKind getSectionKind() {
		return SectionKind.EACH;
	}

}
