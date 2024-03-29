/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt.internal.ls;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;

/**
 * Arguments utilities.
 *
 * @author Angelo ZERR
 *
 */
public class ArgumentUtils {

	public static Map<String, Object> getFirst(List<Object> arguments) {
		return arguments.isEmpty() ? null : (Map<String, Object>) arguments.get(0);
	}

	public static String getString(Map<String, Object> obj, String key) {
		return (String) obj.get(key);
	}

	@SuppressWarnings("unchecked")
	public static List<String> getStringList(Map<String, Object> obj, String key) {
		return (List<String>) obj.get(key);
	}

	public static boolean getBoolean(Map<String, Object> obj, String key) {
		Object result = obj.get(key);
		return result != null && result instanceof Boolean && ((Boolean) result).booleanValue();
	}

	public static int getInt(Map<String, Object> obj, String key) {
		Object result = obj.get(key);
		return result != null && result instanceof Number ? ((Number) result).intValue() : 0;
	}

	public static TextDocumentIdentifier getTextDocumentIdentifier(Map<String, Object> obj, String key) {
		Map<String, Object> textDocumentIdentifierObj = (Map<String, Object>) obj.get(key);
		if (textDocumentIdentifierObj == null) {
			return null;
		}
		String uri = getString(textDocumentIdentifierObj, "uri");
		return new TextDocumentIdentifier(uri);
	}

	public static Position getPosition(Map<String, Object> obj, String key) {
		Map<String, Object> positionObj = (Map<String, Object>) obj.get(key);
		if (positionObj == null) {
			return null;
		}
		int line = getInt(positionObj, "line");
		int character = getInt(positionObj, "character");
		return new Position(line, character);
	}

	public static Range getRange(Map<String, Object> obj, String key) {
		Map<String, Object> rangeObj = (Map<String, Object>) obj.get(key);
		if (rangeObj == null) {
			return null;
		}
		Position start = getPosition(rangeObj, "start");
		Position end = getPosition(rangeObj, "end");
		return new Range(start, end);
	}

	public static CodeActionContext getCodeActionContext(Map<String, Object> obj, String key) {
		Map<String, Object> contextObj = (Map<String, Object>) obj.get(key);
		if (contextObj == null) {
			return null;
		}
		List<Map<String, Object>> diagnosticsObj = (List<Map<String, Object>>) contextObj.get("diagnostics");
		List<Diagnostic> diagnostics = diagnosticsObj.stream().map(diagnosticObj -> {
			Diagnostic diagnostic = new Diagnostic();
			diagnostic.setRange(getRange(diagnosticObj, "range"));
			diagnostic.setCode(getString(diagnosticObj, "code"));
			diagnostic.setMessage(getString(diagnosticObj, "message"));
			diagnostic.setSource(getString(diagnosticObj, "source"));
			return diagnostic;
		}).collect(Collectors.toList());
		List<String> only = null;
		return new CodeActionContext(diagnostics, only);
	}

	/**
	 * Returns the child if it exists and is an object, and null otherwise
	 *
	 * @param obj the object to get the child of
	 * @param key the key of the child
	 * @return the child if it exists and is an object, and null otherwise
	 */
	public static Map<String, Object> getObject(Map<String, Object> obj, String key) {
		Object child = obj.get(key);
		if (child != null && child instanceof Map<?, ?>) {
			return (Map<String, Object>) child;
		}
		return null;
	}
}
