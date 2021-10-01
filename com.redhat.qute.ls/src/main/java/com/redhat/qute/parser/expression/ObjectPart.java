package com.redhat.qute.parser.expression;

import com.redhat.qute.parser.expression.Parts.PartKind;
import com.redhat.qute.parser.template.JavaTypeInfoProvider;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.LoopSection;

public class ObjectPart extends Part {

	public ObjectPart(int start, int end) {
		super(start, end);
	}

	@Override
	public PartKind getPartKind() {
		return PartKind.Object;
	}

	public JavaTypeInfoProvider resolveJavaType() {
		String partName = getPartName();
		Template template = super.getOwnerTemplate();
		// Loop for parent section to discover the class name
		Node parent = super.getParent().getParent().getParent();
		while(parent != null) {
			if (parent.getKind() == NodeKind.Section) {
				Section section = (Section) parent;
				if (section.isIterable()) {
					LoopSection iterableSection = (LoopSection) section;
					String alias = iterableSection.getAlias();
					if (partName.equals(alias)) {
						return iterableSection;
					}
				}
			}
			parent = parent.getParent();
		}
		
		// Try to find the class name from parameter declaration
		return template.findParameterByAlias(partName);
	}

}
