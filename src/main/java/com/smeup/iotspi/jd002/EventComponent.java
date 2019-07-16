package com.smeup.iotspi.jd002;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.dom4j.Document;
import org.dom4j.Node;



public class EventComponent {

	//indici parametri	
	public static final int CMD = 0;
	public static final int IN = 1;
	public static final int OUT = 2;
	//public static final int INOUT = 3;
	//Tipi di dati 
	public static final int INT = 11;
	public static final int STRING = 22;
	public static final int BOOL = 33;
	public static final int REAL = 44;
	//Quando leggere l'evento
	public static final int R_ALLWAYS = 10;
	public static final int R_ONSETDFT = 20;
	public static final int R_ONCHGDFT = 30;
	public static final int R_ONCNDVAR = 40;
	public static final int R_NEVER = 50;


	private boolean iMsgRet = false;
	// Tipo Input/Output
	private int iType = EventComponent.IN;
	private int iDataType = EventComponent.STRING;

	// Quando leggere l'evento di OUTPUT dal PLC
	@SuppressWarnings("unused")
	private int iWhenReadEvt = 10;
	//indicatore stato attivo
	private String iDftValue = "";
	@SuppressWarnings("unused")
	private String iEventId = "";
	private String iEventName = "";
	private String iEventDesc = "";
	// Riferimento alla classe che gestisce  gli eventi in un Thread(Gruppo) Separato
	@SuppressWarnings("unused")
	private String iReqDelivGroup="";
	private Hashtable<String,String[]> iHowRead = null;

	private String iValue = "";

	/************************************
	 * Costruttore con una stringa con 
	 * l'indirizzo
	 * @param aEventId
	 ************************************/
	public EventComponent(String aEventId) {
		iEventId = aEventId;
	}

	public boolean getIsMsgRet() {
		return iMsgRet;
	}
	public void setIsMsgRet(boolean aMsgRet) {
		this.iMsgRet = aMsgRet;
	}

	public int getIType() {
		return iType;
	}
	public void setIType(int aType) {
		this.iType = aType;
	}
	public void setIType(String aDataType)
	{
		if("IN".equalsIgnoreCase(aDataType)) setIType(EventComponent.IN);
		else if("OUT".equalsIgnoreCase(aDataType)) setIType(EventComponent.OUT);
		//else if("INOUT".equalsIgnoreCase(aDataType)) setIType(EventComponent.INOUT);
		else setIType(EventComponent.CMD);
	}

	public int getIDataType() {
		return iDataType;
	}
	public void setIDataType(int aDataType) {
		iDataType = aDataType;
	}
	public void setIDataType(String aDataType)
	{
		if("INT".equalsIgnoreCase(aDataType)) setIDataType(EventComponent.INT);
		else if("BOOL".equalsIgnoreCase(aDataType)) setIDataType(EventComponent.BOOL);
		else if("REAL".equalsIgnoreCase(aDataType)) setIDataType(EventComponent.REAL);
		else setIDataType(EventComponent.STRING);
	}

	public String getIDftValue() {
		return iDftValue;
	}
	public void setIDftValue(String aDftValue) {
		iDftValue = aDftValue;
	}

	public String getIEventName() {
		return iEventName;
	}
	public void setIEventName(String aName) {
		iEventName = aName;
	}

	public String getIEventDesc() {
		return iEventDesc;
	}
	public void setIEventDesc(String aDesc) {
		iEventDesc = aDesc;
	}

