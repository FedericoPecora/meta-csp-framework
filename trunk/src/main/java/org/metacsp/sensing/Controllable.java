package org.metacsp.sensing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

public class Controllable {


	/**
	 * 
	 */
	private static final long serialVersionUID = 2683728513558834245L;
	private Vector<String> syms = new Vector<String>();
	


	
	public void registerSymbolsFromControllableSensor(String act){
		syms.add(act);
	}
	
	
	public Vector<String> getContrallbaleSymbols(){
		return syms;
	}
	
//	protected static String parseName(String everything) {
//		String ret = everything.substring(everything.indexOf("Controllable")+12);
//		ret = ret.substring(0,ret.indexOf(")")).trim();
//		return ret;
//	}
//	
//	protected static HashMap<Long,String> parseControllableValue(String everything) {
//		HashMap<Long,String> ret = new HashMap<Long,String>();
//		int lastSV = everything.lastIndexOf("ControllableValue");
//		while (lastSV != -1) {
//			int bw = lastSV;
//			int fw = lastSV;
//			while (everything.charAt(--bw) != '(') { }
//			int parcounter = 1;
//			while (parcounter != 0) {
//				if (everything.charAt(fw) == '(') parcounter++;
//				else if (everything.charAt(fw) == ')') parcounter--;
//				fw++;
//			}
//			String element = everything.substring(bw,fw);
//			String value = element.substring(element.indexOf("ControllableValue")+17).trim();
//			long time = Long.parseLong(value.substring(value.indexOf(" "),value.lastIndexOf(")")).trim());
//			value = value.substring(0,value.indexOf(" ")).trim();
//			ret.put(time,value);
//			everything = everything.substring(0,bw);
//			lastSV = everything.lastIndexOf("ControllableValue");
//		}
//		return ret;
//	}
//	
//	public void registerControllableSensorTrace(String sensorTraceFile) {
//		String everything = null;
//		try {
//			BufferedReader br = new BufferedReader(new FileReader(sensorTraceFile));
//			try {
//				StringBuilder sb = new StringBuilder();
//				String line = br.readLine();
//				while (line != null) {
//					if (!line.startsWith("#")) {
//						sb.append(line);
//						sb.append('\n');
//					}
//					line = br.readLine();
//				}
//				everything = sb.toString();
//				String name = parseName(everything);
//				if (name.equals(this.name)) {
//					HashMap<Long,String> controllableValues = parseControllableValue(everything);
//					System.out.println("ControllableValues");
//					System.out.println(controllableValues);
//					animator.registerControllableValuesToDispatch(this, controllableValues);
//				}
//			}
//			finally { br.close(); }
//		}
//		catch (FileNotFoundException e) { e.printStackTrace(); }
//		catch (IOException e) { e.printStackTrace(); }
//	}



}
