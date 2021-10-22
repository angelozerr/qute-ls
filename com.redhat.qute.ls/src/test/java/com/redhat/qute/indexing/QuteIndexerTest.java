package com.redhat.qute.indexing;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class QuteIndexerTest {

	@Test
	public void index() {
		long start = System.currentTimeMillis();
		QuteIndexer indexer = new QuteIndexer(Paths.get("src/test/resources/templates"));
		indexer.scan();
		long end = System.currentTimeMillis();
		System.err.println((end - start) + "ms");

		// {#include base.qute.html}
		// {#ti|tle}A Book{/title}
		QuteIndex index = indexer.findDeclaration("base.qute.html", "tag");
		System.err.println(index);
	}
}