	public String getIValue() {
		return iValue;
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	public void setIValue(Document aDoc) throws Exception {

		Hashtable<String,String[]> vHowRead = iHowRead;
		String vValue = "";
		try {

			Vector vVector = new Vector(vHowRead.keySet());
			Collections.sort(vVector);
			Iterator vIter = vVector.iterator();
			if(!(vIter.hasNext()))
				vValue=getIDftValue();
			while (vIter.hasNext()) {
				String vKey =  (String)vIter.next();
				String vFun = (vKey.split("-"))[1];
				if(vFun.equalsIgnoreCase("CONCAT")) {
					/*
					 * la funzione CONCAT prevede 1 parametro
					 * concatena la stringa definita nel parametro
					 */
					String[] vParams = vHowRead.get(vKey);
					String strConcat = vParams[0].replace("'", "");
					vValue += strConcat;
				}
				else if(vFun.equalsIgnoreCase("TAG")) {
					/*
					 * la funzione TAG prevede 1 parametro
					 * NOME_TAG
					 */
					String[] vParams = vHowRead.get(vKey);
					String tagName = vParams[0].replace("'", "");
					//ricerca nodi tagName
					List<Node> nodes = aDoc.getRootElement().selectNodes(tagName);
					for (Node node : nodes) {
						vValue += node.getText();
					}
				}
				else if(vFun.equalsIgnoreCase("DIV")) {
					/*
					 * la DIV prevede 1 parametro
					 * divisore
					 */
					try {
						String[] vParams = vHowRead.get(vKey);
						double vDiv = Double.parseDouble(vParams[0]);
						double vDblValue = 0;
						try {
							vDblValue = Double.parseDouble(vValue);
						}
						catch(Exception vEx) {
							vDblValue = 0;
						}
						vValue = Double.toString((vDblValue/vDiv));
					}
					catch(Exception vEx) {
						vValue = "0";
						throw new Exception("Errore funzione DIV");
					}
				}
				else if(vFun.equalsIgnoreCase("DIR2DIR")) {
					/*
					 * la DIR2DIR non prevede parametri
					 */
					try {
						String[] vParams = vHowRead.get(vKey);
						if(vValue.equalsIgnoreCase("APPROACH"))
							vValue="ENTRATA";
						else if(vValue.equalsIgnoreCase("GOAWAY"))
							vValue="USCITA";
					}
					catch(Exception vEx) {
						throw new Exception("Errore funzione DIR2DIR");
					}
				}
				else if(vFun.equalsIgnoreCase("SPLIT")) {
					/*
					 * la SPLIT prevede 2 parametri
					 * SEPARATOR
					 * INSTANCE
					 */
					try {
					String[] vParams = vHowRead.get(vKey);
					String vSep = vParams[0].replace("'", "");
					int vInst = Integer.parseInt(vParams[1]);
					vValue=(vValue.split(vSep))[vInst];
					}
					catch(Exception vEx) {
						throw new Exception("Errore funzione SPLIT");
					}
				}
				else if(vFun.equalsIgnoreCase("DATE2DATE")) {
					/*
					 * la DATE2DATE prevede 2 parametri
					 * formato iniziale
					 * formato finale
					 */
					try {
						String[] vParams = vHowRead.get(vKey);
						String vFmtSource = vParams[0].replace("'", "");
						String vFmtDest = vParams[1].replace("'", "");

						DateFormat vFormatSource = new SimpleDateFormat(vFmtSource);
						Date date = vFormatSource.parse(vValue);
						vValue = new SimpleDateFormat(vFmtDest).format(date);
					}
					catch(Exception vEx) {
						vValue=(new Date()).toString();
						throw new Exception("Errore funzione DATE2DATE");
					}
				}
				//else if(vFun.equalsIgnoreCase("PROVA2")) {
				/*
				 * la PROVA2 prevede 1 parametro
				 * VALORE
				 */
				/*String[] vParams = vHowRead.get(vKey);
						int vInst = Integer.parseInt(vParams[0]);
						vValue=vValue + Integer.toString(vInst);
					}*/	
			}
			iValue = vValue;
		}
		catch (Exception vEx) {
			iValue = vValue;
			throw new Exception("Metodo SetIValue - " + vEx.getMessage());
		}
	}

	public void setIValue(String aValue) {
		try {
			iValue = aValue;
		}
		catch (Exception vEx) {
			vEx.printStackTrace();
			iValue = "";
		}	
	}

	@SuppressWarnings({ "rawtypes", "unused" })
	public void setICMD(String aValue) throws Exception {
		HashMap vResult = new HashMap();
		try
		{
			//setta il valore del comando e lo deve eventualmente inviare
			setIValue(aValue);
		}
		catch (Exception vEx)
		{
			throw new Exception(vEx.getMessage());
		}
	}

	public Hashtable<String,String[]> getIHowRead() {
		return iHowRead;
	}
	public void setIHowRead(String aMode) {

		Hashtable<String,String[]> aHowRead = new Hashtable<String,String[]>();
		if(!aMode.trim().equals("")) {
			String[] vFuns = aMode.split("\\);");
			for (int i = 0; i < vFuns.length; i++) {
				if(i == vFuns.length-1) vFuns[i]=vFuns[i].substring(0, vFuns[i].length()-1);
				String[] vParams = null;
				int vPosParams = vFuns[i].indexOf("(");
				String vFun = vFuns[i].substring(0, vPosParams);
				String vParamsStr = "";
				if(vFuns[i].length() > vPosParams+1) 
					vParamsStr = vFuns[i].substring(vPosParams+1);
				if(!vParamsStr.equals(""))
					vParams=vParamsStr.split(",");
				else
					vParams=new String[0];
				aHowRead.put(String.format("%03d", i) + "-" + vFun, vParams);
			}	
		}
		iHowRead = aHowRead;
	}

}
