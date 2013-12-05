package org.metacsp.dispatching;

import org.metacsp.multi.activity.Activity;

public abstract class DispatchingFunction {
	
	private String component;
	
	public DispatchingFunction(String component) {
		this.component = component;
	}
	
	public String getComponent() { return component; }
	
	public abstract void dispatch(Activity act);
	

}
