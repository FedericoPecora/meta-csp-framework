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
package framework.multi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import meta.simplePlanner.SimpleDomain.markings;

import sandbox.spatial.rectangleAlgebra2.RectangleConstraintSolver2;
import throwables.ConstraintNotFound;
import framework.Constraint;
import framework.ConstraintNetwork;
import framework.ConstraintSolver;
import framework.Variable;

/**
 * This class extends the {@link ConstraintSolver} class providing functionality
 * to deal with {@link MultiVariable}s and {@link MultiConstraint}s.  It should be extended
 * to create new {@link MultiConstraintSolver}s.  In doing so, the designer should define
 * how variables are created (methods createVariableSub() and createVariablesSub()), how to 
 * create internal constraint solvers underlying this {@link MultiConstraintSolver} (method
 * createConstraintSolvers()) and how propagation should occur (method propagate()).  Note that
 * if propagation occurs by means of the underlying constraint solvers, the propagate() method can
 * left empty (return <code>true</code>).
 *  
 * @author Federico Pecora
 *
 */
public abstract class MultiConstraintSolver extends ConstraintSolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3328919153619683198L;

	/**
	 * These are options used by the {@link ConstraintSolver} class to determine
	 * whether inconsistencies should be allowed in the constraitn networks of the
	 * underlying constraint solvers.
	 *  
	 * @author Federico Pecora
	 */
	public static enum OPTIONS {ALLOW_INCONSISTENCIES,FORCE_CONSISTENCY};
	
	private boolean allowInconsistencies = false;
	
	protected ConstraintSolver[] constraintSolvers;
	private HashMap<Constraint,Constraint> newConstraintMapping = new HashMap<Constraint,Constraint>();
	
		
	/**
	 * The constructor of a extending class must call this constructor.
	 * @param constraintTypes
	 * @param internalSolvers
	 */
	protected MultiConstraintSolver(Class<?>[] constraintTypes, Class<?>[] variableTypes, ConstraintSolver[] internalSolvers) {
		super(constraintTypes, variableTypes);
		this.constraintSolvers = internalSolvers;
	}

	/**
	 *  Method to set the options for this {@link MultiConstraintSolver} (see {@link OPTIONS}).
	 */
	public void setOptions(OPTIONS ...ops) {
		for (OPTIONS op : ops)
			if (op.equals(OPTIONS.ALLOW_INCONSISTENCIES)) allowInconsistencies = true;
			else if (op.equals(OPTIONS.FORCE_CONSISTENCY)) allowInconsistencies = false;
	}
	
	/**
	 * Method to get options of this {@link MultiConstraintSolver} (see {@link OPTIONS}).  
	 */
	public boolean getOption(OPTIONS op) {
		if (op.equals(OPTIONS.ALLOW_INCONSISTENCIES)) return allowInconsistencies;
		else if (op.equals(OPTIONS.FORCE_CONSISTENCY)) return !allowInconsistencies;
		return false;
	}
	

	@Override
	protected boolean addConstraintSub(Constraint c) {
		for (int i = 0; i < this.constraintTypes.length; i++) {
			
			if (c.getClass().equals(this.constraintTypes[i])) {
				Variable[] scope = c.getScope();
				Variable[] internalScopeArray = new Variable[scope.length];
				
				for(int j = 0; j < scope.length; ++j) {
					MultiVariable mv = (MultiVariable) scope[j];
					internalScopeArray[j] = mv.getInternalVariables()[i];
				}
								
				Constraint newConstraint = (Constraint)c.clone();
				newConstraint.setScope(internalScopeArray);
				if (!this.constraintSolvers[i].addConstraint(newConstraint)) {
					/*if (!allowInconsistencies)*/ return false;
				}
				newConstraintMapping.put(c, newConstraint);
				break;
			}
		}
		return true;
	}
	
	@Override
	protected boolean addConstraintsSub(Constraint[] c) {
		
		Vector<Vector<Constraint>> newToAdd = new Vector<Vector<Constraint>>();
		Vector<Vector<Constraint>> added = new Vector<Vector<Constraint>>();

		for(int i = 0; i < this.constraintTypes.length; ++i) {
			newToAdd.add(new Vector<Constraint>());
			added.add(new Vector<Constraint>());			
		}
		
		for (Constraint constr : c) {
			for (int i = 0; i < this.constraintTypes.length; i++) {				
				Vector<Variable> internalScope = new Vector<Variable>(); 
				if (this.constraintTypes[i].isInstance(constr)) {
					Variable[] scope = constr.getScope();
					for (Variable v : scope) {
						MultiVariable mv = (MultiVariable)v;
						internalScope.add(mv.getInternalVariables()[i]);
					}
					Variable[] internalScopeArray = internalScope.toArray(new Variable[internalScope.size()]);
					Constraint newConstraint = (Constraint)constr.clone();
					newConstraint.setScope(internalScopeArray);
					newToAdd.elementAt(i).add(newConstraint);
					newConstraintMapping.put(constr, newConstraint);
					break;
				}
			}
		}
		
		boolean retract = false;
		for (int i = 0; i < this.constraintTypes.length && !retract; i++) {
			Vector<Constraint> newCons = newToAdd.elementAt(i);
			//	if there is something to insert...
			if (!newCons.isEmpty()) {
				Constraint[] newConsArray = newCons.toArray(new Constraint[newCons.size()]);
				if (!this.constraintSolvers[i].addConstraints(newConsArray)) {
					/* if (!allowInconsistencies) */ retract = true;
				}
				else {
					added.set(i, newCons);
				}
			}
		}
		if (retract) {
			for (int i = 0; i < added.size(); i++) {
				Vector<Constraint> toRetract = added.elementAt(i);
				if (!toRetract.isEmpty()) {
					this.constraintSolvers[i].removeConstraints(toRetract.toArray(new Constraint[toRetract.size()]));
					for (Constraint oldConstr : newConstraintMapping.keySet()) {
						for (Constraint newConstr : toRetract) {
							if (newConstraintMapping.get(oldConstr).equals(newConstr)) newConstraintMapping.remove(oldConstr);
							break;
						}
					}
				}
			}
			return false;
		}
		return true;
	}
	
	@Override
	protected final void removeVariableSub(Variable v) {
		if (v instanceof MultiVariable) {
			MultiVariable mv = (MultiVariable)v;
			Variable[] intVars = mv.getInternalVariables();
			HashMap<ConstraintSolver,Vector<Variable>> solvers = new HashMap<ConstraintSolver,Vector<Variable>>();
			for (Variable intVar : intVars) {
				if (solvers.get(intVar.getConstraintSolver()) == null) solvers.put(intVar.getConstraintSolver(), new Vector<Variable>());
				solvers.get(intVar.getConstraintSolver()).add(intVar);
			}
			for (ConstraintSolver cs : solvers.keySet()) {
				cs.removeVariables(solvers.get(cs).toArray(new Variable[solvers.get(cs).size()]));
			}
		}
	}

	@Override
	protected final void removeVariablesSub(Variable[] v) {
		HashMap<ConstraintSolver,Vector<Variable>> solvers = new HashMap<ConstraintSolver,Vector<Variable>>();
		for (Variable oneVar : v) {
			if (oneVar instanceof MultiVariable) {
				MultiVariable mv = (MultiVariable)oneVar;
				Variable[] intVars = mv.getInternalVariables();
				for (Variable intVar : intVars) {
					if (solvers.get(intVar.getConstraintSolver()) == null) solvers.put(intVar.getConstraintSolver(), new Vector<Variable>());
					solvers.get(intVar.getConstraintSolver()).add(intVar);
				}
			}		
		}
		for (ConstraintSolver cs : solvers.keySet()) {
			cs.removeVariables(solvers.get(cs).toArray(new Variable[solvers.get(cs).size()]));
		}
	}
	
	@Override
	protected abstract ConstraintNetwork createConstraintNetwork();
	
	@Override
	protected abstract Variable[] createVariablesSub(int num);

	@Override
	public abstract boolean propagate();

	@Override
	protected void removeConstraintSub(Constraint c) {
		Constraint newConstraint = newConstraintMapping.get(c);
		if (newConstraint == null) throw new ConstraintNotFound(c);
		for (int i = 0; i < this.constraintTypes.length; i++) {
			if (newConstraint.getClass().equals(this.constraintTypes[i])) {
				this.constraintSolvers[i].removeConstraint(newConstraint);
				break;
			}
		}
		newConstraintMapping.remove(c);
	}

	@Override
	protected void removeConstraintsSub(Constraint[] c) {
		Vector<Vector<Constraint>> newToRemove = new Vector<Vector<Constraint>>();
		//for (Class<?> cl : this.constraintTypes) {
		for (int i = 0; i < this.constraintTypes.length; i++) {
			newToRemove.add(new Vector<Constraint>());
		}
		for (Constraint constr : c) {
			Constraint newConstraint = newConstraintMapping.get(constr);
			if (newConstraint == null) throw new ConstraintNotFound(constr);
			for (int i = 0; i < this.constraintTypes.length; i++) {
				if (newConstraint.getClass().equals(this.constraintTypes[i])) {
					newToRemove.elementAt(i).add(newConstraint);
					break;
				}
			}
		}
		for (int i = 0; i < newToRemove.size(); i++) {
			Vector<Constraint> toRemove = newToRemove.elementAt(i);
			if (!toRemove.isEmpty()) {
				this.constraintSolvers[i].removeConstraints(toRemove.toArray(new Constraint[toRemove.size()]));
			}
		}
		for (Constraint constr : c) {
			newConstraintMapping.remove(constr);
		}
	}

	/**
	 * Get the {@link ConstraintSolver}s underlying this {@link MultiConstraintSolver}.
	 * @return The {@link ConstraintSolver}s underlying this {@link MultiConstraintSolver}.
	 */
	public ConstraintSolver[] getConstraintSolvers() {
		return this.constraintSolvers;
	}
	
	public void setConstraintSolver( int i, ConstraintSolver cSolver ) {
		this.constraintSolvers[i] = cSolver;
	}

	@Override
	public String getDescription() {
		String spacer = "";
		for (int i = 0; i < nesting; i++) spacer += spacing;
		String ret = spacer + "[" + this.getClass().getSimpleName() + " vars: [";
		for (int i = 0; i < this.variableTypes.length; i++) {
			ret += this.variableTypes[i].getSimpleName();
			if (i != this.variableTypes.length-1) ret += ",";
		}
		ret += "] constraints: [";
		for (int i = 0; i < this.constraintTypes.length; i++) {
			ret += this.constraintTypes[i].getSimpleName();
			if (i != this.constraintTypes.length-1) ret += ",";
		}
		ret += "]";
		nesting++;
		for (ConstraintSolver cs : this.getConstraintSolvers()) ret += "\n" + cs.getDescription();
		nesting--;
		return ret + "]";
	}
	
	public void failurePruning(int failure_time){

		
//		for(Constraint c: this.getConstraints()){
//			this.removeConstraint(c);
//		}
//		for(Variable v: this.getVariables()){
//			
//			this.removeVariable(v);
//		}
		
		this.removeConstraints(this.getConstraints());
		this.removeVariables(this.getVariables());
		
		
		
//		this.deplenish();
//		for(ConstraintSolver cs: this.constraintSolvers){
//			cs.deplenish();
//		}
		
//		for(Constraint c: this.theNetwork.getConstraints()){
//			this.theNetwork.removeConstraint(c);
//		}
//		for(Variable v: this.theNetwork.getVariables()){
//			this.theNetwork.removeVariable(v);
//		}
		
//		this.theNetwork= createConstraintNetwork();
		for(String k: this.components.keySet()){
			ArrayList<Variable> list= this.components.get(k);
			list.clear();
		}
	}
			

}
