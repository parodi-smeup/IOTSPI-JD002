package com.smeup.iotspi.filesystem.filemonitor;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
//import java.time.LocalTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class WatchDir
{

	public static enum MSGVAR
	{
		EVENT("EVENT"), PATH("PATH"), DIMENSION("DIMENSION"), DATETIME("DATETIME");
		
		String iCode= null;
		
		MSGVAR(String aCode)
		{
			iCode= aCode;
		}
		public String getCode()
		{
			return iCode;
		}
	}

	public static enum CONF
	{
		PATH("PATH"), FILTER("FILTER"), RECURSIVE("RECURSIVE"), EVENT_TYPE("EVENT");
		
		String iCode= null;
		
		CONF(String aCode)
		{
			iCode= aCode;
		}
		public String getCode()
		{
			return iCode;
		}
	}

	public static enum EVENTS
	{
		CREATE("C", ENTRY_CREATE), MODIFY("M", ENTRY_MODIFY), DELETE("D", ENTRY_DELETE);
		String iCode= null;
		
		Kind iKind= null;

		EVENTS(String aCode, Kind aEventKind)
		{
			iCode = aCode;
			iKind = aEventKind;
		}
		
		public String getCode()
		{
			return iCode;
		}
		public Kind getKind()
		{
			return iKind;
		}
		
		public static Kind retrieveKind(String aCode)
		{
			Kind vRet= null;
			if(CREATE.getCode().equalsIgnoreCase(aCode))
			{
				vRet= CREATE.getKind();
			}
			else if(MODIFY.getCode().equalsIgnoreCase(aCode))
			{
				vRet= MODIFY.getKind();
			}
			else if(DELETE.getCode().equalsIgnoreCase(aCode))
			{
				vRet= DELETE.getKind();
			}
			return vRet;
		}

		public static String retrieveCode(Kind aKind)
		{
			String vRet= null;
			if(CREATE.getKind().name().equalsIgnoreCase(aKind.name()))
			{
				vRet= CREATE.getCode();
			}
			else if(MODIFY.getKind().name().equalsIgnoreCase(aKind.name()))
			{
				vRet= MODIFY.getCode();
			}
			else if(DELETE.getKind().name().equalsIgnoreCase(aKind.name()))
			{
				vRet= DELETE.getCode();
			}
			return vRet;
		}
	};
	
//	private WatchDir INSTANCE= null;
	private WatchService WATCHER= null;
	private Map<WatchKey, Path> KEYS = null;

	ArrayList<WatchDirListener> iListenerList = new ArrayList<>();
	private boolean iRecursive;
	private boolean iTrace = false;
	private HashMap<String, String> iFileMap = new HashMap<String, String>();
	String[] iFiltri = new String[] {"*"};
	Kind[] iKind= null; 
	boolean iActive = false;
	private ArrayList<LogReceiverInterface> iLogReceiverList= new ArrayList<>();
	
	public WatchDir() throws IOException
	{
		WATCHER = FileSystems.getDefault().newWatchService();
		KEYS= new HashMap<WatchKey, Path>();
	}
	
//	public static WatchDir getInstance() throws IOException
//	{
//		if(INSTANCE==null) 
//		{
//			INSTANCE= new WatchDir();
//		}
//		
//		return INSTANCE;
//	}

	public void close() throws IOException
	{
		WATCHER.close();
		WATCHER = null;
		iActive = false;
	}

	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event)
	{
		return (WatchEvent<T>) event;
	}

	public boolean addListener(WatchDirListener aListener)
	{
		return iListenerList.add(aListener);
	}

	public boolean removeListener(WatchDirListener aListener)
	{
		return iListenerList.remove(aListener);
	}

	private void register(Path dir) throws IOException
	{
		register(dir, new Kind[] {ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE});
	}

	private void register(Path dir, Kind[] aKind) throws IOException
	{
		WatchKey key = dir.register(WATCHER, aKind);
		if (iTrace)
		{
			Path prev = KEYS.get(key);
			if (prev == null)
			{
				log(MessageFormat.format("register: %s\n", dir));
			}
			else
			{
				if (!dir.equals(prev))
				{
					log(MessageFormat.format("update: %s -> %s\n", prev, dir));
				}
			}
		}
		KEYS.put(key, dir);
	}

	private void registerAll(final Path start) throws IOException
	{
		registerAll(start, new Kind[] {ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE});
	}
	
	private void registerAll(final Path start, Kind[] aKind) throws IOException
	{
		Files.walkFileTree(start, new SimpleFileVisitor<Path>()
		{
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
			{
				register(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	public boolean init(HashMap<String, String> mappa)
	{
		return addRegister(mappa);
	}
	
	public boolean addRegister(HashMap<String, String> mappa)
	{
		boolean vRet = false;
		try
		{
			this.iRecursive = Boolean.valueOf(mappa.get(CONF.RECURSIVE.getCode()));
			Path dir = Paths.get(mappa.get(CONF.PATH.getCode()).toString());
			String[] vEvent= mappa.get(CONF.EVENT_TYPE.getCode())!=null?mappa.get(CONF.EVENT_TYPE.getCode()).split(";"):"M;C;D".split(";");
			ArrayList<Kind> iKindList= new ArrayList<>();
			
			for (int vI = 0; vI < vEvent.length; vI++)
			{
				Kind vKind= EVENTS.retrieveKind(vEvent[vI]);
				if(vKind!=null)
				{
					iKindList.add(vKind);
				}
			}
			iKind= iKindList.toArray(new Kind[iKindList.size()]);

			if(mappa.get(CONF.FILTER.getCode())!=null)
			{
				iFiltri = mappa.get(CONF.FILTER.getCode()).toString().split(";");
			}
			log("filtri applicati :");
			for (int i = 0; i < iFiltri.length; i++)
			{
				log(iFiltri[i]);
			}
			
			if (iRecursive)
			{
				log(MessageFormat.format("Scanning %s ...\n", mappa.get(CONF.PATH.getCode())));
				registerAll(dir, iKind);
				log("Done.");
			}
			else
			{
				register(dir, iKind);
			}
			this.iTrace = true;
			vRet = true;
		}
		catch (IOException vEx)
		{
			vEx.printStackTrace();
		}
		iActive = vRet;
		return vRet;
	}

	public void poll(HashMap<String, String> mappa)
	{
		if (!isActive())
		{
			init(mappa);
		}
		iActive = true;
		boolean vProcess= processEvents(iFiltri);
		log("processEvent: "+vProcess);
	}

	public void start(HashMap<String, String> mappa)
	{
		if (!isActive())
		{
			iActive= init(mappa);
		}

		if(isActive())
		{
			loop();
		}
	}

	private boolean processEvents(String[] filtri)
	{
		WatchKey key;
		key = WATCHER.poll();
		Path dir = KEYS.get(key);
		if (dir == null)
		{
			log("Error: WatchKey not recognized!!");
		}
		for (WatchEvent<?> event : key.pollEvents())
		{
			WatchEvent.Kind kind = event.kind();
			if (kind == OVERFLOW)
			{
				continue;
			}
			WatchEvent<Path> ev = cast(event);
			Path name = ev.context();
			Path child = dir.resolve(name);
			Boolean est = false;
			if (filtri[0].equals("*"))
			{
				est = true;
			}
			else
			{
				for (int i = 0; i < filtri.length; i++)
				{
					if (child.toString().toLowerCase().endsWith(filtri[i].toLowerCase()))
					{
						est = true;
					}
				}
			}
			if (est)
			{
				log(event.kind().name() + " ** " + child + " ** " + child.toFile().length() + "\r\n");
				log(MessageFormat.format("%s: %s - %s\n", event.kind().name(), child, child.toFile().length()));
				iFileMap.put(MSGVAR.EVENT.getCode(), EVENTS.retrieveCode(event.kind()));
				iFileMap.put(MSGVAR.PATH.getCode(), child.toString());
				iFileMap.put(MSGVAR.DIMENSION.getCode(), Long.toString(child.toFile().length()));
				iFileMap.put(MSGVAR.DATETIME.getCode(), GregorianCalendar.getInstance().getTime().toString());
				WatchDirEvent vEvent = new WatchDirEvent(iFileMap);
				Iterator<WatchDirListener> vListenerIterator = iListenerList.iterator();
				while (vListenerIterator.hasNext())
				{
					System.out.println("201 WatchDir");
					WatchDirListener vWatchDirListener = (WatchDirListener) vListenerIterator.next();
					vWatchDirListener.fireWatcherEvent(vEvent);
				}
			}
			if (iRecursive && (kind == ENTRY_CREATE))
			{
				try
				{
					if (Files.isDirectory(child, NOFOLLOW_LINKS))
					{
						registerAll(child);
					}
				}
				catch (IOException x)
				{
				}
			}
		}
		boolean valid = key.reset();
		if (!valid)
		{
			KEYS.remove(key);
			if (KEYS.isEmpty())
			{
			}
		}
		return valid;
	}

	private void loop()
	{
		while (true)
		{
			WatchKey key=null;
			try
			{
				key = WATCHER.take();
			}
			catch (InterruptedException x)
			{
				x.printStackTrace();
				return;
			}
			if(key==null)
			{
				continue;
			}
			Path dir = KEYS.get(key);
			if (dir == null)
			{
				log("Error: WatchKey not recognized!!");
				continue;
			}
			for (WatchEvent<?> event : key.pollEvents())
			{
				WatchEvent.Kind kind = event.kind();
				if (kind == OVERFLOW)
				{
					continue;
				}
				WatchEvent<Path> ev = cast(event);
				Path name = ev.context();
				Path child = dir.resolve(name);
				Boolean vIsValidEvent = false;
				if (iFiltri[0].equals("*"))
				{
					vIsValidEvent = true;
				}
				else
				{
					for (int i = 0; i < iFiltri.length; i++)
					{
						if (child.toString().toLowerCase().endsWith(iFiltri[i].toLowerCase()))
						{
							vIsValidEvent = true;
						}
					}
				}
				if (vIsValidEvent)
				{
					log(event.kind().name() + " ** " + child + " ** " + child.toFile().length() + "\r\n");
					log(MessageFormat.format("%s: %s - %s\n", event.kind().name(), child, child.toFile().length()));
					boolean vFoundKind= false;
					for (int vI = 0; vI < iKind.length && !vFoundKind; vI++)
					{
						Kind vKind = iKind[vI];
						if(vKind!=null)
						{
							vFoundKind= event.kind().name().equalsIgnoreCase(vKind.name());
						}
					}
					if( vFoundKind)
					{
						iFileMap.put(MSGVAR.EVENT.getCode(), EVENTS.retrieveCode(event.kind()));
						iFileMap.put(MSGVAR.PATH.getCode(), child.toString());
						iFileMap.put(MSGVAR.DIMENSION.getCode(), Long.toString(child.toFile().length()));
						iFileMap.put(MSGVAR.DATETIME.getCode(), GregorianCalendar.getInstance().getTime().toString());
						WatchDirEvent vEvent = new WatchDirEvent(iFileMap);
						Iterator<WatchDirListener> vListenerIterator = iListenerList.iterator();
						while (vListenerIterator.hasNext())
						{
							System.out.println("201 WatchDir");
							WatchDirListener vWatchDirListener = (WatchDirListener) vListenerIterator.next();
							vWatchDirListener.fireWatcherEvent(vEvent);
						}
					}
				}
				if (iRecursive && (kind == ENTRY_CREATE))
				{
					try
					{
						if (Files.isDirectory(child, NOFOLLOW_LINKS))
						{
							registerAll(child);
						}
					}
					catch (IOException x)
					{
						x.printStackTrace();
					}
				}
			}
			boolean valid = key.reset();
			if (!valid)
			{
				KEYS.remove(key);
				if (KEYS.isEmpty())
				{
					break;
				}
			}
		}
	}

	public boolean isActive()
	{
		return iActive;
	}

	static void usage(HashMap mappa, String chiave)
	{
		System.out.println(mappa.get(chiave));
		System.exit(-1);
	}

	// Il programma accetta un massimo di due parametri:
	// il primo [-p], che rappresenta la directory da monitorare (incluse le sottocartelle), � OBBLIGATORIO
	// il secondo [-f], che rappresenta eventuali filtri da applicare nella forma pdf;txt;xls, � FACOLTATIVO.
	// Nel caso in cui il parametro [-f] venga omesso, verranno monitorate TUTTE le estensioni.
	public static void main(String[] args) throws IOException
	{
		for (int vI = 0; vI < 2; vI++)
		{
			HashMap<String, String> hmap = new HashMap<String, String>();
			int nrparam = args.length;
			if (nrparam < 1 || nrparam > 2)
			{
				hmap.put("ERRORE", "Numero di parametri errato.");
				usage(hmap, "ERRORE");
			}
			if (nrparam == 1)
			{
				String folderPath = Paths.get(args[0]).toString();
				File folder = new File(folderPath);
				boolean existsFolder = folder.isDirectory();
				if (existsFolder == true)
				{
					Path dir = Paths.get(folder.toURI());
					hmap.put(CONF.PATH.getCode(), dir.toString());
					hmap.put(CONF.FILTER.getCode(), "*");
					hmap.put(CONF.RECURSIVE.getCode(), Boolean.TRUE.toString());
					hmap.put(CONF.EVENT_TYPE.getCode(), "M;C;D");
					WatchDir vWatcher = new WatchDir();
					if (vWatcher.init(hmap))
					{
						vWatcher.processEvents(hmap.get(CONF.FILTER.getCode()).toString().split(";"));
					}
				}
				else
				{
					hmap.put("ERRORE", "La directory specificata non esiste.");
					usage(hmap, "ERRORE");
				}
			}
			else if (nrparam == 2)
			{
				String folderPath = Paths.get(args[0]).toString();
				File folder = new File(folderPath);
				boolean existsFolder = folder.isDirectory();
				if (existsFolder == true)
				{
					Path dir = Paths.get(folder.toURI());
					hmap.put(CONF.PATH.getCode(), dir.toString());
					hmap.put(CONF.FILTER.getCode(), args[1].toString());
					hmap.put(CONF.RECURSIVE.getCode(), Boolean.TRUE.toString());
					hmap.put(CONF.EVENT_TYPE.getCode(), "M;C;D");
					WatchDir vWatcher = new WatchDir();
					if (vWatcher.init(hmap))
					{
						vWatcher.processEvents(hmap.get(CONF.FILTER.getCode()).toString().split(";"));
					}
				}
				else
				{
					hmap.put("ERRORE", "La directory specificata non esiste.");
					usage(hmap, "ERRORE");
				}
			}
		}
	}

	public ArrayList<LogReceiverInterface> getLogReceiverList()
	{
		return iLogReceiverList;
	}

	public void addLogListener(LogReceiverInterface aListener)
	{
		getLogReceiverList().add(aListener);
	}

	public void log(String aText)
	{
		if (getLogReceiverList() != null && getLogReceiverList().size() > 0)
		{
			for (Iterator vIterator = getLogReceiverList().iterator(); vIterator.hasNext();)
			{
				LogReceiverInterface vLogListener = (LogReceiverInterface) vIterator.next();
				vLogListener.logForward(aText);
			}
		}
		else
		{
			System.out.println(aText);
		}
	}
}