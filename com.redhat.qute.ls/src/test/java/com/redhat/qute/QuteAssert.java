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
package com.redhat.qute;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemCapabilities;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.junit.Assert;

import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.TemplateParser;
import com.redhat.qute.services.JavaDataModelCache;
import com.redhat.qute.services.MockJavaDataModelCache;
import com.redhat.qute.services.QuteLanguageService;
import com.redhat.qute.settings.QuteCompletionSettings;
import com.redhat.qute.settings.QuteFormattingSettings;

/**
 * Qute Assert
 * 
 * @author Angelo ZERR
 *
 */
public class QuteAssert {

	private static final JavaDataModelCache DEFAULT_JAVA_DATA_MODEL_CACHE = new MockJavaDataModelCache();

	// ------------------- Completion assert

	public static void testCompletionFor(String value, CompletionItem... expectedItems) throws Exception {
		testCompletionFor(value, false, expectedItems);
	}

	public static void testCompletionFor(String value, boolean snippetSupport, CompletionItem... expectedItems)
			throws Exception {
		testCompletionFor(value, snippetSupport, null, null, expectedItems);
	}

	public static void testCompletionFor(String value, boolean snippetSupport, Integer expectedCount,
			JavaDataModelCache javaCache, CompletionItem... expectedItems) throws Exception {
		testCompletionFor(value, snippetSupport, "test.qute", "project-qute",  expectedCount, DEFAULT_JAVA_DATA_MODEL_CACHE,
				expectedItems);
	}

	public static void testCompletionFor(String value, boolean snippetSupport, String fileUri, String projectUri, Integer expectedCount,
			JavaDataModelCache javaCache, CompletionItem... expectedItems) throws Exception {
		int offset = value.indexOf('|');
		value = value.substring(0, offset) + value.substring(offset + 1);

		Template template = TemplateParser.parse(value, fileUri);
		template.setProjectUri(projectUri);
		Position position = template.positionAt(offset);

		// Add snippet support for completion
		QuteCompletionSettings completionSettings = new QuteCompletionSettings();
		CompletionItemCapabilities completionItemCapabilities = new CompletionItemCapabilities();
		completionItemCapabilities.setSnippetSupport(snippetSupport);
		CompletionCapabilities completionCapabilities = new CompletionCapabilities(completionItemCapabilities);
		completionSettings.setCapabilities(completionCapabilities);

		QuteFormattingSettings formattingSettings = new QuteFormattingSettings();

		QuteLanguageService languageService = new QuteLanguageService(javaCache);
		CompletionList list = languageService
				.doComplete(template, position, completionSettings, formattingSettings, () -> {
				}).get();

		assertCompletions(list, expectedCount, expectedItems);
	}

	public static void assertCompletions(CompletionList actual, Integer expectedCount,
			CompletionItem... expectedItems) {
		// no duplicate labels
		List<String> labels = actual.getItems().stream().map(i -> i.getLabel()).sorted().collect(Collectors.toList());
		String previous = null;
		for (String label : labels) {
			Assert.assertTrue(
					"Duplicate label " + label + " in " + labels.stream().collect(Collectors.joining(",")) + "}",
					previous != label);
			previous = label;
		}
		if (expectedCount != null) {
			Assert.assertEquals(expectedCount.intValue(), actual.getItems().size());
		}
		if (expectedItems != null) {
			for (CompletionItem item : expectedItems) {
				assertCompletion(actual, item);
			}
		}
	}

	private static void assertCompletion(CompletionList completions, CompletionItem expected) {
		List<CompletionItem> matches = completions.getItems().stream().filter(completion -> {
			return expected.getLabel().equals(completion.getLabel());
		}).collect(Collectors.toList());

		Assert.assertEquals(
				expected.getLabel() + " should only exist once: Actual: "
						+ completions.getItems().stream().map(c -> c.getLabel()).collect(Collectors.joining(",")),
				1, matches.size());

		CompletionItem match = matches.get(0);
		/*
		 * if (expected.documentation != null) {
		 * Assert.assertEquals(match.getDocumentation().getRight().getValue(),
		 * expected.getd); } if (expected.kind) { Assert.assertEquals(match.kind,
		 * expected.kind); }
		 */
		// if (expected.getTextEdit() != null && match.getTextEdit() != null) {
		if (expected.getTextEdit() != null && expected.getTextEdit().getLeft() != null) {
			Assert.assertEquals(expected.getTextEdit().getLeft().getNewText(),
					match.getTextEdit().getLeft().getNewText());
		}
		Range r = expected.getTextEdit() != null && expected.getTextEdit().getLeft() != null
				? expected.getTextEdit().getLeft().getRange()
				: null;
		if (r != null && r.getStart() != null && r.getEnd() != null) {
			Assert.assertEquals(expected.getTextEdit().getLeft().getRange(), match.getTextEdit().getLeft().getRange());
		}
		// }
		if (expected.getFilterText() != null && match.getFilterText() != null) {
			Assert.assertEquals(expected.getFilterText(), match.getFilterText());
		}

		/*
		 * if (expected.getDocumentation() != null) {
		 * Assert.assertEquals(DocumentationUtils.getDocumentationTextFromEither(
		 * expected.getDocumentation()),
		 * DocumentationUtils.getDocumentationTextFromEither(match.getDocumentation()));
		 * }
		 */

	}

	public static CompletionItem c(String newText, Range range) {
		return c(newText, newText, range);
	}

	public static CompletionItem c(String label, String newText, Range range) {
		return c(label, newText, range, null);
	}

	public static CompletionItem c(String label, String newText, Range range, String documentation) {
		return c(label, new TextEdit(range, newText), null,
				documentation != null ? Either.forLeft(documentation) : null);
	}

	private static CompletionItem c(String label, TextEdit textEdit, String filterText,
			Either<String, MarkupContent> documentation) {
		CompletionItem item = new CompletionItem();
		item.setLabel(label);
		item.setFilterText(filterText);
		item.setTextEdit(Either.forLeft(textEdit));
		item.setDocumentation(documentation);
		return item;
	}

	public static Range r(int line, int startChar, int endChar) {
		return r(line, startChar, line, endChar);
	}

	public static Range r(int startLine, int startChar, int endLine, int endChar) {
		Position start = new Position(startLine, startChar);
		Position end = new Position(endLine, endChar);
		return new Range(start, end);
	}

}
