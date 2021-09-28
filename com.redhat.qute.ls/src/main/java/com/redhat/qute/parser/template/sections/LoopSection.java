package com.redhat.qute.parser.template.sections;

import java.util.Arrays;
import java.util.List;

import com.redhat.qute.parser.template.JavaTypeInfoProvider;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.ParameterDeclaration;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionMetadata;
import com.redhat.qute.parser.template.Template;

public abstract class LoopSection extends Section implements JavaTypeInfoProvider {

	private static final String DEFAULT_ALIAS = "it";

	private static final int ALIAS_PARAMETER_INDEX = 0;

	private static final int ITERABLE_PARAMETER_INDEX = 2;

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

	public String getAlias() {
		int nbParameters = getParameters().size();
		if (nbParameters >= 3) {
			Parameter alias = getParameterAt(ALIAS_PARAMETER_INDEX);
			if (alias != null) {
				return alias.getValue();
			}
		}
		return DEFAULT_ALIAS;
	}

	public String getIterable() {
		int nbParameters = getParameters().size();
		if (nbParameters >= 2) {
			Parameter iterable = getParameterAt(ITERABLE_PARAMETER_INDEX);
			if (iterable != null) {
				return iterable.getValue();
			}
		} else {
			Parameter iterable = getParameterAt(0);
			if (iterable != null) {
				return iterable.getValue();
			}
		}
		return null;
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
				// return i;
			}
		}
		return getStartTagCloseOffset();
	}

	@Override
	public boolean isIterable() {
		return true;
	}

	public String getClassName() {
		String iterable = getIterable();
		if (iterable == null) {
			return null;
		}
		Template template = getOwnerTemplate();
		// Try to find the class name from parameter declaration
		ParameterDeclaration parameter = template.findParameterByAlias(iterable);
		return parameter != null ? parameter.getClassName() : null;
	}

	@Override
	public Node getNode() {
		return this;
	}
}
