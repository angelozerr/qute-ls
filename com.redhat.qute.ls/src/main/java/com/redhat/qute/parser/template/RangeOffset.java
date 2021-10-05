package com.redhat.qute.parser.template;

public class RangeOffset {

	private final int start;

	private final int end;

	public RangeOffset(int start, int end) {
		this.start = start;
		this.end = end;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

}
