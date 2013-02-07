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
package multi.fuzzyActivity;

import java.util.Vector;

import symbols.SymbolicValueConstraint;
import symbols.fuzzySymbols.FuzzySymbolicVariableConstraintSolver;
import framework.Constraint;
import framework.ConstraintNetwork;
import framework.ConstraintSolver;
import framework.Variable;
import framework.multi.MultiConstraintSolver;
import fuzzyAllenInterval.FuzzyAllenIntervalConstraint;
import fuzzyAllenInterval.FuzzyAllenIntervalNetworkSolver;

public class FuzzyActivityNetworkSolver extends MultiConstraintSolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2189153700229621990L;
	private int IDs = 0;
	
	public FuzzyActivityNetworkSolver() {
		super(new Class[] {FuzzyAllenIntervalConstraint.class, SymbolicValueConstraint.class}, new Class[]{FuzzyActivity.class}, createConstraintSolvers());
	}

	private static ConstraintSolver[] createConstraintSolvers() {
		ConstraintSolver[] ret = new ConstraintSolver[] {new FuzzyAllenIntervalNetworkSolver(), new FuzzySymbolicVariableConstraintSolver()};
		return ret;
	}

	@Override
	protected ConstraintNetwork createConstraintNetwork() {
		return new FuzzyActivityNetwork(this);
	}

	@Override
	protected Variable[] createVariablesSub(int num) {
		Variable[] ret = new Variable[num];
		for (int i = 0; i < num; i++) ret[i] = new FuzzyActivity(this, IDs++, this.constraintSolvers); 
		return ret;
	}

	@Override
	public boolean propagate() {
		// Does nothing... everything is done by the two underlying solvers (FuzzyAllenIntervalNetworkSolver and SymbolicVariableNetworkSolver)
		return true;
	}
	
	public void setVarOfSubGraph(Vector<FuzzyActivity> fas) {
		((FuzzySymbolicVariableConstraintSolver)this.constraintSolvers[1]).setVarOfSubGraph(fas);
		((FuzzyAllenIntervalNetworkSolver)this.constraintSolvers[0]).setVarOfSubGraph(fas);
	}
	
	
	public double getTemporalConsistency() {
		return ((FuzzyAllenIntervalNetworkSolver)this.constraintSolvers[0]).getPosibilityDegree();
	}
	
	public double getValueConsistency() {
		return ((FuzzySymbolicVariableConstraintSolver)this.constraintSolvers[1]).getUpperBound();
	}
	
	public Vector<Constraint> getFalseClause() {
		return ((FuzzySymbolicVariableConstraintSolver)this.constraintSolvers[1]).getFalseConstraint();
	}
	
	
	public void resetFalseClauses() {
		((FuzzySymbolicVariableConstraintSolver)this.constraintSolvers[1]).resetFalseClauses();
	}
	

	public void setCrispCons(Constraint[] crispCons) {
		((FuzzyAllenIntervalNetworkSolver)this.constraintSolvers[0]).setCrispCons(crispCons);
		
	}
	
}
