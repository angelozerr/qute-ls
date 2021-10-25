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
		List<QuteIndex> indexes = indexer.find("base.qute.html", "insert", "title");
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
				"  templateId = \"base.qute.html\"\n" + //
				"]", index.toString());
	}

	@Test
	public void completion() {
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
		List<QuteIndex> indexes = indexer.find("base.qute.html", "insert", null);
		assertNotNull(indexes);
		assertEquals(2, indexes.size());
		assertEquals("[QuteIndex [\n" + //
				"  tag = \"insert\"\n" + //
				"  parameter = \"title\"\n" + //
				"  position = Position [\n" + //
				"    line = 4\n" + //
				"    character = 19\n" + //
				"  ]\n" + //
				"  kind = INSERT\n" + //
				"  templateId = \"base.qute.html\"\n" + //
				"], QuteIndex [\n" + //
				"  tag = \"insert\"\n" + //
				"  parameter = \"body\"\n" + //
				"  position = Position [\n" + //
				"    line = 9\n" + //
				"    character = 10\n" + //
				"  ]\n" + //
				"  kind = INSERT\n" + //
				"  templateId = \"base.qute.html\"\n" + //
				"]]", indexes.toString());
	}

	@Test
	public void referencesOfIncludedFile() {
		long start = System.currentTimeMillis();
		QuteIndexer indexer = new QuteIndexer(Paths.get("src/test/resources/templates"));
		indexer.scan();
		long end = System.currentTimeMillis();
		System.err.println((end - start) + "ms");

		// base.qute.html -->
		// <title>{#insert title}Default Title{/}</title>
		// ...
		// {#insert body}No body!{/}

		// 1. reference
		// BookPage/book.qute.html -->
		// {#include base}
		// {#title}A Book{/title}

		// 2. reference
		// BookPage/books.qute.html -->
		// {#include base}
		// {#title}Books{/title}

		List<QuteIndex> indexes = indexer.find(null, "include", "base");
		assertNotNull(indexes);
		assertEquals(2, indexes.size());
		assertEquals("[QuteIndex [\n" + //
				"  tag = \"include\"\n" + //
				"  parameter = \"base\"\n" + //
				"  position = Position [\n" + //
				"    line = 1\n" + //
				"    character = 11\n" + //
				"  ]\n" + //
				"  kind = INCLUDE\n" + //
				"  templateId = \"BookPage/books.qute.html\"\n" + //
				"], QuteIndex [\n" + //
				"  tag = \"include\"\n" + //
				"  parameter = \"base\"\n" + //
				"  position = Position [\n" + //
				"    line = 1\n" + //
				"    character = 11\n" + //
				"  ]\n" + //
				"  kind = INCLUDE\n" + //
				"  templateId = \"BookPage/book.qute.html\"\n" + //
				"]]", indexes.toString());
	}
	
	@Test
	public void referencesOfIncludedTag() {
		long start = System.currentTimeMillis();
		QuteIndexer indexer = new QuteIndexer(Paths.get("src/test/resources/templates"));
		indexer.scan();
		long end = System.currentTimeMillis();
		System.err.println((end - start) + "ms");

		// base.qute.html -->
		// <title>{#insert title}Default Title{/}</title>
		// ...
		// {#insert body}No body!{/}

		// 1. reference
		// BookPage/book.qute.html -->
		// {#include base}
		// {#title}A Book{/title}

		// 2. reference
		// BookPage/books.qute.html -->
		// {#include base}
		// {#title}Books{/title}

		List<QuteIndex> indexes = indexer.find(null, "body", null);
		assertNotNull(indexes);
		assertEquals(2, indexes.size());
		assertEquals("[QuteIndex [\n" + //
				"  tag = \"body\"\n" + //
				"  parameter = null\n" + //
				"  position = Position [\n" + //
				"    line = 3\n" + //
				"    character = 3\n" + //
				"  ]\n" + //
				"  kind = CUSTOM\n" + //
				"  templateId = \"BookPage/books.qute.html\"\n" + //
				"], QuteIndex [\n" + //
				"  tag = \"body\"\n" + //
				"  parameter = null\n" + //
				"  position = Position [\n" + //
				"    line = 3\n" + //
				"    character = 3\n" + //
				"  ]\n" + //
				"  kind = CUSTOM\n" + //
				"  templateId = \"BookPage/book.qute.html\"\n" + //
				"]]", indexes.toString());
	}
	
}
