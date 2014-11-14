package org.metacsp.dispatching;

import org.metacsp.multi.activity.SymbolicVariableActivity;

public abstract class DispatchingFunction {
	
	private String component;
	
	private Dispatcher dis;
	
	public DispatchingFunction(String component) {
		this.component = component;
	}
	
	public void registerDispatcher(Dispatcher dis) {
		this.dis = dis;
	}
	
	public String getComponent() { return component; }
	
	public abstract void dispatch(SymbolicVariableActivity act);
	
	public void finish(SymbolicVariableActivity act) {
		dis.finish(act);
	}
	

}
