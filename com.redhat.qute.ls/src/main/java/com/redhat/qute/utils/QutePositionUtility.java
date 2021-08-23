package com.redhat.qute.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.Node;
import com.redhat.qute.parser.Section;
import com.redhat.qute.parser.Template;

public class QutePositionUtility {

	private static final Logger LOGGER = Logger.getLogger(QutePositionUtility.class.getName());

	public static Location toLocation(LocationLink locationLink) {
		return new Location(locationLink.getTargetUri(), locationLink.getTargetRange());
	}


	public static Range selectStartTagName(Section section) throws BadLocationException {
		Template template = section.getOwnerTemplate();
		int startOffset = section.getStartTagOpenOffset() + 2; // {#
		int endOffset = startOffset + section.getTag().length();
		return createRange(startOffset, endOffset, template);
	}

	public static Range selectEndTagName(Section sectionTag) throws BadLocationException {
		Template template = sectionTag.getOwnerTemplate();
		int startOffset = sectionTag.getEndTagOpenOffset() + 2; // {\
		int endOffset = startOffset + sectionTag.getTag().length();
		return createRange(startOffset, endOffset, template);
	}

	public static Range createRange(int startOffset, int endOffset, Template template) {
		try {
			return new Range(template.positionAt(startOffset), template.positionAt(endOffset));
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "While creating Range the Offset was a BadLocation", e);
			return null;
		}
	}
	
	public static Range toRange(Node node) throws BadLocationException {
		Template template = node.getOwnerTemplate();
		Position start = template.positionAt(node.getStart());
		Position end = template.positionAt(node.getEnd());
		return new Range(start, end);
	}
}
