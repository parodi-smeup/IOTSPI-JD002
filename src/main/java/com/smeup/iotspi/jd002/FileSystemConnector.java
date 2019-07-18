package com.smeup.iotspi.jd002;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.smeup.iotspi.jd002.filemonitor.LogReceiverInterface;
import com.smeup.iotspi.jd002.filemonitor.WatchDir;
import com.smeup.iotspi.jd002.filemonitor.WatchDirEvent;
import com.smeup.iotspi.jd002.filemonitor.WatchDirListener;

import Smeup.smeui.iotspi.datastructure.interfaces.SezInterface;
import Smeup.smeui.iotspi.datastructure.interfaces.SubInterface;
import Smeup.smeui.iotspi.datastructure.iotconnector.IoTConnectorConf;
import Smeup.smeui.iotspi.datastructure.iotconnector.IoTConnectorInput;
import Smeup.smeui.iotspi.datastructure.iotconnector.IoTConnectorResponse;
import Smeup.smeui.iotspi.interaction.SPIIoTConnectorAdapter;
import Smeup.smeui.iotspi.interaction.SPIIoTEvent;

public class FileSystemConnector extends SPIIoTConnectorAdapter implements WatchDirListener, LogReceiverInterface
{
	WatchDir iMonitor = null;
	IoTConnectorConf iConfiguration= null;
	private ExecutorService iExecutor= null;

	@Override
	public boolean postInit(SezInterface aSez, IoTConnectorConf aConfiguration)
	{
		try
		{
			iMonitor= new WatchDir();
		}
		catch (IOException ex)
		{
			log(0, ex.getMessage());
			ex.printStackTrace();
			return false;
		}
		
		ArrayList<SubInterface> vList= getSubList();
		if(vList!=null && aSez!=null)
		{
			vList.addAll(aSez.getSubTable().values());
		}
		iConfiguration= aConfiguration;
		Hashtable<String, String> vMappa = iConfiguration.getPropertyTable();
		iMonitor.addListener(this);
		iMonitor.addLogListener(this);
		final boolean vRet = iMonitor.addRegister(new HashMap<String, String>(vMappa));
		if(vRet)
		{
			start();
		}
		return vRet;
	}

	private void start()
	{
		Runnable r = new Runnable()
		{
			public void run()
			{
				iMonitor.start(new HashMap<String, String>(iConfiguration.getPropertyTable()));
			}
		};
		iExecutor = Executors.newFixedThreadPool(1);
		iExecutor.execute(r);
	}
	
	@Override
	public boolean unplug()
	{
		boolean vRet = false;
		//		iCommandDispatcher.shutdown();
		//		iCommandWatcher.shutdown();
		try
		{
			if(iMonitor.isActive())
			{
				iMonitor.close();
			}
			
			if(!iExecutor.isShutdown())
			{
				iExecutor.shutdown();
			}
			vRet = true;
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		return vRet;
	}

	@Override
	public boolean ping()
	{
		return iMonitor!=null && iMonitor.isActive();
	}

	@Override
	public void fireWatcherEvent(WatchDirEvent aEvt)
	{
		processWatcherEvent(aEvt);
	}

	public void processWatcherEvent(WatchDirEvent aEvt)
	{
		if (aEvt != null)
		{
			ArrayList<SubInterface> vList = getSubList();
			String vSubId = null;
			SubInterface vSub = null;
			if (vList != null && vList.size() > 0)
			{
				vSub = vList.get(0);
				vSubId = vSub.getId();
			}
			HashMap<String, String> vEvtContent = aEvt.getContent();
			SPIIoTEvent vForwardedEvt = new SPIIoTEvent(vSubId);
			log(0, "FileSystemConnector: comunico " + vEvtContent.get("EVENT") + ": " + vEvtContent.get("PATH"));
			Iterator<String> vKeyIter = vEvtContent.keySet().iterator();
			while (vKeyIter.hasNext())
			{
				String vKeyValue = (String) vKeyIter.next();
				String vFieldValue = vEvtContent.get(vKeyValue);
				vForwardedEvt.setData(vKeyValue, vFieldValue);
			}
			fireEventToSmeup(vForwardedEvt);
		}
	}

	public static void main(String[] args)
	{
		final FileSystemConnector vConnector = new FileSystemConnector();
		final IoTConnectorConf vConf = new IoTConnectorConf();
		File vDir = new File("c:\\temp");
		vConf.addData("PATH", vDir.toString());
		vConf.addData("FILTRI", "*");
		vConf.addData("RECURSIVE", Boolean.TRUE.toString());
		vConf.addData("EVENT", "C;M;D");
		vConnector.postInit(null, vConf);
		//this line will execute immediately, not waiting for your task to complete
	}

	@Override
	public IoTConnectorResponse invoke(IoTConnectorInput aDataTable)
	{
		IoTConnectorResponse vResp = new IoTConnectorResponse();
		
    	if (aDataTable != null)
    	{
    		boolean vPreValue= ping();
    		if ("1".equalsIgnoreCase(aDataTable.getData("STATUS")))
    		{
    			start();
    		}
    		else
    		{
    			unplug();//iMonitor.close();
    		}
    		boolean vPostValue= ping();
    		vResp.addData("Monitor Status Pre", vPreValue ? "active" : "inactive");
    		vResp.addData("Monitor Status Now", vPostValue ? "active" : "inactive");
    	}
		return vResp;
	}
	
	@Override
	public void logForward(String aText)
	{
		if(getSez()!=null)
		{
			getSez().log(aText);
		}
		else
		{
			System.out.println(aText);
		}
	}
}
