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
package org.metacsp.meta.fuzzyActivity;

import java.util.Vector;

import org.metacsp.multi.fuzzyActivity.FuzzyActivity;
import org.metacsp.multi.fuzzyActivity.FuzzyActivityNetworkSolver;
import org.metacsp.multi.symbols.SymbolicValueConstraint;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.meta.MetaConstraintSolver;
import org.metacsp.framework.meta.MetaVariable;
import org.metacsp.fuzzyAllenInterval.FuzzyAllenIntervalConstraint;
import org.metacsp.fuzzyAllenInterval.FuzzyAllenIntervalNetworkSolver;
import org.metacsp.fuzzySymbols.FuzzySymbolicVariableConstraintSolver;

/**
 * Provides a meta-CSP implementation of fuzzy context inference.  The solver
 * combines fuzzy symbolic inference and fuzzy temporal inference.  The former
 * is provided by a {@link FuzzySymbolicVariableConstraintSolver}, while the latter is
 * provided by a {@link FuzzyAllenIntervalNetworkSolver} (see {@link FuzzyActivityNetworkSolver}).
 * 
 * <br>
 * This solver uses Branch-and-Bound search to find the optimal unifications of
 * rules to existing {@link FuzzyActivity} variables (see {@link FuzzyActivityDomain}).
 * 
 * @author Federico Pecora, Masoumeh Mansouri
 */

public class FuzzyActivityMetaSolver extends MetaConstraintSolver {

	private static final long serialVersionUID = -3342951089757068845L;
	private double upperBound = 0;
	private double lowerBound = 0;
	private double tmpLoweBound = 0;
	private ConstraintNetwork cn;
	private ConstraintNetwork optCn;
	private double valueConsistency = 0;
	private double temporalConsistency = 0;
	private double vcTmp = 0;
	private double tcTmp = 0;
	public FuzzyActivityMetaSolver(long animationTime) {
		super(new Class[]{FuzzyAllenIntervalConstraint.class, SymbolicValueConstraint.class}, animationTime, new FuzzyActivityNetworkSolver());
	}
	
		
	@Override
	public void preBacktrack() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postBacktrack(MetaVariable mv) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void retractResolverSub(ConstraintNetwork metaVariable, ConstraintNetwork metaValue) {
		//FuzzyActivityNetworkSolver groundSolver = (FuzzyActivityNetworkSolver)((FuzzyActivityDomain)this.domainFeatures.get(0)).getConstraintSolver();
	
//		Vector<Variable> toRemove = new Vector<Variable>();
//		for (Variable v : metaValue.getVariables()) 
//			if (!metaVariable.containsVariable(v))
//				toRemove.add(v);

		
		//((FuzzyActivityDomain)this.metaConstraints.get(0)).removeFromNetwork(metaVariable, toRemove);
		((FuzzyActivityDomain)this.metaConstraints.get(0)).setUnjustified(metaVariable);
		
	}
	


	@Override
	protected boolean addResolverSub(ConstraintNetwork metaVariable,
			ConstraintNetwork metaValue) {
		return true;
		
	}



	@Override
	protected double getUpperBound() {
		// TODO Auto-generated method stub
		return this.upperBound;
	}


	@Override
	protected void setUpperBound() {
		
		this.upperBound = ((FuzzyActivityDomain)this.metaConstraints.get(0)).getConsitency();
		cn = ((FuzzyActivityDomain)this.metaConstraints.get(0)).getConstraintNetwork();
		vcTmp = ((FuzzyActivityDomain)this.metaConstraints.get(0)).getValueConsistency();
		tcTmp = ((FuzzyActivityDomain)this.metaConstraints.get(0)).getTemporalConsistency();
		tmpLoweBound = upperBound;
	}


	@Override
	protected double getLowerBound() {
		return this.lowerBound;
	}


	@Override
	protected void setLowerBound() {
		if(tmpLoweBound > lowerBound){
			this.lowerBound = tmpLoweBound;
			optCn = cn;
			valueConsistency = vcTmp;
			temporalConsistency = tcTmp;
			System.out.println("getLowebound: " + lowerBound);
			//System.out.println("optCn: " + optCn);
		}
		System.out.println("...........................................................");
	}

	@Override
	protected boolean hasConflictClause(ConstraintNetwork metaValue){
		Vector<Constraint> cons = new Vector<Constraint>();
		cons = ((FuzzyActivityDomain)this.metaConstraints.get(0)).getFalseClause();
		for (int i = 0; i < metaValue.getConstraints().length; i++) {
			for (int j = 0; j < cons.size(); j++) {
				if(isAFalseClause(metaValue.getConstraints()[i], cons.get(j)))
					return true;
			}
		}
		return false;
	}
	
	@Override
	protected void resetFalseClause(){

		((FuzzyActivityDomain)this.metaConstraints.get(0)).resetFalseClause();

	}
	

	private boolean isAFalseClause(Constraint c1, Constraint c2) {
		if((c1.getScope()[0].getID() == c2.getScope()[0].getID()) && (c1.getScope()[1].getID() == c2.getScope()[1].getID()))
			return true;
		if((c1.getScope()[0].getID() == c2.getScope()[1].getID()) && (c1.getScope()[0].getID() == c2.getScope()[1].getID()))
			return true;
		return false;
	}


	public ConstraintNetwork getOptimalConstraint() {
		return optCn;
		
	}
	
	public String getMostLiklyOccuredActivities(){
		
		String str = ((FuzzyActivityDomain)this.metaConstraints.get(0)).getOptimalHypothesis(optCn, valueConsistency, temporalConsistency);	
		return str;
	}

}
