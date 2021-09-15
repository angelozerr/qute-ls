/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.redhat.qute.commons.JavaClassInfo;
import com.redhat.qute.commons.JavaClassMemberInfo;
import com.redhat.qute.commons.QuteJavaClassesParams;
import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.ls.commons.snippets.SnippetRegistry;
import com.redhat.qute.parser.expression.ObjectPart;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.parser.scanner.Scanner;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.ParameterDeclaration;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.scanner.ScannerState;
import com.redhat.qute.parser.template.scanner.TemplateScanner;
import com.redhat.qute.parser.template.scanner.TokenType;
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

	private static final CompletionList EMPTY_COMPLETION = new CompletionList();

	private static final CompletableFuture<CompletionList> EMPTY_FUTURE_COMPLETION = CompletableFuture
			.completedFuture(EMPTY_COMPLETION);
	private SnippetRegistry snippetRegistry;

	private final JavaDataModelCache javaCache;

	public QuteCompletions(JavaDataModelCache javaCache) {
		this.javaCache = javaCache;
	}

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
	public CompletableFuture<CompletionList> doComplete(Template template, Position position,
			QuteCompletionSettings completionSettings, QuteFormattingSettings formattingSettings,
			CancelChecker cancelChecker) {
		CompletionList list = new CompletionList();
		CompletionRequest completionRequest = null;
		try {
			completionRequest = new CompletionRequest(template, position, completionSettings, formattingSettings);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Creation of CompletionRequest failed", e);
			return EMPTY_FUTURE_COMPLETION;
		}
		Node node = completionRequest.getNode();
		if (node == null) {
			return EMPTY_FUTURE_COMPLETION;
		}
		if (NodeKind.Expression == node.getKind()) {
			return doCompleteExpression(completionRequest, cancelChecker);
		}

		String text = template.getText();
		int offset = completionRequest.getOffset();

		Scanner<TokenType, ScannerState> scanner = TemplateScanner.createScanner(text, node.getStart());

		TokenType token = scanner.scan();
		while (token != TokenType.EOS && scanner.getTokenOffset() <= offset) {
			cancelChecker.checkCanceled();
			switch (token) {
			case StartParameterDeclaration:
				if (scanner.getTokenEnd() == offset) {
					int start = offset;
					int end = offset;
					return collectJavaClassesSuggestions(start, end, template, completionSettings);
				}
				break;
			case ParameterDeclaration:
				if (scanner.getTokenOffset() <= offset && offset <= scanner.getTokenEnd()) {
					int start = scanner.getTokenOffset();
					int end = scanner.getTokenEnd();
					return collectJavaClassesSuggestions(start, end, template, completionSettings);
				}
				break;
			default:
			}

			token = scanner.scan();
		}

		collectSnippetSuggestions(completionRequest, list);
		return CompletableFuture.completedFuture(list);
	}

	private CompletableFuture<CompletionList> collectJavaClassesSuggestions(int start, int end, Template template,
			QuteCompletionSettings completionSettings) {
		String projectUri = template.getProjectUri();
		if (projectUri == null) {
			return EMPTY_FUTURE_COMPLETION;
		}
		String pattern = template.getText(start, end);
		QuteJavaClassesParams params = new QuteJavaClassesParams(pattern, projectUri);
		return javaCache.getJavaClasses(params) //
				.thenApply(result -> {
					if (result == null) {
						return null;
					}
					CompletionList list = new CompletionList();
					list.setItems(new ArrayList<>());

					for (JavaClassInfo javaClassInfo : result) {
						String fullClassName = javaClassInfo.getClassName();
						CompletionItem item = new CompletionItem();
						item.setLabel(fullClassName);
						TextEdit textEdit = new TextEdit();
						Range range = QutePositionUtility.createRange(start, end, template);
						textEdit.setRange(range);

						String parameterDeclaration = fullClassName;
						if (javaClassInfo.isPackage()) {
							item.setKind(CompletionItemKind.Module);
						} else {
							item.setKind(CompletionItemKind.Class);
							int index = fullClassName.lastIndexOf('.');
							String className = index != -1 ? fullClassName.substring(index + 1, fullClassName.length())
									: fullClassName;
							String alias = String.valueOf(className.charAt(0)).toLowerCase()
									+ className.substring(1, className.length());

							StringBuilder insertText = new StringBuilder(fullClassName);
							insertText.append(' ');
							if (completionSettings.isCompletionSnippetsSupported()) {
								item.setInsertTextFormat(InsertTextFormat.Snippet);
								insertText.append("${1:");
								insertText.append(alias);
								insertText.append("}$0");
							} else {
								item.setInsertTextFormat(InsertTextFormat.PlainText);
								insertText.append(alias);
							}
							parameterDeclaration = insertText.toString();

						}
						textEdit.setNewText(parameterDeclaration);
						item.setTextEdit(Either.forLeft(textEdit));
						list.getItems().add(item);
					}
					return list;
				});
	}

	private CompletableFuture<CompletionList> doCompleteExpression(CompletionRequest completionRequest,
			CancelChecker cancelChecker) {
		int offset = completionRequest.getOffset();
		Expression expression = (Expression) completionRequest.getNode();
		Node nodeExpression = expression.findNodeExpressionAt(offset);
		if (nodeExpression == null) {
			// ex : { | }
			return doCompleteExpressionForObjectPart(null, completionRequest);
		}

		if (nodeExpression.getKind() == NodeKind.ExpressionPart) {
			Part part = (Part) nodeExpression;
			switch (part.getPartKind()) {
			case Object:
				// ex : { ite|m }
				return doCompleteExpressionForObjectPart(part, completionRequest);
			case Property:
			case Method:
				// ex : { item.n| }
				// ex : { item.n|ame }
				Parts parts = part.getParent();
				return doCompleteExpressionForMemberPart(part, parts, completionRequest);
			default:
				break;
			}
			return EMPTY_FUTURE_COMPLETION;
		}

		if (nodeExpression.getKind() == NodeKind.ExpressionParts) {
			char previous = completionRequest.getTemplate().getText().charAt(offset - 1);
			switch (previous) {
			case ':': {
				// ex : { data:| }
				// ex : { data:|name }
				Parts parts = (Parts) nodeExpression;
				Part part = parts.getPartAt(offset + 1);
				return doCompleteExpressionForObjectPart(part, completionRequest);
			}
			case '.': {
				// ex : { item.| }
				// ex : { item.|name }
				Parts parts = (Parts) nodeExpression;
				Part part = parts.getPartAt(offset + 1);
				return doCompleteExpressionForMemberPart(part, parts, completionRequest);
			}
			}
		}
		return EMPTY_FUTURE_COMPLETION;
	}

	private CompletableFuture<CompletionList> doCompleteExpressionForMemberPart(Part part, Parts parts,
			CompletionRequest completionRequest) {
		ObjectPart objectPart = parts.getObjectPart();
		String className = objectPart.getClassName();
		if (className == null) {
			return EMPTY_FUTURE_COMPLETION;
		}
		int start = part != null ? part.getStart() : parts.getEnd();
		int end = part != null ? part.getEnd() : parts.getEnd();
		Template template = completionRequest.getTemplate();
		int partIndex = parts.getPreviousPartIndex(part);
		return javaCache.getResolvedClass(parts, partIndex, template) //
				.thenApply(resolvedClass -> {
					if (resolvedClass == null) {
						return EMPTY_COMPLETION;
					}
					CompletionList list = new CompletionList();
					list.setItems(new ArrayList<>());
					Range range = QutePositionUtility.createRange(start, end, template);
					for (JavaClassMemberInfo member : resolvedClass.getMembers()) {
						String fullClassName = member.getField();
						if (fullClassName == null) {
							fullClassName = member.getMethod();
						}
						CompletionItem item = new CompletionItem();
						item.setLabel(fullClassName);
						TextEdit textEdit = new TextEdit();
						textEdit.setRange(range);
						textEdit.setNewText(fullClassName);
						item.setTextEdit(Either.forLeft(textEdit));
						list.getItems().add(item);
					}
					return list;

				});

	}

	private CompletableFuture<CompletionList> doCompleteExpressionForObjectPart(Node part,
			CompletionRequest completionRequest) {
		int offset = completionRequest.getOffset();
		// Completion for root object
		int partStart = part != null ? part.getStart() : offset;
		int partEnd = part != null ? part.getEnd() : offset;
		// Collect alias declared from parameter declaration
		Template template = completionRequest.getTemplate();
		List<String> aliases = template.getChildren().stream() //
				.filter(n -> n.getKind() == NodeKind.ParameterDeclaration) //
				.map(n -> ((ParameterDeclaration) n).getAlias()) //
				.filter(alias -> alias != null) //
				.collect(Collectors.toList());
		CompletionList list = new CompletionList();
		for (String alias : aliases) {
			CompletionItem item = new CompletionItem();
			item.setLabel(alias);
			item.setKind(CompletionItemKind.Reference);
			Range range = QutePositionUtility.createRange(partStart, partEnd, template);
			TextEdit textEdit = new TextEdit(range, alias);
			item.setTextEdit(Either.forLeft(textEdit));
			list.getItems().add(item);
		}
		return CompletableFuture.completedFuture(list);
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