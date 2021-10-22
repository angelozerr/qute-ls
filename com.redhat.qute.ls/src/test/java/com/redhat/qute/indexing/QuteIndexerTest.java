package com.redhat.qute.indexing;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class QuteIndexerTest {

	@Test
	public void index() {
		QuteIndexer indexer = new QuteIndexer(Paths.get("src/test/resources/templates"));
		indexer.scan();
	}
}
