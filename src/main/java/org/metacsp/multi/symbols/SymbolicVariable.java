package org.metacsp.multi.symbols;

import java.util.Arrays;
import java.util.Vector;

import org.metacsp.booleanSAT.BooleanConstraint;
import org.metacsp.booleanSAT.BooleanDomain;
import org.metacsp.booleanSAT.BooleanVariable;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiVariable;

public class SymbolicVariable extends MultiVariable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2975768214982114482L;
	
	private String[] nonSolverDomain = new String[0];

	public SymbolicVariable(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers, Variable[] internalVars) {
		super(cs, id, internalSolvers, internalVars);
	}

	@Override
	public int compareTo(Variable arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected Constraint[] createInternalConstraints(Variable[] variables) {
		// INSERT CONSTRAINT SAYING THAT VAR MUST HAVE AT EXACTLY ONE SYMBOL!
		if (variables == null || variables.length == 0) return null;
		if (((SymbolicVariableConstraintSolver)this.solver).getSymbols() == null) return null;
		if (((SymbolicVariableConstraintSolver)this.solver).getSymbols().length == 0) return null;
		Vector<BooleanConstraint> cons = new Vector<BooleanConstraint>();
		for (int i = 0; i < variables.length-1; i++) {
			BooleanConstraint c = new BooleanConstraint(new BooleanVariable[] {(BooleanVariable)variables[i], (BooleanVariable)variables[i+1]}, new boolean[] {false, false});
			cons.add(c);
		}
		
		// === FROM R296 ===
		String wff = "(";
		for (int i = 0; i < variables.length; i++) {
			if (i != variables.length-1) wff += "w" + (i+1) + " v (";
			else wff += "w" + (i+1);
		}
		for (int i = 0; i < variables.length; i++) wff += ")";
		logger.finest("Generated internal WFF for variable " + this.getID() + ": " + wff);
		BooleanVariable[] bvs = new BooleanVariable[variables.length];
		for (int i = 0; i < bvs.length; i++) bvs[i] = (BooleanVariable)variables[i];
		for (BooleanConstraint c : BooleanConstraint.createBooleanConstraints(bvs, wff)) {
			cons.add(c);
		}
		// ====================
		
		return cons.toArray(new BooleanConstraint[cons.size()]);
	}

	@Override
	public void setDomain(Domain d) {
		// TODO Auto-generated method stub
	}

	public void setDomain(String... symbols) {
		String[] solverSymbols = ((SymbolicVariableConstraintSolver)this.solver).getSymbols();
		
		boolean[] solverSymbolsToMakeTrue = new boolean[solverSymbols.length];
		for (int i = 0; i < solverSymbolsToMakeTrue.length; i++) solverSymbolsToMakeTrue[i] = false;
		Vector<String> nonSolverDomainVec = new Vector<String>();
		boolean usesSolverSymbols = false;
		
		for (String symbol : symbols) {
			boolean found = false;
			for (int i = 0; i < solverSymbols.length; i++) {
				if (solverSymbols[i].equals(symbol)) {
					found = true;
					solverSymbolsToMakeTrue[i] = true;
					usesSolverSymbols = true;
					break;
				}
			}
			if (!found) nonSolverDomainVec.add(symbol);
		}
		
		if (usesSolverSymbols) {
			SymbolicValueConstraint equalsCon = new SymbolicValueConstraint(SymbolicValueConstraint.Type.UNARYEQUALS);
			equalsCon.setUnaryValue(solverSymbolsToMakeTrue);
			equalsCon.setFrom(this);
			equalsCon.setTo(this);
			equalsCon.setAutoRemovable(true);
			//this.solver.addConstraint(equalsCon);
			this.solver.addConstraintNoPropagation(equalsCon);
		}
		
		nonSolverDomain = nonSolverDomainVec.toArray(new String[nonSolverDomainVec.size()]);
	}

	@Override
	public String toString() {
		return Arrays.toString(this.getSymbols());
//		String ret = this.getClass().getSimpleName() + " " + this.getID() + "[";
//		boolean foundOne = false;
//		for (int i = 0; i < this.getInternalVariables().length; i++) {
//			BooleanVariable bv = (BooleanVariable)this.getInternalVariables()[i];
//			BooleanDomain bd = (BooleanDomain)bv.getDomain();
//			if (bd.canBeTrue()) {
//				if (foundOne) ret += ",";
//				ret += ((SymbolicVariableConstraintSolver2)this.solver).getSymbol(i);
//				foundOne = true;
//			}
//		}
//		
//		for (int i = 0; i < nonSolverDomain.length; i++) {
//			if (foundOne) ret += ",";
//			ret += nonSolverDomain[i];
//			foundOne = true;
//		}
//
//		ret += "]";
//		return ret;
	}
	
	public String[] getSymbols() {
		Vector<String> ret = new Vector<String>();
		for (int i = 0; i < this.getInternalVariables().length; i++) {
			BooleanVariable bv = (BooleanVariable)this.getInternalVariables()[i];
			BooleanDomain bd = (BooleanDomain)bv.getDomain();
			if (bd.canBeTrue()) {
				ret.add(((SymbolicVariableConstraintSolver)this.solver).getSymbol(i));
			}
		}
		for (int i = 0; i < nonSolverDomain.length; i++) {
			ret.add(nonSolverDomain[i]);
		}
		return ret.toArray(new String[ret.size()]);
	}

}
