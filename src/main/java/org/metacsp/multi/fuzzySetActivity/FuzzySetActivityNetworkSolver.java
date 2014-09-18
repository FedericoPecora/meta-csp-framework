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
package org.metacsp.multi.fuzzySetActivity;

import java.util.Vector;

import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.allenInterval.AllenIntervalNetworkSolver;
import org.metacsp.multi.symbols.SymbolicValueConstraint;
import org.metacsp.time.APSPSolver;
import org.metacsp.fuzzySymbols.FuzzySymbolicVariableConstraintSolver;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.multi.MultiConstraintSolver;


public class FuzzySetActivityNetworkSolver extends MultiConstraintSolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2189153700229621990L;
	private int IDs = 0;
	protected long origin;
	
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

	protected long horizon;
	
	public FuzzySetActivityNetworkSolver(long origin, long horizon, int numActs) {
		super(new Class[] {AllenIntervalConstraint.class, SymbolicValueConstraint.class}, FuzzySetActivity.class, createConstraintSolvers(origin, horizon, numActs), new int[] {1,1});
		this.origin = origin;
		this.horizon = horizon;
		MAX_ACTIVITIES = numActs;
	}
	
	public FuzzySetActivityNetworkSolver(long origin, long horizon) {
		super(new Class[] {AllenIntervalConstraint.class, SymbolicValueConstraint.class}, FuzzySetActivity.class, createConstraintSolvers(origin, horizon), new int[] {1,1});
		this.origin = origin;
		this.horizon = horizon;
	}
	
	/**
	 * Get the rigidity number of the underlying {@link APSPSolver}'s {@link ConstraintNetwork}. 
	 */
	public double getRigidityNumber(){
		return (((AllenIntervalNetworkSolver) (this.constraintSolvers[0]))).getRigidityNumber();
	}
	
	private static ConstraintSolver[] createConstraintSolvers(long origin, long horizon, int numActs) {
		ConstraintSolver[] ret = new ConstraintSolver[] {new AllenIntervalNetworkSolver(origin,horizon,numActs), new FuzzySymbolicVariableConstraintSolver()};
		return ret;
	}

	private static ConstraintSolver[] createConstraintSolvers(long origin, long horizon) {
		ConstraintSolver[] ret = new ConstraintSolver[] {new AllenIntervalNetworkSolver(origin,horizon), new FuzzySymbolicVariableConstraintSolver()};
		return ret;
	}

	@Override
	public boolean propagate() {
		// Does nothing... everything is done by the two underlying solvers (FuzzyAllenIntervalNetworkSolver and SymbolicVariableNetworkSolver)
		return true;
	}
	
//	public void setVarOfSubGraph(Vector<FuzzySetActivity> fas) {
//		((FuzzySymbolicVariableConstraintSolver)this.constraintSolvers[1]).setVarOfSubGraph(fas);
//	}
		
	public double getValueConsistency() {
		return ((FuzzySymbolicVariableConstraintSolver)this.constraintSolvers[1]).getUpperBound();
	}
	
	public Vector<Constraint> getFalseClause() {
		return ((FuzzySymbolicVariableConstraintSolver)this.constraintSolvers[1]).getFalseConstraint();
	}
	
	
	public void resetFalseClauses() {
		((FuzzySymbolicVariableConstraintSolver)this.constraintSolvers[1]).resetFalseClauses();
	}
	

}
