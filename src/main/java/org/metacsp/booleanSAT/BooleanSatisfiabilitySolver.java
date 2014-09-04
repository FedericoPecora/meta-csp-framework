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

package org.metacsp.booleanSAT;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Domain;
import org.metacsp.framework.ValueChoiceFunction;
import org.metacsp.framework.Variable;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

/**
 * The {@link BooleanSatisfiabilitySolver} provides a means to reason upon Boolean Satisfiability
 * constraint problems.  It is based on the off the shelf SAT4J solver, available at <a href="http://www.sat4j.org/">www.sat4j.org</a>.
 * The solver's variables are {@link BooleanVariable}s, which can be bound by {@link BooleanConstraint}s.
 * The latter are disjunctive Boolean clauses, thus the collection of all {@link BooleanConstraint}s in
 * a {@link ConstraintNetwork} represents a well-formed-formula (wff) in Conjunctive Normal Form (CNF).
 * Note that {@link BooleanConstraint}s can be instantiated with a factory method from non-CNF formulas
 * (see {@link BooleanConstraint#createBooleanConstraints(BooleanVariable[], String)}.
 * 
 * @author Federico Pecora
 */
public class BooleanSatisfiabilitySolver extends ConstraintSolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 872716109540894219L;

	public static final int MAX_SAT_VARS = 1000000;
	public static final int MAX_SAT_CLAUSES = 500000;
	
	private transient ISolver sat4JSolver;
		
	private Vector<HashMap<BooleanVariable,Boolean>> currentModels = new Vector<HashMap<BooleanVariable, Boolean>>();
	
	//Progressively increasing IDs for BooleanVariables
	protected int BVIDs = 1;
	
	private int maxVars;
	private int maxClauses;
		
	/**
	 * Create a new {@link BooleanSatisfiabilitySolver} that will accept at most <code>MAX_SAT_VARS</code>
	 * {@link BooleanVariable}s and <code>MAX_SAT_CLAUSES</code> {@link BooleanConstraint}s.
	 */
	public BooleanSatisfiabilitySolver() {
		super(new Class[] {BooleanConstraint.class}, BooleanVariable.class);
		this.maxVars = MAX_SAT_VARS;
		this.maxClauses = MAX_SAT_CLAUSES;
		initSat4JSolver();
		this.setOptions(OPTIONS.AUTO_PROPAGATE, OPTIONS.NO_PROP_ON_VAR_CREATION);
	}

	/**
	 * Create a new {@link BooleanSatisfiabilitySolver}.  This solver will accept
	 * at most <code>maxVars</code> {@link BooleanVariable}s and
	 * <code>maxClauses</code> {@link BooleanConstraint}s.
	 * @param maxVars
	 * @param maxClauses
	 */
	public BooleanSatisfiabilitySolver(int maxVars, int maxClauses) {
		super(new Class[] {BooleanConstraint.class}, BooleanVariable.class);
		this.maxVars = maxVars;
		this.maxClauses = maxClauses;
		initSat4JSolver();
		this.setOptions(OPTIONS.AUTO_PROPAGATE, OPTIONS.NO_PROP_ON_VAR_CREATION);
	}

	private void initSat4JSolver() {
		sat4JSolver = SolverFactory.newDefault();
		sat4JSolver.newVar(maxVars);
		sat4JSolver.setExpectedNumberOfClauses(maxClauses);
		sat4JSolver.setDBSimplificationAllowed(false);
	}
	
	private void resetCurrentModels() {
		currentModels = new Vector<HashMap<BooleanVariable,Boolean>>();
		//generateDefaultModels(this.getConstraintNetwork().getVariables());
		logger.finest("Reset current models");
	}
	
	private void updateCurrentModels(int[]... models) {
		currentModels = new Vector<HashMap<BooleanVariable,Boolean>>();
		for (int[] oneModel : models) {
			HashMap<BooleanVariable,Boolean> aModel = new HashMap<BooleanVariable, Boolean>();
			currentModels.add(aModel);
			for (int lit : oneModel) {
				BooleanVariable bv = (BooleanVariable)this.getConstraintNetwork().getVariable(Math.abs(lit));
				if (lit < 0) aModel.put(bv, false);
				else aModel.put(bv, true);
			}
		}
		this.registerValueChoiceFunctions();
		logger.finest("Updated current models");
	}
	
