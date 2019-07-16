package com.smeup.jd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.smeup.iotspi.jd002.LogLevel;
import com.smeup.rpgparser.interpreter.NumberType;
import com.smeup.rpgparser.interpreter.Program;
import com.smeup.rpgparser.interpreter.ProgramParam;
import com.smeup.rpgparser.interpreter.StringType;
import com.smeup.rpgparser.interpreter.StringValue;
import com.smeup.rpgparser.interpreter.SystemInterface;
import com.smeup.rpgparser.interpreter.Value;

import Smeup.smeui.iotspi.interaction.SPIIoTConnectorAdapter;

public class JD_RCVSCK implements Program {

	private List<ProgramParam> parms;
	@SuppressWarnings("unused")
	private String iError;
	private ServerSocket serverSocket;
	private SPIIoTConnectorAdapter sPIIoTConnectorAdapter;
	
	private int logLevel = LogLevel.DEBUG.getLevel();
	
	public JD_RCVSCK() {
		parms = new ArrayList<ProgramParam>();
		// Socket address
		parms.add(new ProgramParam("ADDRSK", new StringType(15)));
		// Response (read from socket)
		parms.add(new ProgramParam("BUFFER", new StringType(30000)));
		// Response length
		parms.add(new ProgramParam("BUFLEN", new NumberType(5, 0)));
		// Error
		parms.add(new ProgramParam("IERROR", new StringType(1)));
	}

	private String listenSocket(final int port) {

		String msgLog = getTime() + "Executing listenSocket(" + port + ")";
		getsPIIoTConnectorAdapter().log(logLevel, msgLog);
		String responseAsString = "";
		Socket socket = null;
		BufferedReader bufferedReader = null;
		StringWriter stringWriter = new StringWriter();
		try {
			msgLog = getTime() + "Socket listening on port " + port + "...";
			getsPIIoTConnectorAdapter().log(logLevel, msgLog);

			socket = this.serverSocket.accept();

			msgLog = getTime() + "...client connected";
			getsPIIoTConnectorAdapter().log(logLevel, msgLog);

			bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			char[] bufferSize = new char[1024 * 32];
			int readed = 0;
			int charNumber = 0;

			//TIMEOUT: serve perchè la telecamera non manda un carattere di fine (forse) quindi
			//per terminare la lettura si attende 2s. poi viene lanciato il timeoutException e
			//dentro al catch viene recuperato il contennuto della socket.
			//Per me è SHIT.
			socket.setSoTimeout(1000); // SAME AS VEGA PLUGIN (MONKEY COPY, DON'T KNOW WHY)
			while ((readed = bufferedReader.read(bufferSize)) != -1) {
				stringWriter.write(bufferSize, 0, readed);
				charNumber += readed;
			}
			responseAsString = stringWriter.toString().trim();

			msgLog = getTime() + "Content written: " + responseAsString;
			getsPIIoTConnectorAdapter().log(logLevel, msgLog);

			socketAndInBufferDestroy(socket, bufferedReader);

		} catch (SocketTimeoutException e) {
			msgLog = getTime() + "SocketTimeoutException " + e.getMessage();
			getsPIIoTConnectorAdapter().log(logLevel, msgLog);
			responseAsString = stringWriter.toString().trim();
			try {
				socketAndInBufferDestroy(socket, bufferedReader);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		} catch (IOException e) {
			msgLog = getTime() + "IOException " + e.getMessage();
			getsPIIoTConnectorAdapter().log(logLevel, msgLog);
			e.printStackTrace();
			responseAsString = "*ERROR " + e.getMessage();
			iError = "1";
		}

		return responseAsString;
	}

	@Override
	public List<ProgramParam> params() {
		return parms;
	}

	@SuppressWarnings("unused")
	@Override
	public List<Value> execute(SystemInterface arg0, LinkedHashMap<String, Value> arg1) {
		String msgLog = getTime() + "Executing JD_RCVSCK.execute(...)";
		getsPIIoTConnectorAdapter().log(logLevel, msgLog);

		ArrayList<Value> arrayListResponse = new ArrayList<Value>();

		String response = "";
		int bufferLength = 0;
		iError = "";
		String addrsk = "";
		String buffer = "";
		Long buflen = 0L;
		String ierror = "";

		for (Map.Entry<String, ? extends Value> entry : arg1.entrySet()) {

			String parmName = entry.getKey().toString();

			switch (parmName) {
			case "ADDRSK":
				addrsk = entry.getValue().asString().getValue();
				break;
			case "BUFFER":
				buffer = entry.getValue().asString().getValue();
				break;
			case "BUFLEN":
				buflen = entry.getValue().asInt().getValue();
				break;
			case "IERROR":
				ierror = entry.getValue().asString().getValue();
				break;
			}

			// all parms values as received
			arrayListResponse.add(entry.getValue());

		}

		// listen to socket
		int port = Integer.parseInt(addrsk.trim());
		response = listenSocket(port);

		// response from socket content
		arrayListResponse.set(1, new StringValue(response.trim()));

		// response length
		bufferLength = response.trim().length();
		arrayListResponse.set(2, new StringValue(String.valueOf(bufferLength)));

		return arrayListResponse;
	}

	public ServerSocket getServerSocket() {
		return serverSocket;
	}

	public void setServerSocket(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	public SPIIoTConnectorAdapter getsPIIoTConnectorAdapter() {
		return sPIIoTConnectorAdapter;
	}

	public void setsPIIoTConnectorAdapter(SPIIoTConnectorAdapter sPIIoTConnectorAdapter) {
		this.sPIIoTConnectorAdapter = sPIIoTConnectorAdapter;
	}

	public void socketAndInBufferDestroy(Socket aClientSocket, BufferedReader aInBuffer) throws IOException {
		if (aInBuffer != null) {
			aInBuffer.close();
		}
		if (aClientSocket != null) {
			aClientSocket.close();
		}
	}
	
	private static String getTime() {
		return "[" + new Timestamp(System.currentTimeMillis()) + "] ";
	}
}
