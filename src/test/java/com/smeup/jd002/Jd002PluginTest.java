package com.smeup.jd002;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import org.junit.Ignore;
import org.junit.Test;

import com.smeup.iotspi.jd002.FileSystemConnector;

import Smeup.smeui.iotspi.datastructure.interfaces.SezConfInterface;
import Smeup.smeui.iotspi.datastructure.interfaces.SezInterface;
import Smeup.smeui.iotspi.datastructure.interfaces.SubConfInterface;
import Smeup.smeui.iotspi.datastructure.interfaces.SubInterface;
import Smeup.smeui.iotspi.datastructure.interfaces.SubMessageInterface;
import Smeup.smeui.iotspi.datastructure.iotconnector.IoTConnectorConf;
import Smeup.smeui.iotspi.interfaces.SPIIoTFrameworkInterface;

public class Jd002PluginTest extends Thread {

	private IoTConnectorConf connectorConf = new IoTConnectorConf();
	private SezInterface sezInterface = null;
	private FileSystemConnector fileSystemConnector = new FileSystemConnector();

	@Test
	public void test() throws InterruptedException {

		connectorConf.addSub(getSubInterfaceInstance());
		connectorConf.addData("PATH", "/home/tron/temp/test/listener");
		connectorConf.addData("FILTRI", "txt;pdf;jpg;doc");
		connectorConf.addData("RECURSIVE", "true");
		connectorConf.addData("EVENT", "C;M;D");
		connectorConf.addData("RpgSources", "src/test/resources/rpg/");
		sezInterface = getSezInterfaceInstance();

		assertEquals(true, fileSystemConnector.postInit(sezInterface, connectorConf));
		// sleep for debug
		Thread.sleep(1200000);
	}

	private SezInterface getSezInterfaceInstance() {

		return new SezInterface() {

			@Override
			public void log(String aMessage) {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean init(SPIIoTFrameworkInterface aFramework, String aId, String aName) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public int getType() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public String getTOgg() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public HashMap<String, SubInterface> getSubTable() {
				HashMap<String, SubInterface> map = new HashMap<>();
				SubInterface sub = getSubInterfaceInstance();
				map.put("TpValue", sub);

				return map;
			}

			@Override
			public String getPluginClass() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getPgm() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getOgg() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return "A01";
			}

			@Override
			public String getId() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public SezConfInterface getConf() {
				return new SezConfInterface() {

					@Override
					public String getValue(String aKey) {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public ArrayList<String> getKeys() {
						// TODO Auto-generated method stub
						return null;
					}
				};
			}

			@Override
			public boolean checkServerSession() {
				// TODO Auto-generated method stub
				return false;
			}
		};
	}

	private SubInterface getSubInterfaceInstance() {
		return new SubInterface() {

			@Override
			public String getName() {
				return "A01";
			}

			@Override
			public SubMessageInterface getMessage() {
				return new SubMessageInterface() {

					@Override
					public ArrayList<Hashtable<String, String>> getConfTable() {
						System.out.println("getSubMessageInterface");
						ArrayList<Hashtable<String, String>> arr = new ArrayList<>();

						Hashtable<String, String> map1 = new Hashtable<String, String>();
						map1.put("Name", "EVENT");

						Hashtable<String, String> map2 = new Hashtable<String, String>();
						map2.put("Name", "PATH");

						Hashtable<String, String> map3 = new Hashtable<String, String>();
						map3.put("Name", "DIMENSION");
						
						Hashtable<String, String> map4 = new Hashtable<String, String>();
						map3.put("Name", "DATETIME");

						arr.add(map1);
						arr.add(map2);
						arr.add(map3);
						arr.add(map4);

						return arr;
					}
				};
			}

			@Override
			public String getId() {
				return "A01";
			}

			@Override
			public SubConfInterface getConf() {

				return new SubConfInterface() {

					@Override
					public boolean setValue(String aString, String aMessagePath) {
						// TODO Auto-generated method stub
						return false;
					}

					@Override
					public String getValue(String aString) {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public ArrayList<Hashtable<String, String>> getConfTable() {

						ArrayList<Hashtable<String, String>> arr = new ArrayList<>();

						Hashtable<String, String> map1 = new Hashtable<>();
						map1.put("Name", "STATUS");
						map1.put("TpVar", "V2SI/NO");
						map1.put("DftVal", "1");
						
						arr.add(map1);

						return arr;
					}
				};
			}
		};
	}
}
