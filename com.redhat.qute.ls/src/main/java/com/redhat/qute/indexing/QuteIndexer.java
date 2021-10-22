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

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

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
 * @author azerr
 *
 */
public class QuteIndexer {

	private final Path templateBaseDir;

	public QuteIndexer(Path templateBaseDir) {
		this.templateBaseDir = templateBaseDir;
	}

	public void scan() {
		Set<String> fileList = new HashSet<>();
		try {
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(templateBaseDir)) {
				for (Path path : stream) {
					if (!Files.isDirectory(path)) {
						fileList.add(path.getFileName().toString());
					} else {
						System.err.println(path.getFileName().toString());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
