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

import fuzzySymbols.FuzzySymbolicVariable;
import framework.Constraint;
import framework.ConstraintSolver;
import framework.Domain;
import framework.Variable;
import framework.multi.MultiVariable;

public class FuzzyActivity extends MultiVariable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6700038298629079368L;
	//private String[] symbols;
//	private SimpleAllenInterval temporalVariable;
//	private FuzzySymbolicVariable fuzzySymbolicVariable; 
	//private boolean mask = true;//iran 
	private Vector<FuzzyActivity> dependencies;//iran
	private boolean isHypothesis = false;//iran
	
	public FuzzyActivity(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers, Variable[] internalVars) {
		super(cs, id, internalSolvers, internalVars);
		dependencies = new Vector<FuzzyActivity>();
	}
	
	public void setDependencies(Vector<FuzzyActivity> dependencies) {
		this.dependencies = dependencies;
	}
	
	public Vector<FuzzyActivity> getDependencies() {
		return dependencies;
	}
	
	public void setIsHypothesis(boolean isHypothesis) {
		this.isHypothesis = isHypothesis;
	}
	
	public boolean IsHypothesis()
	{
		return isHypothesis;
	}
	
	/*
	public void setSymbolicDomain(String... symbols) {
		this.symbols = symbols;
		fuzzySymbolicVariable.setDomain(this.symbols);
	}
	
	public void setPossibilityDegree(double... vals) throws PossibilityDegreeMismathcException{
		fuzzySymbolicVariable.setPossibilityDegree(vals);
	}
	*/
	
	public void setDomain(String[] symbols, double[] vals) {
		//fuzzySymbolicVariable.setDomain(symbols, vals);
		((FuzzySymbolicVariable)this.getInternalVariables()[1]).setDomain(symbols, vals);
	}

	
	
	@Override
	protected Constraint[] createInternalConstraints(Variable[] variables) {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	protected Variable[] createInternalVariables() {
//		SimpleAllenInterval temporalVariable = (SimpleAllenInterval)internalSolvers[0].createVariable();
//		FuzzySymbolicVariable fuzzySymbolicVariable = (FuzzySymbolicVariable)internalSolvers[1].createVariable();
//		this.fuzzySymbolicVariable = fuzzySymbolicVariable; 
//		this.temporalVariable = temporalVariable;
//		return new Variable[]{temporalVariable,fuzzySymbolicVariable};
//	}

	@Override
	public void setDomain(Domain d) {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		//return "<" + this.fuzzySymbolicVariable + ">U<" + this.temporalVariable + ">";
		return "<" + this.getInternalVariables()[1] + ">U<" + this.getInternalVariables()[0] + ">";
		//return "<" + this.fuzzySymbolicVariable.toString();
	}

	@Override
	public int compareTo(Variable arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

}
