package org.metacsp.sensing;

import java.util.HashMap;

public class Controllable  extends Sensor{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2683728513558834245L;

	public Controllable(String name, ConstraintNetworkAnimator animator) {
		super(name, animator);
		// TODO Auto-generated constructor stub
	}
	
	protected static String parseName(String everything) {
		String ret = everything.substring(everything.indexOf("Controllable")+6);
		ret = ret.substring(0,ret.indexOf(")")).trim();
		return ret;
	}
	
	protected static HashMap<Long,String> parseSensorValue(String everything) {
		HashMap<Long,String> ret = new HashMap<Long,String>();
		int lastSV = everything.lastIndexOf("ControllableValue");
		while (lastSV != -1) {
			int bw = lastSV;
			int fw = lastSV;
			while (everything.charAt(--bw) != '(') { }
			int parcounter = 1;
			while (parcounter != 0) {
				if (everything.charAt(fw) == '(') parcounter++;
				else if (everything.charAt(fw) == ')') parcounter--;
				fw++;
			}
			String element = everything.substring(bw,fw);
			String value = element.substring(element.indexOf("ControllableValue")+11).trim();
			long time = Long.parseLong(value.substring(value.indexOf(" "),value.lastIndexOf(")")).trim());
			value = value.substring(0,value.indexOf(" ")).trim();
			ret.put(time,value);
			everything = everything.substring(0,bw);
			lastSV = everything.lastIndexOf("ControllableValue");
		}
		return ret;
	}



}
