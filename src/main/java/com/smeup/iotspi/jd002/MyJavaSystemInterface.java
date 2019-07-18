package com.smeup.iotspi.jd002;

import java.io.PrintStream;

import com.smeup.jd.JD_NFYEVE;
import com.smeup.jd.JD_LSTFLD;
import com.smeup.rpgparser.interpreter.Program;
import com.smeup.rpgparser.jvminterop.JavaSystemInterface;

import Smeup.smeui.iotspi.interaction.SPIIoTConnectorAdapter;

public class MyJavaSystemInterface extends JavaSystemInterface {
	private SPIIoTConnectorAdapter sPIIoTConnectorAdapter;

	public MyJavaSystemInterface(PrintStream printStream, SPIIoTConnectorAdapter sPIIoTConnectorAdapter) {
		super(printStream);
		this.sPIIoTConnectorAdapter = sPIIoTConnectorAdapter;
	}

	@Override
	public Program instantiateProgram(Class<?> arg0) {
		//This method is called by the interpreter when it has to execute a call to a program implemented in Java
		Program program = super.instantiateProgram(arg0);
		if (program instanceof JD_NFYEVE) {
			((JD_NFYEVE) program).setsPIIoTConnectorAdapter(sPIIoTConnectorAdapter);
		}
		if (program instanceof JD_LSTFLD) {
			((JD_LSTFLD) program).setsPIIoTConnectorAdapter(sPIIoTConnectorAdapter);
		}
		return program;
	}
}
