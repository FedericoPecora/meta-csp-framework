package org.metacsp.multi.symbols;

import java.util.Arrays;
import java.util.Vector;

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
		// TODO Auto-generated method stub
		return null;
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
			System.out.println("ADDING: " + equalsCon);
			this.solver.addConstraint(equalsCon);
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
