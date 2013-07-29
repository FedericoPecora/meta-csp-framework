/*******************************************************************************
 * Copyright (c) 2010-2013 Federico Pecora <federico.pecora@oru.se>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package org.metacsp.onLineMonitoring;

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
