package com.smeup.iotspi.jd002.filemonitor;

import java.util.HashMap;

public class WatchDirEvent
{
	HashMap<String, String> iContent= new HashMap<>();
	
	public WatchDirEvent(HashMap<String, String> aContent)
	{
		iContent= aContent;
	}
	
	public HashMap<String, String> getContent()
	{
		return iContent;
	}
}
