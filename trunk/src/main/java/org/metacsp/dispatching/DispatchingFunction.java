package org.metacsp.dispatching;

import org.metacsp.multi.activity.Activity;

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
	
	public abstract void dispatch(Activity act);
	
	public void finish(Activity act) {
		dis.finish(act);
	}
	

}
