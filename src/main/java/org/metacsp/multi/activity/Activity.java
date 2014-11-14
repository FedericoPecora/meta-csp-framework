package org.metacsp.multi.activity;

import org.metacsp.framework.Variable;
import org.metacsp.multi.allenInterval.AllenInterval;

public interface Activity {

	/**
	 * @return The {@link AllenInterval} representing the temporal value of this {@link SymbolicVariableActivity}.
	 */
	public AllenInterval getTemporalVariable();
	
	public Variable getVariable();


}
