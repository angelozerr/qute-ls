package com.redhat.qute.indexing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

public class QuteIndexerTest {

	@Test
	public void definition() {
		long start = System.currentTimeMillis();
		QuteIndexer indexer = new QuteIndexer(Paths.get("src/test/resources/templates"));
		indexer.scan();
		long end = System.currentTimeMillis();
		System.err.println((end - start) + "ms");

		// base.qute.html -->
		// <title>{#insert title}Default Title{/}</title>

		// BookPage/book.qute.html -->
		// {#include base.qute.html}
		// {#ti|tle}A Book{/title}

		// get definition of title parameter of insert, declared in'base.qute.html'
		List<QuteIndex> indexes = indexer.findIndexes("base.qute.html", "insert", "title");
		assertNotNull(indexes);
		assertEquals(1, indexes.size());
		QuteIndex index = indexes.get(0);
		assertEquals("QuteIndex [\n" + //
				"  tag = \"insert\"\n" + //
				"  parameter = \"title\"\n" + //
				"  position = Position [\n" + //
				"    line = 4\n" + //
				"    character = 19\n" + //
				"  ]\n" + //
				"  kind = INSERT\n" + //
				"]", index.toString());
	}

	@Test
	public void declarations() {
		long start = System.currentTimeMillis();
		QuteIndexer indexer = new QuteIndexer(Paths.get("src/test/resources/templates"));
		indexer.scan();
		long end = System.currentTimeMillis();
		System.err.println((end - start) + "ms");

		// base.qute.html -->
		// <title>{#insert title}Default Title{/}</title>
		// ...
		// {#insert body}No body!{/}

		// BookPage/book.qute.html -->
		// {#include base.qute.html}
		// {#|
		List<QuteIndex> indexes = indexer.findIndexes("base.qute.html", "insert", null);
		assertNotNull(indexes);
		assertEquals(2, indexes.size());
		assertEquals("[QuteIndex [\n"
				+ "  tag = \"insert\"\n"
				+ "  parameter = \"title\"\n"
				+ "  position = Position [\n"
				+ "    line = 4\n"
				+ "    character = 19\n"
				+ "  ]\n"
				+ "  kind = INSERT\n"
				+ "], QuteIndex [\n"
				+ "  tag = \"insert\"\n"
				+ "  parameter = \"body\"\n"
				+ "  position = Position [\n"
				+ "    line = 9\n"
				+ "    character = 10\n"
				+ "  ]\n"
				+ "  kind = INSERT\n"
				+ "]]", indexes.toString());
	}
}
