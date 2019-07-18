package com.smeup.iotspi.jd002;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
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
import Smeup.smeui.iotspi.datastructure.iotconnector.IoTConnectorConf;
import Smeup.smeui.iotspi.datastructure.iotconnector.IoTConnectorInput;
import Smeup.smeui.iotspi.datastructure.iotconnector.IoTConnectorResponse;
import Smeup.smeui.iotspi.interaction.SPIIoTConnectorAdapter;

public class Jd002Plugin extends SPIIoTConnectorAdapter implements Runnable {

	private IoTConnectorConf connectorConf = null;
	private String folder = null;
	private String mode = null;
	private String filter = null;
	private String recursive = null;
	private String maileventc = null;
	private String maileventd = null;

	private final String RPG_FILENAME = "JD_002.rpgle";
	private String rpgSourceName = null;
	private CommandLineProgram commandLineProgram;
	private MyJavaSystemInterface javaSystemInterface;
	private ByteArrayOutputStream byteArrayOutputStream;
	private PrintStream printStream;
	private String a37tags;
	private Thread t = null;
	private Boolean isAlive = true;

	private int logLevel = LogLevel.DEBUG.getLevel();

	@Override
	public boolean postInit(SezInterface sezInterface, IoTConnectorConf connectorConfiguration) {

		String logMsg = getTime() + "Called post-init " + getClass().getName() + "(listeners: "
				+ this.getListenerList().size() + ")";
		log(logLevel, logMsg);
		System.out.println(logMsg);

		// sezInterface not used because cabled response
		connectorConf = connectorConfiguration;

		// To handle system.out response
		byteArrayOutputStream = new ByteArrayOutputStream();
		printStream = new PrintStream(byteArrayOutputStream);

		// Read variables CNFSEZ from script SCP_SET.LOA37_JD3
		if (connectorConfiguration != null) {
			folder = connectorConf.getData("FOLDER");
			mode = connectorConf.getData("MODE");
			filter = connectorConf.getData("FILTER");
			recursive = connectorConf.getData("RECURSIVE");
			maileventc = connectorConf.getData("MAILEVENTC");
			maileventd = connectorConf.getData("MAILEVENTD");

			logMsg = getTime() + "folder:" + folder + " mode:" + mode + " filter:" + filter + " recursive:" + recursive
					+ " maileventc:" + maileventc + " maileventd:" + maileventd;
			log(logLevel, logMsg);
			System.out.println(logMsg);

			rpgSourceName = connectorConfiguration.getData("RpgSources").trim() + RPG_FILENAME;
			logMsg = getTime() + "Selected rpgSourceName: " + rpgSourceName;
			log(logLevel, logMsg);
			System.out.println(logMsg);
		}

		logMsg = getTime() + "new JavaSystemInterface...";
		log(logLevel, logMsg);
		javaSystemInterface = new MyJavaSystemInterface(printStream, this);
		javaSystemInterface.addJavaInteropPackage("com.smeup.jd");

		t = new Thread(this);
		t.start();

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
			final String p = String.format("Folder(%s) Mode(%s) Filter(%s) Recursive(s%)", folder.trim(), mode.trim(), filter.trim(), recursive.trim()); 
			parms.add(p);
			parms.add("");
			response = callProgram(parms);

			logMsg = getTime() + "Program " + RPG_FILENAME;
			log(logLevel, logMsg);
			System.out.println(logMsg);
		}
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

		// Table of all plugin-in
		ArrayList<Hashtable<String, String>> subVarTable = subConf.getConfTable();
		StringBuilder a37tags = new StringBuilder();

		a37tags.append(subId + "@");

		for (int i = 0; i < subVarTable.size(); i++) {

			a37tags.append(subVarTable.get(i).get("Name"));
			a37tags.append("{");
			a37tags.append(createValueString("TpVar", subVarTable.get(i).get("TpVar")));
			a37tags.append(createValueString("DftVal", subVarTable.get(i).get("DftVal")));
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

	// For create a name[value] string
	private String createValueString(String name, String value) {
		return name + "[" + value + "]";
	}

	private String callProgram(final List<String> parms) {
		String logMsg = getTime() + "Calling " + rpgSourceName + " with " + parms.size() + " parms: "
				+ String.join(",", parms);
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
		return false;
	}

	@Override
	public boolean ping() {
		// TODO Auto-generated method stub
		return true;
	}

	private static String getTime() {
		return "[" + new Timestamp(System.currentTimeMillis()) + "] ";
	}

}
