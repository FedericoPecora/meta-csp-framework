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
package org.metacsp.multi.symbols;

import java.util.HashSet;

import org.metacsp.booleanSAT.BooleanSatisfiabilitySolver;
import org.metacsp.booleanSAT.BooleanVariable;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiConstraintSolver;
import org.metacsp.multi.symbols.SymbolicValueConstraint.Type;


public class SymbolicVariableConstraintSolver extends MultiConstraintSolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4961558508886363042L;
	protected int IDs = 0;
	protected String[] symbols;
	protected boolean singleValue = true;
	protected boolean enumerateSets = true;
	protected static SymbolicVariableConstraintSolver thisSolver = null;
	protected HashSet<Constraint> toMask = new HashSet<Constraint>();

	public SymbolicVariableConstraintSolver() {
		super(new Class[] {SymbolicValueConstraint.class}, SymbolicVariable.class, createConstraintSolvers(), new int[] {0});
		this.symbols = new String[]{};
	}
	
	public SymbolicVariableConstraintSolver(String[] symbols, int maxVars) {
		this(symbols,maxVars,false);
	}

	public SymbolicVariableConstraintSolver(String[] symbols, int maxVars, boolean propagateOnVarCreation) {
		super(new Class[] {SymbolicValueConstraint.class}, SymbolicVariable.class, createConstraintSolvers(symbols.length*maxVars, (int)Math.pow(symbols.length*maxVars, 2), propagateOnVarCreation), new int[] {symbols.length});
		this.symbols = symbols;
		thisSolver = this;
		this.setOptions(ConstraintSolver.OPTIONS.AUTO_PROPAGATE);
		if (this.getEnumerateSets()) ((BooleanSatisfiabilitySolver)this.getConstraintSolvers()[0]).setEnumerateModels(true);
		else ((BooleanSatisfiabilitySolver)this.getConstraintSolvers()[0]).setEnumerateModels(false);
	}
	
	private HashSet<Variable> getConstrainedVariables(Constraint[] c) {
		HashSet<Variable> ret = new HashSet<Variable>();
		HashSet<Variable> unconstrainedVars = getUnconstrainedVariables(c);
		for (Variable v : this.getConstraintNetwork().getVariables())
			if (!unconstrainedVars.contains(v)) ret.add(v);
		return ret;
	}

	private HashSet<Variable> getUnconstrainedVariables(Constraint[] c) {
		HashSet<Variable> ret = new HashSet<Variable>();
		for (Variable var : this.getConstraintNetwork().getVariables()) {
			boolean skip  = false;
			Variable[] connectedVars = this.getConstraintNetwork().getNeighboringVariables(var);
			if (connectedVars.length == 0) {
				for (Constraint con : c) {
					if (skip) break;
					for (Variable v : con.getScope()) {
						if (v.equals(var)) {
							skip = true;
							break;
						}
					}
				}
				if (!skip) ret.add(var);
			}
		}
		return ret;
	}

	private boolean isMeaningfulConstraint(HashSet<Variable> constrainedVariables, Constraint con) {
		for (Variable var : con.getScope())
			if (constrainedVariables.contains(var)) return true;
		return false;
	}
	
	public static Variable union(Variable ... vars) {
		SymbolicVariable ret = (SymbolicVariable)thisSolver.createVariable(vars[0].getComponent());
		SymbolicValueConstraint unaryEquals = new SymbolicValueConstraint(Type.VALUEEQUALS);
		boolean[] unaryValue = new boolean[thisSolver.symbols.length];
		for (int i = 0; i < unaryValue.length; i++) unaryValue[i] = false;
		for (int j = 0; j < vars.length; j++) {
			String[] symb = ((SymbolicVariable)vars[j]).getSymbols();
			for (int i = 0; i < symb.length; i++) {
				for (int k = 0; k < thisSolver.symbols.length; k++) {
					if (symb[i].equals(thisSolver.getSymbols()[k])) {
						unaryValue[k] = true;
						break;
					}
				}
			}
		}
		unaryEquals.setValue(unaryValue);
		unaryEquals.setFrom(ret);
		unaryEquals.setTo(ret);
		thisSolver.addConstraint(unaryEquals);
		return ret;
	}
		
	public static Variable intersection(Variable ... vars) {
		SymbolicValueConstraint unaryEquals = new SymbolicValueConstraint(Type.VALUEEQUALS);
		boolean[] unaryValue = new boolean[thisSolver.symbols.length];
		String[] allSymbols = thisSolver.symbols;
		boolean atLeastOneValue = false;
		for (int i = 0; i < allSymbols.length; i++) {
			boolean found = true;
			for (int j = 0; j < vars.length; j++) {
				for (int k = 0; k < ((SymbolicVariable)vars[j]).getSymbols().length; k++) {
					String oneSymbol = ((SymbolicVariable)vars[j]).getSymbols()[k];
					if (oneSymbol.equals(allSymbols[i])) {
						break;
					}
					if (k == ((SymbolicVariable)vars[j]).getSymbols().length-1) found = false;
				}
				if (!found) break;
			}
			if (found) {
				unaryValue[i] = true;
				atLeastOneValue = true;
			}
			else unaryValue[i] = false;
		}
		if (!atLeastOneValue) return null;
		SymbolicVariable ret = (SymbolicVariable)thisSolver.createVariable(vars[0].getComponent());
		unaryEquals.setValue(unaryValue);
		unaryEquals.setFrom(ret);
		unaryEquals.setTo(ret);
		thisSolver.addConstraint(unaryEquals);
		return ret;
	}
	
	public void setSingleValue(boolean singleValue) {
		this.singleValue = singleValue;
	}

	public void setEnumerateSets(boolean enumerateSets) {
		this.enumerateSets = enumerateSets;
		if (enumerateSets) ((BooleanSatisfiabilitySolver)this.getConstraintSolvers()[0]).setEnumerateModels(true);
		else ((BooleanSatisfiabilitySolver)this.getConstraintSolvers()[0]).setEnumerateModels(false);
	}

	public boolean getSingleValue() {
		return singleValue;
	}
	
	public boolean getEnumerateSets() {
		return enumerateSets;
	}

	public String[] getSymbols() { return this.symbols; }
	
	public String getSymbol(int i) { return this.symbols[i]; }
	
	public BooleanVariable getBooleanForSymbol(String symbol) {
		for (int i = 0; i < symbols.length; i++)
			if (symbols[i].equals(symbol)) return (BooleanVariable)this.getConstraintSolvers()[0].getConstraintNetwork().getVariable(i);
		return null;
	}

	private static ConstraintSolver[] createConstraintSolvers(int maxSATVars, int maxSATClauses, boolean propagateOnVarCreation) {
		ConstraintSolver[] ret = new ConstraintSolver[] {new BooleanSatisfiabilitySolver(maxSATVars, maxSATClauses, propagateOnVarCreation)};
		return ret;
	}
	
	private static ConstraintSolver[] createConstraintSolvers() {
		ConstraintSolver[] ret = new ConstraintSolver[] {new BooleanSatisfiabilitySolver(0,0)};
		return ret;
	}
		
	@Override
	public boolean propagate() {
		//Do nothing - propagation is taken care of
		//by the underlying BooleanSatisfiabilitySolver
		return true;
	}
	
	@Override
	public void maskConstraints(Constraint[] c) {
		//Mask the internal constraints (BooleanConstraints) that model unary values for variables
		//that have no neighbors - this makes the SAT solver much more efficient
		HashSet<Variable> unconstrainedVariables = getUnconstrainedVariables(c);
		for (Variable var : unconstrainedVariables) {
			SymbolicVariable sv = (SymbolicVariable)var;
			Constraint[] internalCons = sv.getInternalConstraints();
			ConstraintNetwork.maskConstraints(internalCons);
		}
		logger.fine("Masked internal constraints");
	}
	
	@Override
	public void unmaskConstraints(Constraint[] c) {
		this.getConstraintSolvers()[0].getConstraintNetwork().unmaskConstraints();
		logger.finest("Unmasked internal constraints");
	}
		
}