//	private void resetSat4JSolver() {
//		sat4JSolver.reset();
//		
//		constraintsToICons = new HashMap<BooleanConstraint, IConstr>(); 
//		iConsToConstraints = new HashMap<IConstr, BooleanConstraint>();
//		
//		for (Constraint con : this.getConstraintNetwork().getConstraints()) {
//			boolean skip = false;
//			if (tempConstraints != null) {
//				for (Constraint tempCon : tempConstraints) {
//					if (tempCon.equals(con)) {
//						skip = true;
//						break;
//					}
//				}
//			}
//			if (!skip) {
//				BooleanConstraint bc = (BooleanConstraint)con;
//				try {
//					logger.info("readding: " + bc);
//					IConstr clasue = sat4JSolver.addClause(bc.getLiterals());
//					constraintsToICons.put(bc, clasue);
//					iConsToConstraints.put(clasue, bc);
//				}
//				catch (ContradictionException e) { throw new Error("Error resetting SAT4J Solver"); }
//			}
//		}
//		tempConstraints = null;
//	}

	private void updateDomains(int[]... allModels) {
		HashSet<Variable> allVars = new HashSet<Variable>();
		allVars.addAll(Arrays.asList(this.getConstraintNetwork().getVariables()));
		for (int[] oneModel : allModels) {
			for (int i : oneModel) {
				BooleanVariable bv = (BooleanVariable)this.getConstraintNetwork().getVariable(Math.abs(i));
				if (bv != null) {
					if (i < 0) bv.allowFalse();
					else bv.allowTrue();
					if (allVars.contains(bv)) allVars.remove(bv);
				}
			}
		}
		for (Variable var : allVars) {
			((BooleanVariable)var).allowFalse();
			((BooleanVariable)var).allowTrue();
		}
	}
	
	@Override
	public boolean propagate() {
		
		sat4JSolver.reset();
		
		logger.finest("Solving SAT problem...");
		Constraint[] cons = this.getConstraintNetwork().getConstraints();
		
		for (Constraint con : cons) {
			BooleanConstraint bc = (BooleanConstraint)con;
			try { sat4JSolver.addClause(bc.getLiterals()); }
			catch (ContradictionException e) { return false; }
		}
		
		Vector<int[]> allModels = new Vector<int[]>();
		
		try {
			if (!sat4JSolver.isSatisfiable()) return false;
			while (sat4JSolver.isSatisfiable()) {
				int[] oneModel = sat4JSolver.model();
				if (oneModel.length == 0) break;
				allModels.add(oneModel);
				//logger.info("Model: " + Arrays.toString(oneModel));
				int[] negClause = new int[oneModel.length];
				for (int i = 0; i < oneModel.length; i++) {
					negClause[i] = -oneModel[i];
				}
				try { sat4JSolver.addClause(new VecInt(negClause)); }
				catch (ContradictionException e) { break; }				
			}
		}
		catch (TimeoutException e1) { e1.printStackTrace(); }
		
		if (!allModels.isEmpty()) {
			logger.finest("allmodels[0].length: " + allModels.firstElement().length);
			for (Variable var : this.getConstraintNetwork().getVariables()) {
				BooleanVariable bv = (BooleanVariable)var;
				bv.setDomain(new BooleanDomain(bv,false,false));					
			}
			updateDomains(allModels.toArray(new int[allModels.size()][]));
			updateCurrentModels(allModels.toArray(new int[allModels.size()][]));
		}
		else { resetCurrentModels(); }
		
		return true;
	}

	@Override
	protected boolean addConstraintsSub(Constraint[] c) {
		return true;
	}

	@Override
	protected void removeConstraintsSub(Constraint[] c) { /* do nothing */ }

	@Override
	protected BooleanVariable[] createVariablesSub(int num) {
		Vector<BooleanVariable> ret = new Vector<BooleanVariable>();
		for (int i = 0; i < num; i++) {
			ret.add(new BooleanVariable(this,BVIDs++));
		}
		return ret.toArray(new BooleanVariable[ret.size()]);
	}

	@Override
	protected void removeVariablesSub(Variable[] v) { /*do nothing */ }

	@Override
	public void registerValueChoiceFunctions() {
		Domain.removeValueChoiceFunctions(BooleanDomain.class);
		if (currentModels != null && !currentModels.isEmpty()) {
			for (int i = 0; i < currentModels.size(); i++) {
				final int index = i;
				ValueChoiceFunction vcf = new ValueChoiceFunction() {
					@Override
					public Object getValue(Domain dom) {
						if (currentModels.get(index).get(((BooleanVariable)dom.getVariable())) != null)
							return currentModels.get(index).get(((BooleanVariable)dom.getVariable()));
						return new Boolean(true);
					}
				};
				Domain.registerValueChoiceFunction(BooleanDomain.class, vcf, "model"+i);
			}
			logger.finest("Updated value choice functions (there are currently " + currentModels.size() + " models)");
		}
		else {
			ValueChoiceFunction vcf = new ValueChoiceFunction() {
				@Override
				public Object getValue(Domain dom) {
					return new Boolean(true);
				}
			};
			Domain.registerValueChoiceFunction(BooleanDomain.class, vcf, "model0");
			logger.finest("Updated value choice functions (there is currently only the default model)");
		}
		
	}

}
