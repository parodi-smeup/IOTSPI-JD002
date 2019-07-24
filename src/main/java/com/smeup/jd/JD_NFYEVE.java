package com.smeup.jd;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.smeup.iotspi.jd002.EventComponent;
import com.smeup.iotspi.jd002.LogLevel;
import com.smeup.rpgparser.interpreter.Program;
import com.smeup.rpgparser.interpreter.ProgramParam;
import com.smeup.rpgparser.interpreter.StringType;
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
		parms.add(new ProgramParam("§§FUNZ", new StringType(10)));
		// Sme.UP Method
		parms.add(new ProgramParam("§§METO", new StringType(10)));
		// XML from camera
		parms.add(new ProgramParam("§§SVAR", new StringType(4096)));
		// List of A37 attributes from script
		parms.add(new ProgramParam("A37TAGS", new StringType(4096)));
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
		
		//§§FUNZ='NFY', §§METO='EVE', §§SVAR=Name,Type,Operation JD_LSTFLD, A37TAGS=list of tags and their values 
		if("NFY".equals(funz.trim()) && "EVE".equals(meto.trim())) {
			
			final String a37tags = tags;
			extractTags(a37tags);
			
			final String eventData = svar;
			notifyEvent(eventData);
		}

		return arrayListResponse;
	}

	@SuppressWarnings("unused")
	private void extractTags(final String a37tags) {

		setA37SubId(a37tags.split("@")[0]);
		final String[] rows = a37tags.split("@")[1].split("\\|");

		for (String row : rows) {
			// name
			String name = row.split("\\{")[0];

			// attributes of name var
			String nameAttributes = row.split("\\{")[1];

			// assume 3 attribute (Name, TpVar, DftVal)
			String txt_keyValue = nameAttributes.split("\\]")[0];
			String txt_key = txt_keyValue.split("\\[")[0];
			String tpVar = "";
			if (txt_keyValue.split("\\[").length > 1) {
				tpVar = txt_keyValue.split("\\[")[1];
			}
			
			String tpDato_keyValue = nameAttributes.split("\\]")[1];
			String tpDato_key = tpDato_keyValue.split("\\[")[0];
			String dftVal = "";
			if (tpDato_keyValue.split("\\[").length > 1) {
				dftVal = tpDato_keyValue.split("\\[")[1];
			}
			
			EventComponent eventComponent = new EventComponent(a37SubId);
			String msgLog = getTime() + "EventName:" + name + " Type:" + tpVar + " DftValue:" + dftVal;
			getsPIIoTConnectorAdapter().log(logLevel, msgLog);
			eventComponent.setIEventName(name);
			eventComponent.setIType(tpVar);
			eventComponent.setIDftValue(dftVal);

			this.eventList.put(name, eventComponent);
		}

	}

	private void notifyEvent(final String eventData) {
		String msgLog = getTime() + "Metodo createEvent (listeners: " + getsPIIoTConnectorAdapter().getListenerList().size() + ")";
		getsPIIoTConnectorAdapter().log(logLevel, msgLog);
		
		msgLog = getTime() + "Event data: " + eventData.trim() + ")";
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
				vEvent.setData(vKey, vEvtComp.getIValue());
			}
			
			String[] eventDataSplit = eventData.trim().split("\\|");
			for(String keyValue:eventDataSplit) {
				String key = keyValue.substring(0, keyValue.indexOf("("));
				String value = keyValue.substring(keyValue.indexOf("(")+1, keyValue.indexOf(")") );
				msgLog = getTime() + " datatable evento: key=" + key + " value=" + value  ;
				getsPIIoTConnectorAdapter().log(logLevel, msgLog);
				vEvent.getDataTable().put(key, value);
			}
			
			// invia Evento
			msgLog = getTime() + "Invio evento (fireEventToSmeup)" + vEvent.getDataTable().toString();
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
