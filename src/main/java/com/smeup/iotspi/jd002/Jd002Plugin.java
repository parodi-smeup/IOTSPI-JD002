package com.smeup.iotspi.jd002;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.smeup.rpgparser.CommandLineProgram;
import com.smeup.rpgparser.RunnerKt;

import Smeup.smeui.iotspi.datastructure.interfaces.SezInterface;
import Smeup.smeui.iotspi.datastructure.interfaces.SubConfInterface;
import Smeup.smeui.iotspi.datastructure.interfaces.SubInterface;
import Smeup.smeui.iotspi.datastructure.interfaces.SubMessageInterface;
import Smeup.smeui.iotspi.datastructure.iotconnector.IoTConnectorConf;
import Smeup.smeui.iotspi.datastructure.iotconnector.IoTConnectorInput;
import Smeup.smeui.iotspi.datastructure.iotconnector.IoTConnectorResponse;
import Smeup.smeui.iotspi.interaction.SPIIoTConnectorAdapter;

public class Jd002Plugin extends SPIIoTConnectorAdapter implements Runnable {

	private IoTConnectorConf connectorConf = null;
	private String socketPort = null;

	private final String RPG_FILENAME = "JD_003.rpgle";
	private String rpgSourceName = null;
	private CommandLineProgram commandLineProgram;
	private MyJavaSystemInterface javaSystemInterface;
	private ByteArrayOutputStream byteArrayOutputStream;
	private PrintStream printStream;
	private String a37tags;
	private Thread t = null;
	private Boolean isAlive = true;
	private ServerSocket serverSocket = null;
	
	private int logLevel = LogLevel.DEBUG.getLevel();

	@Override
	public boolean postInit(SezInterface sezInterface, IoTConnectorConf connectorConfiguration) {

		String logMsg = getTime() + "Called post-init " + getClass().getName() + "(listeners: " + this.getListenerList().size()
				+ ")";
		log(logLevel, logMsg);
		System.out.println(logMsg);

		// sezInterface not used because cabled response
		connectorConf = connectorConfiguration;

		// To handle system.out response
		byteArrayOutputStream = new ByteArrayOutputStream();
		printStream = new PrintStream(byteArrayOutputStream);

		// Read variables CNFSEZ from script SCP_SET.LOA37_JD3
		if (connectorConfiguration != null) {
			socketPort = connectorConf.getData("Port");
			logMsg = getTime() + "Selected port: " + socketPort;
			log(logLevel, logMsg);
			System.out.println(logMsg);

			rpgSourceName = connectorConfiguration.getData("RpgSources").trim() + RPG_FILENAME;
			logMsg = getTime() + "Selected rpgSourceName: " + rpgSourceName;
			log(logLevel, logMsg);
			System.out.println(logMsg);
		}

		try {
			logMsg = getTime() + "new ServerSocket on port: " + socketPort;
			log(logLevel, logMsg);
			serverSocket = new ServerSocket(Integer.valueOf(socketPort));
			logMsg = getTime() + "new JavaSystemInterface...";
			log(logLevel, logMsg);
			javaSystemInterface = new MyJavaSystemInterface(printStream, this, serverSocket);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			logMsg = e.getMessage();
			log(logLevel, logMsg);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logMsg = e.getMessage();
			log(logLevel, logMsg);
			e.printStackTrace();
		}
		javaSystemInterface.addJavaInteropPackage("com.smeup.jd");

		t = new Thread(this);
		t.start();

		return true;
	}

