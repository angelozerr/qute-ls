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
package com.redhat.qute;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemCapabilities;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticRelatedInformation;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightKind;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverCapabilities;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.indexing.QuteProjectRegistry;
import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.TemplateParser;
import com.redhat.qute.services.QuteLanguageService;
import com.redhat.qute.services.datamodel.JavaDataModelCache;
import com.redhat.qute.services.datamodel.MockJavaDataModelCache;
import com.redhat.qute.services.diagnostics.IQuteErrorCode;
import com.redhat.qute.settings.QuteCodeLensSettings;
import com.redhat.qute.settings.QuteCompletionSettings;
import com.redhat.qute.settings.QuteFormattingSettings;
import com.redhat.qute.settings.SharedSettings;
import com.redhat.qute.utils.StringUtils;

/**
 * Qute Assert
 * 
 * @author Angelo ZERR
 *
 */
public class QuteAssert {

	private static final String QUTE_SOURCE = "qute";

	private static final String PROJECT_URI = "project-qute";

	private static final String TEMPLATE_BASE_DIR = "src/test/resources/templates";

	private static final String FILE_URI = "test.qute";

	private static final JavaDataModelCache DEFAULT_JAVA_DATA_MODEL_CACHE = new MockJavaDataModelCache();

	// ------------------- Completion assert

	public static void testCompletionFor(String value, CompletionItem... expectedItems) throws Exception {
		testCompletionFor(value, false, expectedItems);
	}

	public static void testCompletionFor(String value, Integer expectedCount, CompletionItem... expectedItems)
			throws Exception {
		testCompletionFor(value, true, expectedCount, expectedItems);
	}

	public static void testCompletionFor(String value, boolean snippetSupport, CompletionItem... expectedItems)
			throws Exception {
		testCompletionFor(value, snippetSupport, null, expectedItems);
	}

	public static void testCompletionFor(String value, boolean snippetSupport, Integer expectedCount,
			CompletionItem... expectedItems) throws Exception {
		testCompletionFor(value, snippetSupport, FILE_URI, PROJECT_URI, expectedCount, DEFAULT_JAVA_DATA_MODEL_CACHE,
				expectedItems);
	}

	public static void testCompletionFor(String value, boolean snippetSupport, String fileUri, String projectUri,
			Integer expectedCount, JavaDataModelCache javaCache, CompletionItem... expectedItems) throws Exception {
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

		// no duplicate labels
		List<String> labels = list.getItems().stream().map(i -> i.getLabel()).sorted().collect(Collectors.toList());
		String previous = null;
		for (String label : labels) {
			if (expectedCount != null) {
				continue;
			}
			assertNotEquals(previous, label, () -> {
				return "Duplicate label " + label + " in " + labels.stream().collect(Collectors.joining(",")) + "}";
			});
			previous = label;
		}
		if (expectedCount != null) {
			assertEquals(expectedCount.intValue(), list.getItems().size());
		}
		if (expectedItems != null) {
			for (CompletionItem item : expectedItems) {
				assertCompletion(list, item, expectedCount);
			}
		}
	}

	public static void assertCompletion(CompletionList completions, CompletionItem expected, Integer expectedCount) {
		List<CompletionItem> matches = completions.getItems().stream().filter(completion -> {
			return expected.getLabel().equals(completion.getLabel());
		}).collect(Collectors.toList());

		if (expectedCount != null) {
			assertTrue(matches.size() >= 1, () -> {
				return expected.getLabel() + " should only exist once: Actual: "
						+ completions.getItems().stream().map(c -> c.getLabel()).collect(Collectors.joining(","));
			});
		} else {
			assertEquals(1, matches.size(), () -> {
				return expected.getLabel() + " should only exist once: Actual: "
						+ completions.getItems().stream().map(c -> c.getLabel()).collect(Collectors.joining(","));
			});
		}

		CompletionItem match = getCompletionMatch(matches, expected);
		if (expected.getTextEdit() != null && match.getTextEdit() != null) {
			if (expected.getTextEdit().getLeft().getNewText() != null) {
				assertEquals(expected.getTextEdit().getLeft().getNewText(), match.getTextEdit().getLeft().getNewText());
			}
			Range r = expected.getTextEdit().getLeft().getRange();
			if (r != null && r.getStart() != null && r.getEnd() != null) {
				assertEquals(expected.getTextEdit().getLeft().getRange(), match.getTextEdit().getLeft().getRange());
			}
		}
		if (expected.getFilterText() != null && match.getFilterText() != null) {
			assertEquals(expected.getFilterText(), match.getFilterText());
		}

		if (expected.getDocumentation() != null) {
			assertEquals(expected.getDocumentation(), match.getDocumentation());
		}

	}

