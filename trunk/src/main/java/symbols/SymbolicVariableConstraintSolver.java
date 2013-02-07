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
package symbols;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.variables.set.SetConstantVariable;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.Solver;
import framework.Constraint;
import framework.ConstraintNetwork;
import framework.ConstraintSolver;
import framework.Variable;

public class SymbolicVariableConstraintSolver extends ConstraintSolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5440978548399829763L;
	/*
	 * Data structures for maintaining
	 * Choco variables
	 */
	private Vector<SetVariable> realVariables = null;
	private Vector<SetVariable> auxVariables = null;
	private Model m = null;
	private Vector<String> allValues = new Vector<String>();
	private Solver s = null;
	
	private SetConstantVariable cEmpty = Choco.emptySet();
	
	//Progressively increasing IDs for state variables
	protected int SVIDs = 0;
	
	private HashMap<SetVariable,SymbolicVariable> chocoToSV = new HashMap<SetVariable,SymbolicVariable>();
	private HashMap<SymbolicVariable,SetVariable> SVToChoco = new HashMap<SymbolicVariable,SetVariable>();
	
	@Override
	protected ConstraintNetwork createConstraintNetwork() {
		return new SymbolicVariableNetwork(this);
	}
	
	public SymbolicVariableConstraintSolver() {
		super(new Class[]{SymbolicValueConstraint.class}, new Class[]{SymbolicVariable.class});
		this.setOptions(OPTIONS.AUTO_PROPAGATE);
	}
	
	private void makeChocoConstraints() {
		Constraint[] constraints = this.theNetwork.getConstraints();
		for (Constraint c : constraints) {
			if (c instanceof SymbolicValueConstraint) {
				SymbolicValueConstraint con = (SymbolicValueConstraint)c;
				SymbolicVariable from = (SymbolicVariable)con.getFrom();
				SymbolicVariable to = (SymbolicVariable)con.getTo();
				if (con.getType().equals(SymbolicValueConstraint.Type.EQUALS)) {
					this.equals(SVToChoco.get(from), SVToChoco.get(to));
				}
				if (con.getType().equals(SymbolicValueConstraint.Type.DIFFERENT)) {
					this.notEquals(SVToChoco.get(from), SVToChoco.get(to));
				}
				if (con.getType().equals(SymbolicValueConstraint.Type.UNARYEQUALS)) {
					this.unaryEquals(SVToChoco.get(from), con.getUnaryValue());
				}
				if (con.getType().equals(SymbolicValueConstraint.Type.UNARYDIFFERENT)) {
					this.unaryNotEquals(SVToChoco.get(from), con.getUnaryValue());
				}
				if (con.getType().equals(SymbolicValueConstraint.Type.FORBIDDENTUPLE)) {
					this.forbiddenTuple(SVToChoco.get(from),SVToChoco.get(to), con.getBinaryValue(0), con.getBinaryValue(1));
				}
			}
		}
	}
	
	@Override
	protected boolean addConstraintSub(Constraint c) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean addConstraintsSub(Constraint[] c) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected Variable[] createVariablesSub(int num) {
		SymbolicVariable[] ret = new SymbolicVariable[num];
		for (int i = 0; i < num; i++) ret[i] = new SymbolicVariable(this, SVIDs++);
		return ret;
	}

	@Override
	public boolean propagate() {
		resetDomains();
		createChocoProblem();
		if (chocoSolve()) {
			writeBackDomains();
			return true;
		}
		return false;
	}

	@Override
	protected void removeConstraintSub(Constraint c) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void removeConstraintsSub(Constraint[] c) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void removeVariableSub(Variable v) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void removeVariablesSub(Variable[] v) {
		// TODO Auto-generated method stub
	}
	
	
	/*
	 * Low-level private methods for dealing with Choco
	 * and maintaining correspondence between real variables
	 * and Choco variables
	 */
	
	private int getMin(int[] arg) {
		int min = Integer.MAX_VALUE;
		for (int i : arg) if (i < min) min = i;
		return min;
	}
	
	//x EQUALS y <--> isIncluded(x,y) AND isIncluded(y,x)
	//m.addConstraint(Choco.isIncluded(realVariables.elementAt(1), realVariables.elementAt(0))) ;
	//m.addConstraint(Choco.isIncluded(realVariables.elementAt(0), realVariables.elementAt(1))) ;
	private void equals(SetVariable i, SetVariable j) {
		m.addConstraint(Choco.isIncluded(j, i));
		m.addConstraint(Choco.isIncluded(i, j));
	}
	
	//x NOTEQUALS y <--> isNotIncluded(x,y) AND isNotIncluded(y,x)
	//m.addConstraint(Choco.isNotIncluded(realVariables.elementAt(1), realVariables.elementAt(0))) ;
	//m.addConstraint(Choco.isNotIncluded(realVariables.elementAt(0), realVariables.elementAt(1))) ;
	private void notEquals(SetVariable i, SetVariable j) {
		m.addConstraint(Choco.isNotIncluded(j, i));
		m.addConstraint(Choco.isNotIncluded(i, j));
	}
	
	//x UNARYEQUALS c <--> y = setVar(c,c) AND eq(x,y)
	//String c = "A";
	//SetVariable y = Choco.makeSetVar("UNARY"+c, allValues.indexOf(c), allValues.indexOf(c));
	//m.addConstraint(Choco.eq(y, realVariables.elementAt(1)));
	private void unaryEquals(SetVariable i, String c) {
		SetVariable y = Choco.makeSetVar("UNARY"+c, allValues.indexOf(c), allValues.indexOf(c));
		m.addConstraint(Choco.eq(y, i));
	}
	
	//x UNARYNOTEQUALS c <--> y = setVar(c,c) AND isNotIncluded(y,x)
	//String c = "D";
	//SetVariable y = Choco.makeSetVar("UNARY"+c, allValues.indexOf(c), allValues.indexOf(c));
	//m.addConstraint(Choco.isNotIncluded(y, realVariables.elementAt(1)));
	private void unaryNotEquals(SetVariable i, String c) {
		SetVariable y = Choco.makeSetVar("UNARY"+c, allValues.indexOf(c), allValues.indexOf(c));
		m.addConstraint(Choco.isNotIncluded(y, i));
	}
	
	private void forbiddenTuple(SetVariable i, SetVariable j, String c_i, String c_j) {
		SetVariable x = Choco.makeSetVar("UNARY"+c_i, allValues.indexOf(c_i), allValues.indexOf(c_i));
		SetVariable y = Choco.makeSetVar("UNARY"+c_j, allValues.indexOf(c_j), allValues.indexOf(c_j));
		m.addConstraint(Choco.isNotIncluded(x, y));
	}
	
	private int getMax(int[] arg) {
		int max = Integer.MIN_VALUE;
		for (int i : arg) if (i > max) max = i;
		return max;
	}
	
	private void createChocoProblem() {
		
		this.chocoToSV = new HashMap<SetVariable, SymbolicVariable>();
		this.SVToChoco = new HashMap<SymbolicVariable, SetVariable>();
		
		this.realVariables = new Vector<SetVariable>();
		this.auxVariables = new Vector<SetVariable>();
		
		Variable[] allVars = this.theNetwork.getVariables();
		String[][] allDomains = new String[allVars.length][];
		for (int i = 0; i < allVars.length; i++) {
			allDomains[i] = ((SymbolicDomain)allVars[i].getDomain()).getSymbols();
		}
		
		m = new CPModel();
		for (String[] oneDomain : allDomains)
			for (String domValue : oneDomain) 
				if (!allValues.contains(domValue))
					allValues.add(domValue);
		
		Collections.sort(allValues);		
		
		// Build the model
		for (int i = 0; i < allDomains.length; i++) {
			String[] oneDomain = allDomains[i];
			int[] indexes = new int[oneDomain.length];
			for (int j = 0; j < oneDomain.length; j++) indexes[j] = allValues.indexOf(oneDomain[j]);
			int min = getMin(indexes);
			int max = getMax(indexes);
			//System.out.println("min = " + min + " max = " + max);
			boolean holes = false;
			if (max-min+1 > indexes.length) holes = true;
			SetVariable domainVar = Choco.makeSetVar("Domain" + i + "Var", min, max);
			m.addConstraint(Choco.neq(cEmpty, domainVar));
			//System.out.println("Made var " + domainVar.getName());
			realVariables.add(domainVar);
			if (holes) {
				java.util.Arrays.sort(indexes);
				for (int k = 0; k < indexes.length-1; k++) {
					if ((indexes[k+1]-indexes[k]) != 1) {
						for (int y = indexes[k]+1; y < indexes[k+1]; y++) {
							SetVariable auxDomainVar = Choco.makeSetVar("AuxDomain" + i + "Var" + y, y, y);
							m.addConstraint(Choco.isNotIncluded(auxDomainVar, domainVar));
							auxVariables.add(auxDomainVar);
						}
					}
				}
			}
			this.chocoToSV.put(domainVar, (SymbolicVariable)allVars[i]);
			this.SVToChoco.put((SymbolicVariable)allVars[i],domainVar);
		}
		
		makeChocoConstraints();
	}
	
	private void writeBackDomains() {
		for (int i = 0; i < realVariables.size(); i++) {
			SetVariable setvar = realVariables.elementAt(i);
			SymbolicVariable sv = this.chocoToSV.get(setvar);
			//System.out.println(chocoToSV);
			//System.out.println("Getting domain of " + sv + " (setvar = " + setvar + ")");
			SymbolicDomain st = (SymbolicDomain)sv.getDomain();
			String[] chocostates = this.getDomain(setvar);
//			System.out.println("Chocostates = " + Arrays.toString(chocostates));
			//System.out.println("Internal state of " + setvar + ": " + Arrays.toString(s.getVar(setvar).getValue()));
			for (String stname : st.getSymbols()) {
				//need to mask state
				if (Arrays.binarySearch(chocostates, stname) < 0) {
					st.setMask(stname, true);
				}
			}
		}
	}
	
	private void resetDomains() {
		for (Variable var : this.theNetwork.getVariables()) {
			SymbolicDomain st = (SymbolicDomain)var.getDomain();
			st.resetMasks();
		}
	}
	
	private boolean chocoSolve() {
		// Build a solver
		s = new CPSolver();
		// Read the model
		s.read(m);
		// Solve the problem
		s.solve();
		return s.existsSolution();
	}
	
	
	private String[] getDomain(SetVariable chocoVar) {
		String[] ret = null;
/*		Vector<String> retVec = new Vector<String>();
		SetVar setvar = s.getVar(chocoVar);
		for (int i = 0; i < allValues.size(); i++)
		{
			SetVariable auxVar = Choco.makeSetVar("AuxVarValue", i, i);
			m.addConstraint(Choco.neq(cEmpty, auxVar));
			System.out.println("Gonna compare " + Arrays.toString(setvar.getValue()) + " and " + auxVar);
			if (setvar.canBeEqualTo(s.getVar(auxVar))) {
				retVec.add(allValues.elementAt(i));
			}
		}
		
		if (retVec.size() > 0) return retVec.toArray(new String[retVec.size()]);
		return null;
*/		
		
		if (s.getVar(chocoVar) == null) {
			//System.out.println("HERE!");
			ret = new String[chocoVar.getUppB()-chocoVar.getLowB()+1];
			int counter = 0;
			for (int j = chocoVar.getLowB(); j <= chocoVar.getUppB(); j++) {
				ret[counter]=allValues.elementAt(j);
				counter++;
			}
		}
		else {
			//System.out.println("THERE!");
			ret = new String[s.getVar(chocoVar).getValue().length];
			int counter = 0;
			for (int j : s.getVar(chocoVar).getValue()) {
				ret[counter] = allValues.elementAt(j);
				counter++;
			}	
		}
		return ret;
	}

}
