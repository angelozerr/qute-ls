package com.redhat.qute.parser.template.sections;

import com.redhat.qute.parser.template.Section;

public interface SectionFactory {

	Section createSection(String tag, int start, int end);
}
