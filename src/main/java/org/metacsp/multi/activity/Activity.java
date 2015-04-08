package org.metacsp.multi.activity;

import org.metacsp.framework.Variable;
import org.metacsp.multi.allenInterval.AllenInterval;

public interface Activity {

	/**
	 * @return The {@link AllenInterval} representing the temporal value of this {@link SymbolicVariableActivity}.
	 */
	public AllenInterval getTemporalVariable();
	
	/**
	 * @return A description of this {@link Activity}'s symbolic variable.
	 */
	public String[] getSymbols();

	
	public Variable getVariable();


}
