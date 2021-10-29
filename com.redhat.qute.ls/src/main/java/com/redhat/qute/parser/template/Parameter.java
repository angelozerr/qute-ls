package com.redhat.qute.parser.template;

public class Parameter extends Node implements JavaTypeInfoProvider {

	private String name = null;

	private String value = null;

	private int startName;

	private int endName;

	private int startValue = -1;

	private int endValue = -1;

	private ExpressionParameter expression;

	private boolean canHaveExpression;

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

	public void setParameterParent(ParametersContainer container) {
		super.setParent((Node) container);
	}

	@Override
	public String toString() {
		return getName() + "=" + getValue();
	}

	/**
	 * Returns the parameter name.
	 * 
	 * @return the parameter name.
	 */
	public String getName() {
		if (name == null) {
			name = getOwnerTemplate().getText(getStartName(), getEndName());
		}
		return name;
	}

	public String getValue() {
		if (value == null) {
			if (hasValueAssigned()) {
				value = getOwnerTemplate().getText(getStartValue(), getEndValue());
			} else {
				value = getName();
			}
		}
		return value;
	}

	public boolean hasValueAssigned() {
		return startValue != -1;
	}

	@Override
	public Expression getJavaTypeExpression() {
		if (!isCanHaveExpression()) {
			return null;
		}
		if (expression != null) {
			return expression;
		}
		// Parameter has name only, the expression is the name
		// ex : items in {#each items}
		int startExpression = getStartName();
		int endExpression = getEndName();
		if (hasValueAssigned()) {
			// Parameter has value, the expression is the value
			// ex : myParent=item.name in {#set myParent=item.name}
			startExpression = getStartValue();
			endExpression = getEndValue();
		}
		expression = new ExpressionParameter(startExpression - 1, endExpression + 1);
		expression.setParent(this);
		return expression;
	}

	public Section getOwnerSection() {
		Node parent = super.getParent();
		if (parent != null && parent.getKind() == NodeKind.Section) {
			return (Section) parent;
		}
		return null;
	}

	@Override
	public Node getJavaTypeOwnerNode() {
		return this;
	}

	public void setCanHaveExpression(boolean canHaveExpression) {
		this.canHaveExpression = canHaveExpression;
	}

	public boolean isCanHaveExpression() {
		return canHaveExpression;
	}

}