	private static CompletionItem getCompletionMatch(List<CompletionItem> matches, CompletionItem expected) {
		for (CompletionItem item : matches) {
			if (expected.getTextEdit().getLeft().getNewText().equals(item.getTextEdit().getLeft().getNewText())) {
				return item;
			}
		}
		return matches.get(0);
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

	// ------------------- Diagnostics assert

	public static void testDiagnosticsFor(String value, Diagnostic... expected) {
		testDiagnosticsFor(value, FILE_URI, PROJECT_URI, false, DEFAULT_JAVA_DATA_MODEL_CACHE, expected);
	}

	public static void testDiagnosticsFor(String value, String fileUri, String projectUri, boolean filter,
			JavaDataModelCache javaCache, Diagnostic... expected) {

		Template template = TemplateParser.parse(value, fileUri);
		template.setProjectUri(projectUri);

		QuteLanguageService languageService = new QuteLanguageService(javaCache);
		List<Diagnostic> actual = languageService.doDiagnostics(template, null, null, () -> {
		});
		if (expected == null) {
			assertTrue(actual.isEmpty());
			return;
		}
		assertDiagnostics(actual, Arrays.asList(expected), filter);
	}

	public static void assertDiagnostics(List<Diagnostic> actual, Diagnostic... expected) {
		assertDiagnostics(actual, Arrays.asList(expected), false);
	}

	public static void assertDiagnostics(List<Diagnostic> actual, List<Diagnostic> expected, boolean filter) {
		List<Diagnostic> received = actual;
		final boolean filterMessage;
		if (expected != null && !expected.isEmpty() && !StringUtils.isEmpty(expected.get(0).getMessage())) {
			filterMessage = true;
		} else {
			filterMessage = false;
		}
		if (filter) {
			received = actual.stream().map(d -> {
				Diagnostic simpler = new Diagnostic(d.getRange(), "");
				if (d.getCode() != null && !StringUtils.isEmpty(d.getCode().getLeft())) {
					simpler.setCode(d.getCode());
				}
				if (filterMessage) {
					simpler.setMessage(d.getMessage());
				}
				return simpler;
			}).collect(Collectors.toList());
		}
		// Don't compare message of diagnosticRelatedInformation
		for (Diagnostic diagnostic : received) {
			List<DiagnosticRelatedInformation> diagnosticRelatedInformations = diagnostic.getRelatedInformation();
			if (diagnosticRelatedInformations != null) {
				for (DiagnosticRelatedInformation diagnosticRelatedInformation : diagnosticRelatedInformations) {
					diagnosticRelatedInformation.setMessage("");
				}
			}
		}
		assertIterableEquals(expected, received, "Unexpected diagnostics:\n" + actual);
	}

	public static Diagnostic d(int startLine, int startCharacter, int endLine, int endCharacter, IQuteErrorCode code) {
		return d(startLine, startCharacter, endLine, endCharacter, code, "");
	}

	public static Diagnostic d(int startLine, int startCharacter, int endCharacter, IQuteErrorCode code) {
		// Diagnostic on 1 line
		return d(startLine, startCharacter, startLine, endCharacter, code);
	}

	public static Diagnostic d(int startLine, int startCharacter, int endLine, int endCharacter, IQuteErrorCode code,
			String message) {
		return d(startLine, startCharacter, endLine, endCharacter, code, message, null);
	}

	public static Diagnostic d(int startLine, int startCharacter, int endLine, int endCharacter, IQuteErrorCode code,
			String message, DiagnosticSeverity severity) {
		return d(startLine, startCharacter, endLine, endCharacter, code, message, QUTE_SOURCE, severity);
	}

	public static Diagnostic d(int startLine, int startCharacter, int endLine, int endCharacter, IQuteErrorCode code,
			String message, String source, DiagnosticSeverity severity) {
		// Diagnostic on 1 line
		return new Diagnostic(r(startLine, startCharacter, endLine, endCharacter), message, severity, source,
				code != null ? code.getCode() : null);
	}

	// ------------------- Hover assert

	public static void assertHover(String value) throws Exception {
		assertHover(value, null, null);
	}

	public static void assertHover(String value, String expectedHoverLabel, Range expectedHoverRange) throws Exception {
		assertHover(value, null, expectedHoverLabel, expectedHoverRange);
	}

	public static void assertHover(String value, String fileURI, String expectedHoverLabel, Range expectedHoverRange)
			throws Exception {
		SharedSettings sharedSettings = new SharedSettings();
		HoverCapabilities capabilities = new HoverCapabilities(Arrays.asList(MarkupKind.MARKDOWN), false);
		sharedSettings.getHoverSettings().setCapabilities(capabilities);
		assertHover(value, fileURI, PROJECT_URI, expectedHoverLabel, expectedHoverRange, sharedSettings,
				DEFAULT_JAVA_DATA_MODEL_CACHE);
	}

	public static void assertHover(String value, String fileUri, String projectUri, String expectedHoverLabel,
			Range expectedHoverRange, SharedSettings sharedSettings, JavaDataModelCache javaCache) throws Exception {
		int offset = value.indexOf("|");
		value = value.substring(0, offset) + value.substring(offset + 1);

		Template template = TemplateParser.parse(value, fileUri != null ? fileUri : FILE_URI);
		template.setProjectUri(projectUri);
		Position position = template.positionAt(offset);

		QuteLanguageService languageService = new QuteLanguageService(javaCache);
		Hover hover = languageService.doHover(template, position, sharedSettings, () -> {
		}).get();
		if (expectedHoverLabel == null) {
			assertNull(hover);
		} else {
			String actualHoverLabel = getHoverLabel(hover);
			assertEquals(expectedHoverLabel, actualHoverLabel);
			if (expectedHoverRange != null) {
				assertEquals(expectedHoverRange, hover.getRange());
			}
		}
	}

	private static String getHoverLabel(Hover hover) {
		Either<List<Either<String, MarkedString>>, MarkupContent> contents = hover != null ? hover.getContents() : null;
		if (contents == null) {
			return null;
		}
		return contents.getRight().getValue();
	}

	// ------------------- Definition assert

	public static void testDefinitionFor(String value, LocationLink... expected) throws Exception {
		testDefinitionFor(value, null, expected);
	}

	public static void testDefinitionFor(String value, String fileURI, LocationLink... expected) throws Exception {
		testDefinitionFor(value, fileURI, PROJECT_URI, DEFAULT_JAVA_DATA_MODEL_CACHE, expected);
	}

	public static void testDefinitionFor(String value, String fileUri, String projectUri, JavaDataModelCache javaCache,
			LocationLink... expected) throws Exception {
		int offset = value.indexOf("|");
		value = value.substring(0, offset) + value.substring(offset + 1);

		Template template = TemplateParser.parse(value, fileUri != null ? fileUri : FILE_URI);
		template.setProjectUri(projectUri);
		Position position = template.positionAt(offset);

		QuteLanguageService languageService = new QuteLanguageService(javaCache);

		List<? extends LocationLink> actual = languageService.findDefinition(template, position, () -> {
		}).get();
		assertLocationLink(actual, expected);

	}

	public static LocationLink ll(final String uri, final Range originRange, Range targetRange) {
		return new LocationLink(uri, targetRange, targetRange, originRange);
	}

	public static void assertLocationLink(List<? extends LocationLink> actual, LocationLink... expected) {
		assertEquals(expected.length, actual.size());
		for (int i = 0; i < expected.length; i++) {
			actual.get(i).setTargetUri(actual.get(i).getTargetUri().replace("file:///", "file:/"));
			expected[i].setTargetUri(expected[i].getTargetUri().replace("file:///", "file:/"));
		}
		assertArrayEquals(expected, actual.toArray());
	}

	// ------------------- Links assert

	public static void testDocumentLinkFor(String value, String fileUri, DocumentLink... expected) throws Exception {
		testDocumentLinkFor(value, fileUri, PROJECT_URI, TEMPLATE_BASE_DIR, DEFAULT_JAVA_DATA_MODEL_CACHE, expected);
	}

	public static void testDocumentLinkFor(String value, String fileUri, String projectUri, String templateBaseDir,
			JavaDataModelCache javaCache, DocumentLink... expected) {
		Template template = TemplateParser.parse(value, fileUri != null ? fileUri : FILE_URI);
		template.setProjectUri(projectUri);
		QuteProjectRegistry projectRegistry = new QuteProjectRegistry();
		projectRegistry.registerProject(new ProjectInfo(projectUri, templateBaseDir));
		template.setProjectRegistry(projectRegistry);

		QuteLanguageService languageService = new QuteLanguageService(javaCache);
		List<DocumentLink> actual = languageService.findDocumentLinks(template);
		assertDocumentLinks(actual, expected);
	}

	public static DocumentLink dl(Range range, String target) {
		return new DocumentLink(range, target);
	}

	public static void assertDocumentLinks(List<DocumentLink> actual, DocumentLink... expected) {
		assertEquals(expected.length, actual.size());
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i].getRange(), actual.get(i).getRange(), " Range test '" + i + "' link");
			assertEquals(Paths.get(expected[i].getTarget()).toUri().toString().replace("file:///", "file:/"),
					actual.get(i).getTarget().replace("file:///", "file:/"), " Target test '" + i + "' link");
		}
	}

	// ------------------- Highlights assert

	public static void testHighlightsFor(String value, DocumentHighlight... expected) throws BadLocationException {
		testHighlightsFor(value, FILE_URI, PROJECT_URI, DEFAULT_JAVA_DATA_MODEL_CACHE, expected);
	}

	public static void testHighlightsFor(String value, String fileUri, String projectUri, JavaDataModelCache javaCache,
			DocumentHighlight... expected) throws BadLocationException {
		int offset = value.indexOf('|');
		value = value.substring(0, offset) + value.substring(offset + 1);

		Template template = TemplateParser.parse(value, fileUri != null ? fileUri : FILE_URI);
		Position position = template.positionAt(offset);

		QuteLanguageService languageService = new QuteLanguageService(javaCache);

		List<? extends DocumentHighlight> actual = languageService.findDocumentHighlights(template, position, () -> {
		});
		assertDocumentHighlight(actual, expected);
	}

	public static void assertDocumentHighlight(List<? extends DocumentHighlight> actual,
			DocumentHighlight... expected) {
		assertEquals(expected.length, actual.size());
		assertArrayEquals(expected, actual.toArray());
	}

	public static DocumentHighlight hl(Range range) {
		return hl(range, DocumentHighlightKind.Read);
	}

	public static DocumentHighlight hl(Range range, DocumentHighlightKind kind) {
		return new DocumentHighlight(range, kind);
	}

	// ------------------- CodeLens assert

	public static void testCodeLensFor(String value, CodeLens... expected) throws Exception {
		testCodeLensFor(value, FILE_URI, PROJECT_URI, DEFAULT_JAVA_DATA_MODEL_CACHE, expected);
	}

	public static void testCodeLensFor(String value, String fileUri, String projectUri, JavaDataModelCache javaCache,
			CodeLens... expected) throws Exception {
		Template template = TemplateParser.parse(value, fileUri != null ? fileUri : FILE_URI);
		QuteLanguageService languageService = new QuteLanguageService(javaCache);

		QuteCodeLensSettings codeLensSettings = new QuteCodeLensSettings();
		List<? extends CodeLens> actual = languageService.getCodeLens(template, codeLensSettings, () -> {
		}).get();
		assertCodeLens(actual, expected);
	}

	public static CodeLens cl(Range range, String title, String command) {
		return new CodeLens(range, new Command(title, command), null);
	}

	public static void assertCodeLens(List<? extends CodeLens> actual, CodeLens... expected) {
		assertEquals(expected.length, actual.size());
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i].getRange(), actual.get(i).getRange());
			Command expectedCommand = expected[i].getCommand();
			Command actualCommand = actual.get(i).getCommand();
			if (expectedCommand != null && actualCommand != null) {
				assertEquals(expectedCommand.getTitle(), actualCommand.getTitle());
				assertEquals(expectedCommand.getCommand(), actualCommand.getCommand());
			}
			assertEquals(expected[i].getData(), actual.get(i).getData());
		}
	}

}
