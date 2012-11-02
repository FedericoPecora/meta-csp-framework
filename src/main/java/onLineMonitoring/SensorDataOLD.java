package onLineMonitoring;

import java.util.HashMap;

public class SensorDataOLD {

	private String stateVarible;
	private HashMap<String, Double> svalue = new HashMap<String, Double>();;
	private String[] states = null;
	private double[] psbs = null;
	private int activityIndex = 0;
	private boolean isAdded;
	public SensorDataOLD() {
		// TODO Auto-generated constructor stub
		this.isAdded = false;
	}
	
	public SensorDataOLD(String stateVarible, String name, HashMap<String, Double> svalue){
		this.stateVarible = stateVarible;
		this.svalue = svalue;
		this.isAdded = false;
	}
	
	public void setAdded(boolean isAdded) {
		this.isAdded = isAdded;
	}
	
	public void setStateVarible(String stateVarible) {
		this.stateVarible = stateVarible;
	}
	
	public void setStates(String ...strings) {
		this.states = strings;
	}
	
	public void setActivityIndex(int activityIndex) {
		this.activityIndex = activityIndex;
	}
	
	public void setPsbs(double ...ds) {
		this.psbs = ds;
		for(int i = 0; i < states.length; i++)
			this.svalue.put(states[i], psbs[i]);		
	}
	
	public String getStateVarible() {
		return stateVarible;
	}
	
	public HashMap<String, Double> getSvalue() {
		return svalue;
	}
	
	public int getActivityIndex() {
		return activityIndex;
	} 
	
	public boolean isAdded() {
		return isAdded;
	}
	
	}
