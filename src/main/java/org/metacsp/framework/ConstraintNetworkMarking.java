package org.metacsp.framework;

import java.io.Serializable;



public class ConstraintNetworkMarking implements Serializable {
	
	private static final long serialVersionUID = 1979722716677315395L;
	protected String networkState;

	// the state has to be replaced by this
	public enum markings{OBSERVABLE,IMPOSSIBLE,NONE}

	
	public ConstraintNetworkMarking() {
		this.networkState="NONE";
	}
	
	public ConstraintNetworkMarking(String state){
		this.networkState=state;
	}
	
	public String getState(){return this.networkState;}
	public void setState(String s){
		this.networkState=s;
	}
	
	

}
