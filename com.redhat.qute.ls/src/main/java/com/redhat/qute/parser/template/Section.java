package com.redhat.qute.parser.template;

import java.util.ArrayList;
import java.util.List;

public class Section extends Node {

	private String tag;

	private int startTagOpenOffset;

	private int startTagCloseOffset;

	private int endTagOpenOffset;

	private int endTagCloseOffset;

	private boolean selfClosed;

	private List<Parameter> parameters;

	Section(int start, int end) {
		super(start, end);
	}

	@Override
	public NodeKind getKind() {
		return NodeKind.Section;
	}

	public int getStartTagOpenOffset() {
		return startTagOpenOffset;
	}

	void setStartTagOpenOffset(int startTagOpenOffset) {
		this.startTagOpenOffset = startTagOpenOffset;
	}

	public int getStartTagCloseOffset() {
		return startTagCloseOffset;
	}

	void setStartTagCloseOffset(int startTagCloseOffset) {
		this.startTagCloseOffset = startTagCloseOffset;
	}

	public String getTag() {
		return tag;
	}

	void setTag(String tag) {
		this.tag = tag;
	}

	public int getEndTagOpenOffset() {
		return endTagOpenOffset;
	}

	void setEndTagOpenOffset(int endTagOpenOffset) {
		this.endTagOpenOffset = endTagOpenOffset;
	}

	public int getEndTagCloseOffset() {
		return endTagCloseOffset;
	}

	void setEndTagCloseOffset(int endTagCloseOffset) {
		this.endTagCloseOffset = endTagCloseOffset;
	}

	public boolean isSelfClosed() {
		return selfClosed;
	}

	void setSelfClosed(boolean selfClosed) {
		this.selfClosed = selfClosed;
	}

	@Override
	public String getNodeName() {
		return "#" + getTag();
	}

	public boolean isInStartTagName(int offset) {
		if (startTagOpenOffset == NULL_VALUE || startTagCloseOffset == NULL_VALUE) {
			// case <|
			return true;
		}
		if (offset > startTagOpenOffset && offset <= startTagCloseOffset - (hasStartTag() ? getTag().length() : 0)) {
			// case <bean | >
			return true;
		}
		return false;
	}

	public boolean isInStartTag(int offset) {
		if (startTagOpenOffset == NULL_VALUE || startTagCloseOffset == NULL_VALUE) {
			// case <|
			return true;
		}
		if (offset > startTagOpenOffset && offset <= startTagCloseOffset) {
			// case <bean | >
			return true;
		}
		return false;
	}

	public boolean isInEndTag(int offset) {
		return isInEndTag(offset, false);
	}

	public boolean isInEndTag(int offset, boolean afterBackSlash) {
		if (endTagOpenOffset == NULL_VALUE) {
			// case >|
			return false;
		}
		if (offset > endTagOpenOffset + (afterBackSlash ? 1 : 0) && offset < getEnd()) {
			// case </bean | >
			return true;
		}
		return false;
	}

	/**
	 * Returns true if has a start tag.
	 *
	 * In our source-oriented DOM, a lone end tag will cause a node to be created in
	 * the tree, unlike well-formed-only DOMs.
	 *
	 * @return true if has a start tag.
	 */
	public boolean hasStartTag() {
		return getStartTagOpenOffset() != NULL_VALUE;
	}

	/**
	 * Returns true if has an end tag.
	 *
	 * In our source-oriented DOM, sometimes Elements are "ended", even without an
	 * explicit end tag in the source.
	 *
	 * @return true if has an end tag.
	 */
	public boolean hasEndTag() {
		return getEndTagOpenOffset() != NULL_VALUE;
	}

	public SectionKind getSectionKind() {
		return SectionKind.get(getTag());
	}

	public List<Parameter> getParameters() {
		if (parameters == null) {
			this.parameters = parseParameters(this);
		}
		return this.parameters;
	}

	public Parameter getParameterAt(int index) {
		List<Parameter> parameters = getParameters();
		if (parameters.size() > index) {
			return parameters.get(index);
		}
		return null;
	}

	private List<Parameter> parseParameters(Section section) {
		List<Parameter> parameters = new ArrayList<>();
		return parameters;
	}

}
