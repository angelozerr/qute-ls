package com.redhat.qute.parser.template;

public class Parameter extends Node {

	private String name = null;

	private String value = null;

	private int startName;

	private int endName;

	private int startValue = -1;

	private int endValue = -1;

	public Parameter(int start, int end) {
		super(start, end);
		this.startName = start;
		this.endName = end;
	}

	public int getStartName() {
		return startName;
	}

	public int getEndName() {
		return endName;
	}

	public int getStartValue() {
		return startValue;
	}

	public void setStartValue(int startValue) {
		this.startValue = startValue;
	}

	public int getEndValue() {
		return endValue;
	}

	public void setEndValue(int endValue) {
		this.endValue = endValue;
		super.setEnd(endValue);
	}

	@Override
	public String getNodeName() {
		return getName();
	}

	@Override
	public NodeKind getKind() {
		return NodeKind.Parameter;
	}

	public void setParameterParent(ParametersContainer expression) {
		super.setParent((Node) expression);
	}

	@Override
	public String toString() {
		return getName() + "=" + getValue();
	}

	public String getValue() {
 		if (value == null) {
			if (startValue != -1) {
				value = getOwnerTemplate().getText(getStartValue(), getEndValue());
			} else {
				value = getName();
			}
		}
		return value;
	}

	public String getName() {
		if (name == null) {
			name = getOwnerTemplate().getText(getStartName(), getEndName());
		}
		return name;
	}

}
