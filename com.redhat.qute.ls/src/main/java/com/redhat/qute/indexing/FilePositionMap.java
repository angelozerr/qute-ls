package com.redhat.qute.indexing;

import org.eclipse.lsp4j.Position;

public interface FilePositionMap {

	/**
	 * @param int offset Similar to getStartLine but includes both the line and the
	 *            column
	 */
	default Position getLineCharacterPositionForOffset(int offset) {
		int line = this.getLineNumberForOffset(offset);
		int character = this.getColumnForOffset(offset);
		return new Position(line - 1, character - 1);
	}

	/**
	 * @param int offset - A 0-based byte offset
	 * @return int - gets the 1-based line number for offset
	 */
	int getLineNumberForOffset(int offset);

	/**
	 * @param int offset - A 0-based byte offset
	 * @return int - gets the 1-based column number for offset
	 */
	int getColumnForOffset(int offset);

}