package com.smeup.iotspi.jd002;

public enum LogLevel {
	DEBUG(0), INFO(10), ERROR(50);

	private int level;

	LogLevel(int level) {
		this.level = level;
	}

	public int getLevel() {
		return level;
	}
}