	/*
	 * Method to create a big string with all information of SUBVARS
	 * 
	 * idSub@valueName1{tagName1[valueTag1]tagname2[valueTag2].....}| ....
	 * valuename2{tagName1[valueTag1]tagname2[valueTag2].....}
	 */
	private String readSubVars(IoTConnectorConf configuration) {

		ArrayList<SubInterface> subList = configuration.getSubList();

		// This plug-in implements only ONE Sub. (get(0))
		SubInterface sub = subList.get(0);
		SubConfInterface subConf = sub.getConf();
		String subId = sub.getId();

		// Table of all plugin-in tabella di tutte le variabili del plug-in
		ArrayList<Hashtable<String, String>> subVarTable = subConf.getConfTable();
		StringBuilder a37tags = new StringBuilder();

		a37tags.append(subId + "@");

		for (int i = 0; i < subVarTable.size(); i++) {

			a37tags.append(subVarTable.get(i).get("Name"));
			a37tags.append("{");
			a37tags.append(createValueString("Txt", subVarTable.get(i).get("Txt")));
			a37tags.append(createValueString("TpDato", subVarTable.get(i).get("TpDato")));
			a37tags.append(createValueString("TpVar", subVarTable.get(i).get("TpVar")));
			a37tags.append(createValueString("DftVal", subVarTable.get(i).get("DftVal")));
			a37tags.append(createValueString("HowRead", subVarTable.get(i).get("HowRead")));
			a37tags.append(createValueString("IO", calcolateIOVars(sub, subVarTable.get(i).get("Name"))));
			a37tags.append("}");
			if (i != subVarTable.size() - 1) {
				a37tags.append("|");
			}
		}

		String logMsg = "a37tags: " + a37tags.toString();
		log(logLevel, logMsg);
		System.out.println(logMsg);

		return a37tags.toString();
	}

	// Calcolate if SUBVAR in MSGVAR is input or output
	private String calcolateIOVars(SubInterface sub, String name) {

		SubMessageInterface subMsg = sub.getMessage();
		ArrayList<Hashtable<String, String>> msgVarTable = subMsg.getConfTable();
		String msgIO = "";

		for (int i = 0; i < msgVarTable.size(); i++) {
			String msgNome = msgVarTable.get(i).get("Name");
			if (msgNome.equals(name)) {
				msgIO = msgVarTable.get(i).get("IO");
				break;
			}
		}
		return msgIO;
	}

	// For create a name[value] string
	private String createValueString(String name, String value) {
		return name + "[" + value + "]";
	}

	private String callProgram(final List<String> parms) {
		String logMsg = getTime() + "Calling " + rpgSourceName + " with " + parms.size() + " parms: " + String.join(",", parms);
		log(logLevel, logMsg);
		System.out.println(logMsg);

		commandLineProgram.singleCall(parms);
		String response = new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);
		byteArrayOutputStream.reset();

		return response;
	}

	@Override
	public IoTConnectorResponse invoke(IoTConnectorInput aDataTable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean unplug() {
		// TODO Auto-generated method stub
		isAlive = false;
		if(null != this.serverSocket) {
			try {
				this.serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public boolean ping() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void run() {

		String logMsg = getTime() + "New Thread started";
		log(logLevel, logMsg);
		System.out.println(logMsg);

		while (isAlive) {
			logMsg = getTime() + "Thread alive...";
			log(logLevel, logMsg);
			System.out.println(logMsg);
			// Read variables SUBVAR from script SCP_SET.LOA37_JD3
			a37tags = readSubVars(connectorConf);

			// Inizialize and call RPG Parser
			commandLineProgram = RunnerKt.getProgram(rpgSourceName, javaSystemInterface);
			commandLineProgram.setTraceMode(true);

			String response = null;
			List<String> parms = new ArrayList<String>();
			// Call JD003 A37TAGS method
			parms.add("INZ");
			parms.add("A37TAGS");
			parms.add(a37tags);
			parms.add("");
			response = callProgram(parms);

			logMsg = getTime() + "Response A37TAGS RPG method: " + response;
			log(logLevel, logMsg);
			System.out.println(logMsg);

			parms.clear();

			// Call JD003 POSTINIT method
			parms.add("INZ");
			parms.add("POSTINIT");
			parms.add(socketPort);
			parms.add("");
			response = callProgram(parms);

			logMsg = getTime() + "Program " + RPG_FILENAME;
			log(logLevel, logMsg);
			System.out.println(logMsg);
		}
	}
	
	private static String getTime() {
		return "[" + new Timestamp(System.currentTimeMillis()) + "] ";
	}

}
