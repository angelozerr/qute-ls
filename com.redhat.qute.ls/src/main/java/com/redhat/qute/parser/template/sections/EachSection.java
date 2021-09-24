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

	@Override
	public String getIterableElementAlias() {
		return "it";
	}

	@Override
	public String getIterableAlias() {
		Template template = getOwnerTemplate();
		int classNameStart = getIterableAliasStart();
		int classNameEnd = getClassNameEnd();
		return template.getText(classNameStart, classNameEnd).trim();
	}

	public int getIterableAliasStart() {
		return getStartTagOpenOffset() + getTag().length();
	}

	public int getClassNameEnd() {
		Template template = getOwnerTemplate();
		String text = template.getText();
		for (int i = getIterableAliasStart(); i < getStartTagCloseOffset(); i++) {
			char c = text.charAt(i);
			if (c == ' ') {
				//return i;
			}
		}
		return getStartTagCloseOffset();
	}

}
