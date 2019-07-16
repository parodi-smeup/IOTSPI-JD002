package com.smeup.iotspi.filesystem.filemonitor;

public interface WatchDirListener
{
	public void fireWatcherEvent(WatchDirEvent aEvent);
}
