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
package multi.allenInterval;

import java.util.ArrayList;

import time.APSPSolver;
import time.Bounds;
import time.TimePoint;
import framework.ConstraintNetwork;
import framework.ConstraintSolver;
import framework.Variable;
import framework.multi.MultiConstraintSolver;

public class AllenIntervalNetworkSolver extends MultiConstraintSolver {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2059523989033941914L;
	
	public AllenIntervalNetworkSolver(long origin, long horizon, int maxActivities) {
		super(new Class[]{AllenIntervalConstraint.class}, AllenInterval.class, createConstraintSolvers(origin, horizon, maxActivities), new int[] {2});
	}
	
	public AllenIntervalNetworkSolver(long origin, long horizon) {
		super(new Class[]{AllenIntervalConstraint.class}, AllenInterval.class, createConstraintSolvers(origin, horizon, -1), new int[] {2});
	}
	
	private static ConstraintSolver[] createConstraintSolvers(long origin, long horizon, int maxActivities) {
		APSPSolver stpSolver;
		if (maxActivities >= 1)
			stpSolver = new APSPSolver(origin, horizon, 2*maxActivities);
		else
			stpSolver = new APSPSolver(origin, horizon);
		return new ConstraintSolver[] {stpSolver};
	}

	/**
	 * Gets the minimum and maximum allowed distance between a pair of {@link TimePoint}s.
	 * @param tpFrom the {@link TimePoint} that is considered as the "origin"/source.  
	 * @param tpTo the {@link TimePoint} that is considered as the "target".
	 * @return Returns the bounds of the distance between the two {@link TimePoint}s
	 * or <code>null</code> if there is no constraint between the two.
	 */
	public final Bounds getAdmissibleDistanceBounds(TimePoint tpFrom, TimePoint tpTo) {
		final APSPSolver stpSolver = (APSPSolver) constraintSolvers[0];
		return stpSolver.getDistanceBounds(tpFrom, tpTo);
	}
	
	/**
	 * Get the minimum and maximum allowed distance between the global source and a {@link TimePoint}. 
	 * Calling this function is equivalent to calling 
	 * {@link #getAdmissibleDistanceBounds(TimePoint, TimePoint)}
	 * With the parameters <code>(<i>Source</i>, tpTo)<code>.
	 * @param tpTo the {@link TimePoint} whose temporal placement should be queried.
	 * @return The allowed bounds of the {@link TimePoint}.
	 */
	public final Bounds getAdmissibleDistanceBounds(TimePoint tpTo) {
		final APSPSolver stpSolver = (APSPSolver) constraintSolvers[0];
		return stpSolver.getDistanceBounds(stpSolver.getSource(), tpTo);
	}
	
	public final double getRigidityNumber(){
		final APSPSolver stpSolver = (APSPSolver) constraintSolvers[0];
		return stpSolver.getRMSRigidity();
	}
	
	/********************/

	@Override
	protected ConstraintNetwork createConstraintNetwork() {
		return new AllenIntervalNetwork(this);
	}
	

//	@Override
//	protected AllenInterval[] createVariablesSub(int num) {
//		HashMap<ConstraintSolver,Integer> ingredients = new HashMap<ConstraintSolver, Integer>();
//		ingredients.put(this.constraintSolvers[0], 2);
//		Variable[][] internalVars = MultiVariable.createInternalVariables(ingredients, num);
//		AllenInterval[] ret = new AllenInterval[num];
//		for (int i = 0; i < num; i++) {
//			ret[i] = new AllenInterval(this, IDs++, this.constraintSolvers, internalVars[i]);
//		}
//		return ret;
//	}

	
//	@Override
//	protected AllenInterval[] createVariablesSub(int num) {
//		AllenInterval[] ret = new AllenInterval[num];
//		for (int i = 0; i < num; i++) {
//			ret[i] = new AllenInterval(this, IDs++, this.constraintSolvers);
//		}
//		
////		Vector<Constraint> cons = new Vector<Constraint>();
////		for (Variable ai : ret) {
////			AllenIntervalConstraint dur = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, AllenIntervalConstraint.Type.Duration.getDefaultBounds());
////			dur.setFrom(ai);
////			dur.setTo(ai);
////			cons.add(dur);
////			dur.setAutoRemovable(true);
////		}
////		this.addConstraints(cons.toArray(new Constraint[cons.size()]));
//		
//		return ret;
//	}

	@Override
	public boolean propagate() {
		// Do nothing, APSPSolver takes care of propagation...
		return true;
	}
	
	private ArrayList<AllenIntervalNetwork> activityNetworkRollback = new ArrayList<AllenIntervalNetwork>();
	
	
	public int bookmark() {
		AllenIntervalNetwork aNet = (AllenIntervalNetwork) ((AllenIntervalNetwork)this.getConstraintNetwork()).clone();
		activityNetworkRollback.add(aNet);
		
		APSPSolver stpSolver = (APSPSolver) constraintSolvers[0];
		return stpSolver.bookmark();
	}
	
	public void removeBookmark( int i ) {
		activityNetworkRollback.remove(i);
		
		APSPSolver stpSolver = (APSPSolver) constraintSolvers[0];
		stpSolver.removeBookmark(i);
	}
	
	public void revert( int i ) {
		this.theNetwork = this.activityNetworkRollback.get(i);
		
		for ( int j = this.activityNetworkRollback.size()-1 ; j >= i ; j-- ) {
			this.activityNetworkRollback.remove(j);
		}		
		
		APSPSolver stpSolver = (APSPSolver) constraintSolvers[0];
		stpSolver.revert(i);
		
		AllenIntervalNetwork aNet = ((AllenIntervalNetwork)this.theNetwork);		
		for ( Variable v : aNet.getVariables() ) {
			AllenInterval vAI = (AllenInterval)v;
			
			vAI.setStart(stpSolver.getEqualTimePoint(vAI.getStart()));
			vAI.setEnd(stpSolver.getEqualTimePoint(vAI.getEnd()));
		}
	}
	
	public int numBookmarks() {
		APSPSolver stpSolver = (APSPSolver) constraintSolvers[0];
		return stpSolver.numBookmarks();
	}

	
}
