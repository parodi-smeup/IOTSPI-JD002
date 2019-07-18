package com.smeup.iotspi.jd002;

import java.io.PrintStream;
import java.net.ServerSocket;

import com.smeup.jd.JD_NFYEVE;
import com.smeup.jd.JD_RCVSCK;
import com.smeup.rpgparser.interpreter.Program;
import com.smeup.rpgparser.jvminterop.JavaSystemInterface;

import Smeup.smeui.iotspi.interaction.SPIIoTConnectorAdapter;

public class MyJavaSystemInterface extends JavaSystemInterface {
	private SPIIoTConnectorAdapter sPIIoTConnectorAdapter;
	private ServerSocket serverSocket;

	public MyJavaSystemInterface(PrintStream printStream, SPIIoTConnectorAdapter sPIIoTConnectorAdapter, ServerSocket serverSocket) {
		super(printStream);
		this.sPIIoTConnectorAdapter = sPIIoTConnectorAdapter;
		this.serverSocket = serverSocket;
	}

	@Override
	public Program instantiateProgram(Class<?> arg0) {
		//This method is called by the interpreter when it has to execute a call to a program implemented in Java
		Program program = super.instantiateProgram(arg0);
		if (program instanceof JD_NFYEVE) {
			((JD_NFYEVE) program).setsPIIoTConnectorAdapter(sPIIoTConnectorAdapter);
		}
		if (program instanceof JD_RCVSCK) {
			((JD_RCVSCK) program).setsPIIoTConnectorAdapter(sPIIoTConnectorAdapter);
			((JD_RCVSCK) program).setServerSocket(serverSocket);
		}
		return program;
	}
}
