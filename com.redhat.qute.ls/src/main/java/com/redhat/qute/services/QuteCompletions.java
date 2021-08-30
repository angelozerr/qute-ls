/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.ls.commons.snippets.SnippetRegistry;
import com.redhat.qute.parser.Node;
import com.redhat.qute.parser.Template;
import com.redhat.qute.services.snippets.IQuteSnippetContext;
import com.redhat.qute.settings.QuteCompletionSettings;
import com.redhat.qute.settings.QuteFormattingSettings;
import com.redhat.qute.utils.QutePositionUtility;

/**
 * The Qute completions
 * 
 * @author Angelo ZERR
 *
 */
class QuteCompletions {

	private static final Logger LOGGER = Logger.getLogger(QuteCompletions.class.getName());
	private boolean snippetsLoaded;
	private SnippetRegistry snippetRegistry;

	/**
	 * Returns completion list for the given position
	 * 
	 * @param template           the Qute template
	 * @param position           the position where completion was triggered
	 * @param completionSettings the completion settings.
	 * @param formattingSettings the formatting settings.
	 * @param cancelChecker      the cancel checker
	 * @return completion list for the given position
	 */
	public CompletionList doComplete(Template template, Position position, QuteCompletionSettings completionSettings,
			QuteFormattingSettings formattingSettings, CancelChecker cancelChecker) {
		CompletionList list = new CompletionList();
		CompletionRequest completionRequest = null;
		try {
			completionRequest = new CompletionRequest(template, position, completionSettings, formattingSettings);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Creation of CompletionRequest failed", e);
			return list;
		}

		collectSnippetSuggestions(completionRequest, list);
		return list;
	}

	/**
	 * Collect snippets suggestions.
	 *
	 * @param completionRequest  completion request.
	 * @param completionResponse completion response.
	 */
	private void collectSnippetSuggestions(CompletionRequest completionRequest, CompletionList list) {
		Node node = completionRequest.getNode();
		int offset = completionRequest.getOffset();
		Template template = node.getOwnerTemplate();
		String text = template.getText();
		int endExpr = offset;
		// compute the from for search expression according to the node
		int fromSearchExpr = getExprLimitStart(node, endExpr);
		// compute the start expression
		int startExpr = getExprStart(text, fromSearchExpr, endExpr);
		try {
			Range replaceRange = getReplaceRange(startExpr, endExpr, offset, template);
			String lineDelimiter = template.lineDelimiter(replaceRange.getStart().getLine());
			List<CompletionItem> snippets = getSnippetRegistry().getCompletionItems(replaceRange, lineDelimiter,
					completionRequest.canSupportMarkupKind(MarkupKind.MARKDOWN),
					completionRequest.isCompletionSnippetsSupported(), (context, model) -> {
						if (context instanceof IQuteSnippetContext) {
							return (((IQuteSnippetContext) context).isMatch(completionRequest, model));
						}
						return false;
					}, (suffix) -> {
						// Search the suffix from the right of completion offset.
						for (int i = endExpr; i < text.length(); i++) {
							char ch = text.charAt(i);
							if (Character.isWhitespace(ch)) {
								// whitespace, continue to eat character
								continue;
							} else {
								// the current character is not a whitespace, search the suffix index
								Integer eatIndex = getSuffixIndex(text, suffix, i);
								if (eatIndex != null) {
									try {
										return template.positionAt(eatIndex);
									} catch (BadLocationException e) {
										return null;
									}
								}
								return null;
							}
						}
						return null;
					});
			for (CompletionItem completionItem : snippets) {
				list.getItems().add(completionItem);
			}

		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In QuteCompletions, collectSnippetSuggestions position error", e);
		}
	}

	private static Integer getSuffixIndex(String text, String suffix, final int initOffset) {
		int offset = initOffset;
		char ch = text.charAt(offset);
		// Try to search the first character which matches the suffix
		Integer suffixIndex = null;
		for (int j = 0; j < suffix.length(); j++) {
			if (suffix.charAt(j) == ch) {
				suffixIndex = j;
				break;
			}
		}
		if (suffixIndex != null) {
			// There is one of character of the suffix
			offset++;
			if (suffixIndex == suffix.length()) {
				// the suffix index is the last character of the suffix
				return offset;
			}
			// Try to eat the most characters of the suffix
			for (; offset < text.length(); offset++) {
				suffixIndex++;
				if (suffixIndex == suffix.length()) {
					// the suffix index is the last character of the suffix
					return offset;
				}
				ch = text.charAt(offset);
				if (suffix.charAt(suffixIndex) != ch) {
					return offset;
				}
			}
			return offset;
		}
		return null;
	}

	/**
	 * Returns the limit start offset of the expression according to the current
	 * node.
	 *
	 * @param currentNode the node.
	 * @param offset      the offset.
	 * @return the limit start offset of the expression according to the current
	 *         node.
	 */
	private static int getExprLimitStart(Node currentNode, int offset) {
		if (currentNode == null) {
			// should never occurs
			return 0;
		}
		// if (currentNode.isText()) {
		return currentNode.getStart();
		// }
	}

	private static Range getReplaceRange(int replaceStart, int replaceEnd, int offset, Template template)
			throws BadLocationException {
		if (replaceStart > offset) {
			replaceStart = offset;
		}
		return QutePositionUtility.createRange(replaceStart, replaceEnd, template);
	}

	private SnippetRegistry getSnippetRegistry() {
		if (snippetRegistry == null) {
			snippetRegistry = new SnippetRegistry();
		}
		return snippetRegistry;
	}

	private static int getExprStart(String value, int from, int to) {
		if (to == 0) {
			return to;
		}
		int index = to - 1;
		while (index > 0) {
			if (Character.isWhitespace(value.charAt(index))) {
				return index + 1;
			}
			if (index <= from) {
				return from;
			}
			index--;
		}
		return index;
	}

}