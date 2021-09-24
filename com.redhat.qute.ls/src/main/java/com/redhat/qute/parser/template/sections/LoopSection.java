package com.redhat.qute.parser.template.sections;

import java.util.Arrays;
import java.util.List;

import com.redhat.qute.parser.template.JavaTypeInfoProvider;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.ParameterDeclaration;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionMetadata;
import com.redhat.qute.parser.template.Template;

public abstract class LoopSection extends Section implements JavaTypeInfoProvider {

	private static final List<SectionMetadata> METADATA = Arrays.asList(//
			new SectionMetadata("count", "count"), //
			new SectionMetadata("index", "index"), //
			new SectionMetadata("indexParity", "indexParity"), //
			new SectionMetadata("hasNext", "hasNext"), //
			new SectionMetadata("isOdd", "isOdd"), //
			new SectionMetadata("odd", "odd"), //
			new SectionMetadata("isEven", "isEven"), //
			new SectionMetadata("even", "even"));

	public LoopSection(String tag, int start, int end) {
		super(tag, start, end);
	}

	@Override
	public List<SectionMetadata> getMetadata() {
		return METADATA;
	}

	public abstract String getIterableAlias();

	public abstract String getIterableElementAlias();

	@Override
	public boolean isIterable() {
		return true;
	}

	public String getClassName() {
		String alias = getIterableAlias();
		if (alias == null) {
			return null;
		}
		Template template = getOwnerTemplate();
		// Try to find the class name from parameter declaration
		ParameterDeclaration parameter = template.findParameterByAlias(alias);
		return parameter != null ? parameter.getClassName() : null;
	}
	
	@Override
	public Node getNode() {
		return this;
	}
}
