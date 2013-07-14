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
package multi.activity;

import java.util.Vector;

import multi.allenInterval.AllenIntervalConstraint;
import multi.allenInterval.AllenIntervalNetworkSolver;
import multi.symbols.SymbolicValueConstraint;
import multi.symbols.SymbolicVariableConstraintSolver;
import utility.UI.PlotActivityNetworkGantt;
import framework.ConstraintNetwork;
import framework.ConstraintSolver;
import framework.Variable;
import framework.multi.MultiConstraintSolver;

public class ActivityNetworkSolver extends MultiConstraintSolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4961558508886363042L;
	protected int IDs = 0;
	protected long origin;
	
	private static int MAX_ACTIVITIES = 500;
	
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

	private long horizon;
	
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

	private static ConstraintSolver[] createConstraintSolvers(long origin, long horizon, int numActivities) {
		ConstraintSolver[] ret = new ConstraintSolver[] {new AllenIntervalNetworkSolver(origin, horizon, numActivities), new SymbolicVariableConstraintSolver()};
		return ret;
	}
	
	private static ConstraintSolver[] createConstraintSolvers(long origin, long horizon, int numActivities, String[] symbols) {
		ConstraintSolver[] ret = new ConstraintSolver[] {new AllenIntervalNetworkSolver(origin, horizon, numActivities), new SymbolicVariableConstraintSolver(symbols, numActivities)};
		return ret;
	}
	
	/**
	 * Get the rigidity number belonging to the  underlaying AllenIntervalNetworkSolver that in turn exploits an APSP solver
	 * @param selectedVariableNames Only variable/components in this {@link Vector} will be plotted 
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
		
//	@Override
//	protected Variable[] createVariablesSub(int num) {
//		Variable[] ret = new Variable[num];
//		for (int i = 0; i < num; i++)
//			ret[i] = new Activity(this, IDs++, this.constraintSolvers); 
//		// Has been moved to AllenIntervalNetworkSolver because it is bypassed 
//		// if AllenIntervalNetworkSolver is used without ActivityIntervalNetworkSolver 
////		Vector<Constraint> cons = new Vector<Constraint>();
////		for (Variable ai : ret) {
////			AllenIntervalConstraint dur = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, AllenIntervalConstraint.Type.Duration.getDefaultBounds());
////			dur.setFrom(ai);
////			dur.setTo(ai);
////			cons.add(dur);
////		}
////		this.addConstraints(cons.toArray(new Constraint[cons.size()]));
//		return ret;
//	}

	@Override
	public boolean propagate() {
		// For now, does nothing.  Propagation is take care of by lower layers (ultimately, the underlying
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

}
