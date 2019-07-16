package com.smeup.jd;

import java.io.StringReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import com.smeup.iotspi.jd002.EventComponent;
import com.smeup.iotspi.jd002.LogLevel;
import com.smeup.rpgparser.interpreter.Program;
import com.smeup.rpgparser.interpreter.ProgramParam;
import com.smeup.rpgparser.interpreter.StringType;
import com.smeup.rpgparser.interpreter.StringValue;
import com.smeup.rpgparser.interpreter.SystemInterface;
import com.smeup.rpgparser.interpreter.Value;

import Smeup.smeui.iotspi.interaction.SPIIoTConnectorAdapter;
import Smeup.smeui.iotspi.interaction.SPIIoTEvent;

public class JD_NFYEVE implements Program {

	private List<ProgramParam> parms;
	private Map<String, EventComponent> eventList = new HashMap<>();
	private String a37SubId;
	private SPIIoTConnectorAdapter sPIIoTConnectorAdapter;

	private int logLevel = LogLevel.DEBUG.getLevel();
	
	public JD_NFYEVE() {
		parms = new ArrayList<ProgramParam>();
		// Sme.UP Function
		parms.add(new ProgramParam("§§FUNZ", new StringType(15)));
		// Sme.UP Method
		parms.add(new ProgramParam("§§METO", new StringType(30000)));
		// XML from camera
		parms.add(new ProgramParam("§§SVAR", new StringType(210000)));
		// List of A37 attributes from script
		parms.add(new ProgramParam("A37TAGS", new StringType(4096)));
	}

	private String notifyEvent(final String xml) {
		String response = "";
		
		String msgLog = getTime() + " Create document from iResult:" + xml.trim();
		getsPIIoTConnectorAdapter().log(logLevel, msgLog);
		
		Document document= null;
		SAXReader xmlReader = new SAXReader();
		try {
			document = xmlReader.read(new InputSource(new StringReader(xml.trim())));
		} catch (DocumentException e) {
			msgLog = getTime() + "Error parsing xml:" + xml.trim();
			getsPIIoTConnectorAdapter().log(logLevel, msgLog);
			e.printStackTrace();
			return response;
		}
		
		msgLog = getTime() + "...done";
		getsPIIoTConnectorAdapter().log(logLevel, msgLog);
		
		createEvent(document);
		
		return response;
	}

	@Override
	public List<ProgramParam> params() {
		return parms;
	}

	@Override
	public List<Value> execute(SystemInterface arg0, LinkedHashMap<String, Value> arg1) {
		String msgLog = getTime() + "Executing JD_NFYEVE.execute(...)";
		getsPIIoTConnectorAdapter().log(logLevel, msgLog);
		
		ArrayList<Value> arrayListResponse = new ArrayList<Value>();

		String funz = "";
		String meto = "";
		String svar = "";
		String tags = "";

		//Receive parms (similar to *ENTRY PLIST RPG)
		for (Map.Entry<String, ? extends Value> entry : arg1.entrySet()) {

			String parmName = entry.getKey().toString();

			switch (parmName) {
			case "§§FUNZ":
				funz = entry.getValue().asString().getValue();
				break;
			case "§§METO":
				meto = entry.getValue().asString().getValue();
				break;
			case "§§SVAR":
				svar = entry.getValue().asString().getValue();
				break;
			case "A37TAGS":
				tags = entry.getValue().asString().getValue();
				break;
			}

			//all parms values as received
			arrayListResponse.add(entry.getValue());
		}
		
		//§§FUNZ='NFY', §§METO='EVE', §§SVAR=XML from socket, A37TAGS=list of tags and their values 
		if("NFY".equals(funz.trim()) && "EVE".equals(meto.trim())) {
			
			final String a37tags = tags;
			extractTags(a37tags);
			
			final String xml = svar;
			String response = notifyEvent(xml);
			arrayListResponse.set(2, new StringValue(response.trim()));
		}

		return arrayListResponse;
	}

