package org.metacsp.multi.activity;

import org.metacsp.framework.Variable;
import org.metacsp.multi.allenInterval.AllenInterval;
import org.metacsp.multi.symbols.SymbolicVariable;

public interface Activity {

	/**
	 * @return The {@link AllenInterval} representing the temporal value of this {@link SymbolicVariableActivity}.
	 */
	public AllenInterval getTemporalVariable();
	
	/**
	 * @return The a description of this {@link Activity}'s symbolic variable.
	 */
	public String[] getSymbols();

	/**
	 * @return The {@link AllenInterval} representing the temporal value of this {@link SymbolicVariableActivity}.
	 */

	
	public Variable getVariable();


}
