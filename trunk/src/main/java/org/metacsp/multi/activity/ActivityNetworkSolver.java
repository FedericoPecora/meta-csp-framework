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
package org.metacsp.multi.activity;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiConstraintSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.allenInterval.AllenIntervalNetworkSolver;
import org.metacsp.multi.symbols.SymbolicValueConstraint;
import org.metacsp.multi.symbols.SymbolicVariableConstraintSolver;
import org.metacsp.time.APSPSolver;
import org.metacsp.utility.UI.PlotActivityNetworkGantt;

public class ActivityNetworkSolver extends MultiConstraintSolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4961558508886363042L;
	protected int IDs = 0;
	protected long origin;
	protected long horizon;
	
	protected static int MAX_ACTIVITIES = 500;
	
	/**
	 * @return the origin
	 */
	public long getOrigin() {
		return origin;
	}

	/**
	 * @return the horizon
	 */
	public long getHorizon() {
		return horizon;
	}
	
	protected ActivityNetworkSolver(Class<?>[] constraintTypes, Class<?> variableType, ConstraintSolver[] internalSolvers, int[] ingredients, long origin, long horizon) {		
		super(constraintTypes,variableType,internalSolvers,ingredients);
		this.origin=origin;
		this.horizon=horizon;
			
	}

	public ActivityNetworkSolver(long origin, long horizon) {
		super(new Class[] {AllenIntervalConstraint.class, SymbolicValueConstraint.class}, Activity.class, createConstraintSolvers(origin,horizon,500), new int[] {1,1});
		this.origin = origin;
		this.horizon = horizon;
	}
	
	public ActivityNetworkSolver(long origin, long horizon, int numActivities) {
		super(new Class[] {AllenIntervalConstraint.class, SymbolicValueConstraint.class}, Activity.class, createConstraintSolvers(origin,horizon,numActivities), new int[] {1,1});
		this.origin = origin;
		this.horizon = horizon;
		MAX_ACTIVITIES = numActivities;
	}

	public ActivityNetworkSolver(long origin, long horizon, String[] symbols) {
		super(new Class[] {AllenIntervalConstraint.class, SymbolicValueConstraint.class}, Activity.class, createConstraintSolvers(origin,horizon,MAX_ACTIVITIES,symbols), new int[] {1,1});
		this.origin = origin;
		this.horizon = horizon;
	}
	
	public ActivityNetworkSolver(long origin, long horizon, int numActivities, String[] symbols) {
		super(new Class[] {AllenIntervalConstraint.class, SymbolicValueConstraint.class}, Activity.class, createConstraintSolvers(origin,horizon,numActivities,symbols), new int[] {1,1});
		this.origin = origin;
		this.horizon = horizon;
		MAX_ACTIVITIES = numActivities;
	}

	protected static ConstraintSolver[] createConstraintSolvers(long origin, long horizon, int numActivities) {
		ConstraintSolver[] ret = new ConstraintSolver[] {new AllenIntervalNetworkSolver(origin, horizon, numActivities), new SymbolicVariableConstraintSolver()};
		return ret;
	}
	
	protected static ConstraintSolver[] createConstraintSolvers(long origin, long horizon, int numActivities, String[] symbols) {
		ConstraintSolver[] ret = new ConstraintSolver[] {new AllenIntervalNetworkSolver(origin, horizon, numActivities), new SymbolicVariableConstraintSolver(symbols, numActivities)};
		return ret;
	}
	
	/**
	 * Get the rigidity number of the underlying {@link APSPSolver}'s {@link ConstraintNetwork}. 
	 */
	public double getRigidityNumber(){
		return (((AllenIntervalNetworkSolver) (this.constraintSolvers[0]))).getRigidityNumber();
	}
	
	/**
	 * Draw all activities on Gantt chart 
	 */
	public void drawAsGantt() {
		new PlotActivityNetworkGantt(this, null, "Activity Network Gantt");
	}
	
	/**
	 * Draw selected variables on Gantt chart
	 * @param selectedVariableNames Only variable/components in this {@link Vector} will be plotted 
	 */
	public void drawAsGantt( Vector<String> selectedVariableNames ) {
		new PlotActivityNetworkGantt(this, selectedVariableNames, "Activity Network Gantt");
	}
	
	/**
	 * Draw variables matching this {@link String} on Gantt chart
	 * @param varsMatchingThis {@link String} that must be contained in a variable to be plotted
	 */
	public void drawAsGantt( String varsMatchingThis ) {
		Vector<String> selectedVariables = new Vector<String>();
		for ( Variable v : this.getVariables() ) {
			if ( v.getComponent().contains(varsMatchingThis)) {
				selectedVariables.add(v.getComponent());
			}
		}
		new PlotActivityNetworkGantt(this, selectedVariables, "Activity Network Gantt");
	}
		
	@Override
	public boolean propagate() {
		// For now, does nothing.  Propagation is taken care of by lower layers (ultimately, the underlying
		// temporal constraints are propagated by the APSPSolver)
		return true;
	}
	
	public int bookmark() {
		AllenIntervalNetworkSolver aSolver = (AllenIntervalNetworkSolver)this.constraintSolvers[0];
		return aSolver.bookmark();
	}
	
	public void removeBookmarks( int i ) {
		AllenIntervalNetworkSolver aSolver = (AllenIntervalNetworkSolver)this.constraintSolvers[0];
		aSolver.removeBookmark(i);
	}
	
	public void revert( int i ) {
		AllenIntervalNetworkSolver aSolver = (AllenIntervalNetworkSolver)this.constraintSolvers[0];
		aSolver.revert(i);
	}
	
	public int numBookmarks() {		
		AllenIntervalNetworkSolver aSolver = (AllenIntervalNetworkSolver)this.constraintSolvers[0];
		return aSolver.numBookmarks();
	}
	
	public Activity[] getActivitiesWithSymbols(String component, String[] values) {
		Variable[] vars = this.getConstraintNetwork().getVariables(component);
		return getActivitiesWithSymbolsHelper(vars, values);
	}
	
	public Activity[] getActivitiesWithSymbols(String[] values) {
		Variable[] vars = this.getConstraintNetwork().getVariables();
		return getActivitiesWithSymbolsHelper(vars, values);
	}
	
	private Activity[] getActivitiesWithSymbolsHelper(Variable[] vars, String[] values) {
		Vector<Activity> ret = new Vector<Activity>();
		for (Variable var : vars) {
			Activity act = (Activity)var;
			List<String> symbolList = Arrays.asList(act.getSymbolicVariable().getSymbols());
			for (String value : values) {
				if (symbolList.contains(value)) {
					ret.add(act);
					break;
				}
			}
		}
		return ret.toArray(new Activity[ret.size()]);		
	}

}
