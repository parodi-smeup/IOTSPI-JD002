package com.smeup.jd002;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Ignore;
import org.junit.Test;

import com.smeup.iotspi.jd002.Jd002Plugin;

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
	private Jd002Plugin jd002Plugin = new Jd002Plugin();

	@Test
	@Ignore
	public void test() throws InterruptedException {

		connectorConf.addSub(getSubInterfaceInstance());
		connectorConf.addData("Port", "1234");
		connectorConf.addData("RpgSources", "src/test/resources/rpg/");
		sezInterface = getSezInterfaceInstance();

		assertEquals(true, jd002Plugin.postInit(sezInterface, connectorConf));
		// sleep for debug
		Thread.sleep(1200000);
	}

	@Test
	@Ignore
	public void test_openSocket() throws UnknownHostException, IOException, InterruptedException {

		// wait the socket is up
		Thread.sleep(2000);

		final String address = "localhost";
		final int port = 1234;
		final String message = "Data send to: " + address + ":" + port;

		Callable<String> callable = new Callable<String>() {
			@Override
			public String call() {
				connectorConf.addData("Port", "1234");
				connectorConf.addData("RpgSources", "src/test/resources/rpg/");

				jd002Plugin.postInit(sezInterface, connectorConf);

				return null;
			}
		};

		ExecutorService executor = Executors.newSingleThreadExecutor();
		@SuppressWarnings("unused")
		Future<String> future = executor.submit(callable);

		writeSocket(address, port, message);

		executor.shutdown();

	}

	private void writeSocket(String address, int port, String message) throws UnknownHostException, IOException {

		Socket socket = new Socket(address, port);
		OutputStream output = socket.getOutputStream();
		PrintWriter writer = new PrintWriter(output, true);
		writer.println(message);
		writer.flush();
		writer.close();
		socket.close();

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
				return "TL1";
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
				return "TL1";
			}

			@Override
			public SubMessageInterface getMessage() {
				return new SubMessageInterface() {

					@Override
					public ArrayList<Hashtable<String, String>> getConfTable() {
						System.out.println("getSubMessageInterface");
						ArrayList<Hashtable<String, String>> arr = new ArrayList<>();

						Hashtable<String, String> map1 = new Hashtable<String, String>();
						map1.put("Name", "CAMERA");
						map1.put("IO", "I");

						Hashtable<String, String> map2 = new Hashtable<String, String>();
						map2.put("Name", "TARGA");
						map2.put("IO", "I");

						Hashtable<String, String> map3 = new Hashtable<String, String>();
						map3.put("Name", "DIREZIONE");
						map3.put("IO", "I");

						Hashtable<String, String> map4 = new Hashtable<String, String>();
						map4.put("Name", "SPEED");
						map4.put("IO", "I");

						Hashtable<String, String> map5 = new Hashtable<String, String>();
						map5.put("Name", "DATA");
						map5.put("IO", "I");

						Hashtable<String, String> map6 = new Hashtable<String, String>();
						map6.put("Name", "ORA");
						map6.put("IO", "I");

						Hashtable<String, String> map7 = new Hashtable<String, String>();
						map7.put("Name", "IMMAGINE");
						map7.put("IO", "I");

						Hashtable<String, String> map8 = new Hashtable<String, String>();
						map8.put("Name", "CMD");
						map8.put("IO", "O");

						arr.add(map1);
						arr.add(map2);
						arr.add(map3);
						arr.add(map4);
						arr.add(map5);
						arr.add(map6);
						arr.add(map7);
						arr.add(map8);

						return arr;
					}
				};
			}

			@Override
			public String getId() {
				return "TL1";
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
						map1.put("Name", "CAMERA");
						map1.put("TpDato", "STRING");
						map1.put("DftVal", "CANCELLO_1");
						map1.put("Txt", "");
						map1.put("TpVar", "IN");
						map1.put("HowRead", "");

						Hashtable<String, String> map2 = new Hashtable<>();
						map2.put("Name", "TARGA");
						map2.put("TpDato", "STRING");
						map2.put("DftVal", "");
						map2.put("Txt", "");
						map2.put("TpVar", "IN");
						map2.put("HowRead", "TAG('PLATE_STRING')");

						Hashtable<String, String> map3 = new Hashtable<>();
						map3.put("Name", "DIREZIONE");
						map3.put("TpDato", "STRING");
						map3.put("DftVal", "");
						map3.put("Txt", "");
						map3.put("TpVar", "IN");
						map3.put("HowRead", "TAG('DIRECTION');DIR2DIR()");

						Hashtable<String, String> map4 = new Hashtable<>();
						map4.put("Name", "SPEED");
						map4.put("TpDato", "STRING");
						map4.put("DftVal", "");
						map4.put("Txt", "");
						map4.put("TpVar", "IN");
						map4.put("HowRead", "TAG('SPEED');DIV(100)");

						Hashtable<String, String> map5 = new Hashtable<>();
						map5.put("Name", "DATA");
						map5.put("TpDato", "STRING");
						map5.put("DftVal", "");
						map5.put("Txt", "");
						map5.put("TpVar", "IN");
						map5.put("HowRead", "TAG('DATE');DATE2DATE('yyyy-MM-dd','dd/MM/yyyy')");

						Hashtable<String, String> map6 = new Hashtable<>();
						map6.put("Name", "ORA");
						map6.put("TpDato", "STRING");
						map6.put("DftVal", "");
						map6.put("Txt", "");
						map6.put("TpVar", "IN");
						map6.put("HowRead", "TAG('TIME');DATE2DATE('HH-mm-ss-SS','HH:mm:ss')");

						Hashtable<String, String> map7 = new Hashtable<>();
						map7.put("Name", "IMMAGINE");
						map7.put("TpDato", "STRING");
						map7.put("DftVal", "");
						map7.put("Txt", "");
						map7.put("TpVar", "IN");
						map7.put("HowRead",
								"CONCAT('\\\\172.31.0.59\\ftp_cam_targhe_erbusco\\');TAG('DATE');CONCAT('_');TAG('TIME');CONCAT('_');TAG('PLATE_STRING');CONCAT('.JPG')");

						Hashtable<String, String> map8 = new Hashtable<>();
						map8.put("Name", "CMD");
						map8.put("TpDato", "STRING");
						map8.put("DftVal", "");
						map8.put("Txt", "");
						map8.put("TpVar", "CMD");
						map8.put("HowRead", "");

						arr.add(map1);
						arr.add(map2);
						arr.add(map3);
						arr.add(map4);
						arr.add(map5);
						arr.add(map6);
						arr.add(map7);
						arr.add(map8);
						return arr;
					}
				};
			}
		};
	}
}