	@SuppressWarnings("unused")
	private Map<String, EventComponent> extractTags(final String a37tags) {

		setA37SubId(a37tags.split("@")[0]);
		final String[] rows = a37tags.split("@")[1].split("\\|");

		for (String row : rows) {
			// name
			String name = row.split("\\{")[0];

			// attributes of name var
			String nameAttributes = row.split("\\{")[1];

			// assume 6 attributes
			String txt_keyValue = nameAttributes.split("\\]")[0];
			String txt_key = txt_keyValue.split("\\[")[0];
			String txt_value = "";
			if (txt_keyValue.split("\\[").length > 1) {
				txt_value = txt_keyValue.split("\\[")[1];
			}
			
			String tpDato_keyValue = nameAttributes.split("\\]")[1];
			String tpDato_key = tpDato_keyValue.split("\\[")[0];
			String tpDato_value = "";
			if (tpDato_keyValue.split("\\[").length > 1) {
				tpDato_value = tpDato_keyValue.split("\\[")[1];
			}
			
			String tpVar_keyValue = nameAttributes.split("\\]")[2];
			String tpVar_key = tpVar_keyValue.split("\\[")[0];
			String tpVar_value = "";
			if (tpVar_keyValue.split("\\[").length > 1) {
				tpVar_value = tpVar_keyValue.split("\\[")[1];
			}

			String dftVal_keyValue = nameAttributes.split("\\]")[3];
			String dftVal_key = dftVal_keyValue.split("\\[")[0];
			String dftVal_value = "";
			if (dftVal_keyValue.split("\\[").length > 1) {
				dftVal_value = dftVal_keyValue.split("\\[")[1];
			}

			String howRead_keyValue = nameAttributes.split("\\]")[4];
			String howRead_key = howRead_keyValue.split("\\[")[0];
			String howRead_value = "";
			if (howRead_keyValue.split("\\[").length > 1) {
				howRead_value = howRead_keyValue.split("\\[")[1];
			}

			String iO_keyValue = nameAttributes.split("\\]")[5];
			String iO_key = iO_keyValue.split("\\[")[0];
			String iO_value = "";
			if (iO_keyValue.split("\\[").length > 1) {
				iO_value = iO_keyValue.split("\\[")[1];
			}

			EventComponent eventComponent = new EventComponent(a37SubId);
			String msgLog = getTime() + "EventName:" + name + " DataType:" + tpDato_value + " Type:" + tpVar_value + " HowRead:" + howRead_value + " MsgRet:" + iO_value + " DftValue:" + dftVal_value + " EventDesc:"  + txt_value;
			getsPIIoTConnectorAdapter().log(logLevel, msgLog);
			eventComponent.setIEventName(name);
			eventComponent.setIDataType(tpDato_value);
			eventComponent.setIType(tpVar_value);
			eventComponent.setIHowRead(howRead_value);
			eventComponent.setIsMsgRet("I".equals(iO_value));
			eventComponent.setIDftValue(dftVal_value);
			eventComponent.setIEventDesc(txt_value);

			this.eventList.put(name, eventComponent);
		}

		return this.eventList;
	}

	public void createEvent(Document aDoc) {
		try {
			String msgLog = getTime() + "Metodo readData - Lettura buffer plugin";
			getsPIIoTConnectorAdapter().log(logLevel, msgLog);
			// Alimento i vari TAG
			for (String vKey : this.eventList.keySet()) {
				EventComponent vEvtComp = this.eventList.get(vKey);
				if (vEvtComp.getIsMsgRet())
					vEvtComp.setIValue(aDoc);
			}

			// Creo evento - di default crea evento con tutte le variabili
			createEvent();
		} catch (Exception vEx) {
			String msgLog = getTime() + "Errore metodo readData - " + vEx.getMessage();
			getsPIIoTConnectorAdapter().log(logLevel, msgLog );
		}
	}

	private void createEvent() {
		String msgLog = getTime() + "Metodo createEvent (listeners: " + getsPIIoTConnectorAdapter().getListenerList().size() + ")";
		getsPIIoTConnectorAdapter().log(logLevel, msgLog);
		try {
			// Crea SPIIOTEvent
			SPIIoTEvent vEvent = new SPIIoTEvent(getA37SubId());
			// Alimentazione struttura Event
			msgLog = getTime() + "Metodo createEvent: alimentazione struttura event (elementi lista eventi " + this.eventList.size() + ")" ;
			getsPIIoTConnectorAdapter().log(logLevel, msgLog);
			for (String vKey : this.eventList.keySet()) {
				EventComponent vEvtComp = this.eventList.get(vKey);
				msgLog = getTime() + " evento:" + vKey ;
				getsPIIoTConnectorAdapter().log(logLevel, msgLog);
				// Ritorno solo le variabili non di tipo CMD
				if (vEvtComp.getIsMsgRet())
					vEvent.setData(vKey, vEvtComp.getIValue());
			}
			// invia Evento
			msgLog = getTime() + "invio evento (fireEventToSmeup)" + vEvent.getDataTable().toString();
			getsPIIoTConnectorAdapter().log(logLevel, msgLog);
			getsPIIoTConnectorAdapter().fireEventToSmeup(vEvent);
		} catch (Exception vEx) {
			msgLog = getTime() + "Errore metodo createEvent- " + vEx.getMessage();
			getsPIIoTConnectorAdapter().log(logLevel, msgLog);
		}
	}

	public String getA37SubId() {
		return a37SubId;
	}

	public void setA37SubId(String a37SubId) {
		this.a37SubId = a37SubId;
	}

	public SPIIoTConnectorAdapter getsPIIoTConnectorAdapter() {
		return sPIIoTConnectorAdapter;
	}

	public void setsPIIoTConnectorAdapter(SPIIoTConnectorAdapter sPIIoTConnectorAdapter) {
		this.sPIIoTConnectorAdapter = sPIIoTConnectorAdapter;
	}
	
	private static String getTime() {
		return "[" + new Timestamp(System.currentTimeMillis()) + "] ";
	}

}
