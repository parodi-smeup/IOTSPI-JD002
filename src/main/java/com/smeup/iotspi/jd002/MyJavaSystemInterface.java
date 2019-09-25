package com.smeup.iotspi.jd002;

import java.io.PrintStream;
import java.util.concurrent.ExecutorService;

import com.smeup.iotspi.jd002.filemonitor.WatchDir;
import com.smeup.jd.JD_LSTFLD;
import com.smeup.jd.JD_NFYEVE;
import com.smeup.rpgparser.interpreter.Program;
import com.smeup.rpgparser.jvminterop.JavaSystemInterface;
import com.smeup.rpgparser.logging.LoggingKt;

import Smeup.smeui.iotspi.interaction.SPIIoTConnectorAdapter;

public class MyJavaSystemInterface extends JavaSystemInterface {
	private SPIIoTConnectorAdapter sPIIoTConnectorAdapter;
	private WatchDir watchDir;	
	private ExecutorService executorService;

	public MyJavaSystemInterface(PrintStream printStream, SPIIoTConnectorAdapter sPIIoTConnectorAdapter, WatchDir watchDir, ExecutorService executorService) {
		super(printStream);
		this.sPIIoTConnectorAdapter = sPIIoTConnectorAdapter;
		this.watchDir = watchDir;	
		this.executorService = executorService;
		setLoggingConfiguration(LoggingKt.consoleLoggingConfiguration(LoggingKt.STATEMENT_LOGGER, LoggingKt.EXPRESSION_LOGGER));
//		setLoggingConfiguration(LoggingKt.fileLoggingConfiguration(new File("/home/tron/temp/log", "mioLog.csv"),
//				LoggingKt.DATA_LOGGER,
//				LoggingKt.LOOP_LOGGER, 
//				LoggingKt.RESOLUTION_LOGGER,
//				LoggingKt.EXPRESSION_LOGGER, 
//				LoggingKt.STATEMENT_LOGGER,
//				LoggingKt.PERFORMANCE_LOGGER));
		
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
			((JD_LSTFLD) program).setWatchDir(watchDir);
			((JD_LSTFLD) program).setExecutorService(executorService);
		}
		return program;
	}
}
