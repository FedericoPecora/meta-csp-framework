package org.metacsp.framework;

public class ConstraintNetworkMarking {
	
	protected String networkState;
	
	public ConstraintNetworkMarking() {
		this.networkState="NONE";
	}
	
	public String getState(){return this.networkState;}
	public void setState(String s){this.networkState=s;}
	
	

}
