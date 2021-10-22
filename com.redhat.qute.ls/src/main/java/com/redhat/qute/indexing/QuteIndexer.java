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
package com.redhat.qute.indexing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * A Qute indexer is used to scan Qute templates for a Qute project which
 * defines a template base dir (ex : src/main/resources/templates) to stores
 * information about location and references of #include and #insert sections.
 * 
 * <p>
 * This indexer can be used to manage
 * <ul>
 * <li>completion for custom tag which comes from included template and which
 * defines #insert.</li>
 * <li>find references of #insert</li>
 * <li>display codelens references for #insert</li>
 * <li>manage go to the definition from custom tag to the #insert</li>
 * <p>
 * 
 * <p>
 * This indexer is able to manage those reference for opened and closed Qute
 * templates.
 * </p>
 * 
 * @author Angelo ZERR
 *
 */
public class QuteIndexer {

	private final Path templateBaseDir;

	private final Map<String, QuteTemplateIndex> indexes;

	public QuteIndexer(Path templateBaseDir) {
		this.templateBaseDir = templateBaseDir;
		this.indexes = new HashMap<>();
	}

	public void scan() {
		this.indexes.clear();
		try {
			Files.walk(templateBaseDir).forEach(path -> {
				if (!Files.isDirectory(path)) {
					try {
						System.err.println("---> " + path);
						
						QuteTemplateIndex templateIndex = new QuteTemplateIndex(path);
						templateIndex.collect();
						
						if (!templateIndex.getIndexes().isEmpty()) {
							String key = templateBaseDir.relativize(path).toString().replace('\\', '/');
							indexes.put(key, templateIndex);
							System.err.println("[" + key + "] ---> " + templateIndex.getIndexes());
						}
						
					} catch (IOException e) {
						e.printStackTrace();
					}

					System.err.println(path.getFileName().toString());
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public QuteIndex findDeclaration(String templateId, String parameter) {
		QuteTemplateIndex templateIndex = indexes.get(templateId);
		if (templateIndex == null) {
			return null;
		}
		return null;
	}
}
