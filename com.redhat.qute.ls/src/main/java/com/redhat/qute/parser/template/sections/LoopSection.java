package com.redhat.qute.parser.template.sections;

import static com.redhat.qute.parser.template.ParameterInfo.EMPTY;

import java.util.Arrays;
import java.util.List;

import com.redhat.qute.parser.template.JavaTypeInfoProvider;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.ParameterInfo;
import com.redhat.qute.parser.template.ParametersInfo;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionMetadata;
import com.redhat.qute.parser.template.Template;

public abstract class LoopSection extends Section implements JavaTypeInfoProvider {

	private static final String DEFAULT_ALIAS = "it";

	private static final String ALIAS = "alias";

	private static final String IN = "in";

	private static final String ITERABLE = "iterable";

	private static final int ALIAS_PARAMETER_INDEX = 0;

	private static final int ITERABLE_PARAMETER_INDEX = 2;

	private static final List<SectionMetadata> METADATA = Arrays.asList(//
			new SectionMetadata("count", Integer.class.getName()), //
			new SectionMetadata("index", Integer.class.getName()), //
			new SectionMetadata("indexParity", String.class.getName()), //
			new SectionMetadata("hasNext", Boolean.class.getName()), //
			new SectionMetadata("isOdd", Boolean.class.getName()), //
			new SectionMetadata("odd", Boolean.class.getName()), //
			new SectionMetadata("isEven", Boolean.class.getName()), //
			new SectionMetadata("even", Boolean.class.getName()));

	private static final ParametersInfo PARAMETER_INFOS = ParametersInfo.builder() //
			.addParameter(ALIAS, EMPTY) //
			.addParameter(IN, EMPTY) //
			.addParameter(new ParameterInfo(ITERABLE, null, true)) //
			.build();

	public LoopSection(String tag, int start, int end) {
		super(tag, start, end);
	}

	@Override
	public List<SectionMetadata> getMetadata() {
		return METADATA;
	}

	public String getAlias() {
		Parameter alias = getAliasParameter();
		if (alias != null) {
			return alias.getValue();
		}
		return DEFAULT_ALIAS;
	}

	public Parameter getAliasParameter() {
		int nbParameters = getParameters().size();
		if (nbParameters >= 3) {
			return getParameterAt(ALIAS_PARAMETER_INDEX);
		}
		return null;
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
		JavaTypeInfoProvider javaTypeInfoProvider = template.findInInitialDataModel(iterable);
		return javaTypeInfoProvider != null ? javaTypeInfoProvider.getClassName() : null;
	}

	@Override
	public Node getNode() {
		return this;
	}

	@Override
	public ParametersInfo getParametersInfo() {
		return PARAMETER_INFOS;
	}
}
