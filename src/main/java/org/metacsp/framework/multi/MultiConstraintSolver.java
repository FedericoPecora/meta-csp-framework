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
package org.metacsp.framework.multi;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.logging.Logger;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.throwables.ConstraintNotFound;
import org.metacsp.utility.logging.MetaCSPLogging;

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
	protected int[] ingredients;
	private HashMap<Constraint,Constraint> newConstraintMapping = new HashMap<Constraint,Constraint>();
	
	public static ConstraintSolver getConstraintSolver(ConstraintSolver cs, Class<?> constraintSolverClass) {
		if (cs.getClass().equals(constraintSolverClass)) return cs;
		if (cs instanceof MultiConstraintSolver) {
			MultiConstraintSolver mcs = (MultiConstraintSolver)cs;
			for (int i = 0; i < mcs.getConstraintSolvers().length; i++) {
				ConstraintSolver ret = getConstraintSolver(mcs.getConstraintSolvers()[i], constraintSolverClass);
				if (ret != null) return ret;
			}
		}
		return null;
	}

	/**
	 * The constructor of a extending class must call this constructor.
	 * @param constraintTypes
	 * @param internalSolvers
	 */
	protected MultiConstraintSolver(Class<?>[] constraintTypes, Class<?> variableType, ConstraintSolver[] internalSolvers, int[] ingredients) {
		super(constraintTypes, variableType);
		this.constraintSolvers = internalSolvers;
		this.ingredients = ingredients;
	}

	/**
	 *  Method to set the options for this {@link MultiConstraintSolver} (see {@link OPTIONS}).
	 *  @param ops Options to set (see {@link OPTIONS}).
	 */
	public void setOptions(OPTIONS ...ops) {
		for (OPTIONS op : ops)
			if (op.equals(OPTIONS.ALLOW_INCONSISTENCIES)) allowInconsistencies = true;
			else if (op.equals(OPTIONS.FORCE_CONSISTENCY)) allowInconsistencies = false;
	}
	
	/**
	 * Method to get options of this {@link MultiConstraintSolver} (see {@link OPTIONS}).
	 * @param op The option that should be checked
	 * @return <code>true</code> iff the given option was set  
	 */
	public boolean getOption(OPTIONS op) {
		if (op.equals(OPTIONS.ALLOW_INCONSISTENCIES)) return allowInconsistencies;
		else if (op.equals(OPTIONS.FORCE_CONSISTENCY)) return !allowInconsistencies;
		return false;
	}
	
	/**
	 * Set the number of internal variables of different types that are to be created when
	 * calling the method createVariables().
	 * @param ingredients The number of internal variables, oen number for each type, that are to 
	 * be created.
	 */
	public void setIngredients(int[] ingredients) {
		this.ingredients = ingredients;
	}
		
	@Override
	protected final boolean addConstraintsSub(Constraint[] c) {
		HashMap<ConstraintSolver, ArrayList<Constraint>> sortedCons = new HashMap<ConstraintSolver, ArrayList<Constraint>>();
		for (Constraint con : c) {
			if (con instanceof MultiConstraint) {
				MultiConstraint mc = (MultiConstraint)con;
				MultiVariable mv = (MultiVariable)mc.getScope()[0];
				for (ConstraintSolver cs : mv.getInternalConstraintSolvers()) {
					if (mc.propagateImmediately()) {
						if (!sortedCons.containsKey(cs)) {
							sortedCons.put(cs, new ArrayList<Constraint>());
						}
						Constraint[] internalCons = mc.getInternalConstraints();
						if (internalCons != null) {
							for (Constraint ic : internalCons) {
								if (!ic.isSkippableSolver(cs)) sortedCons.get(cs).add(ic);
							}
						}
					}
				}
			}
		}

		HashMap<ConstraintSolver, ArrayList<Constraint>> sortedConsRetract = new HashMap<ConstraintSolver, ArrayList<Constraint>>();
		for (ConstraintSolver cs : sortedCons.keySet()) {
			//if caller is noprop do not prop
			if (!this.skipPropagation) {
				if (cs.addConstraints(sortedCons.get(cs).toArray(new Constraint[sortedCons.get(cs).size()]))) {
					logger.finest("Added sub-constraints " + sortedCons.get(cs));
					sortedConsRetract.put(cs, sortedCons.get(cs));
				}
				else {
					for (ConstraintSolver cs1 : sortedConsRetract.keySet()) {
						logger.finest("Removing internal constraints (" + this.getClass().getSimpleName() + ") " + sortedConsRetract.get(cs1));
						cs1.removeConstraints(sortedConsRetract.get(cs1).toArray(new Constraint[sortedConsRetract.get(cs1).size()]));
					}
					logger.finest("Failed to add sub-constraints " + Arrays.toString(c));
					return false;
				}				
			}
			else {
				cs.addConstraintsNoPropagation(sortedCons.get(cs).toArray(new Constraint[sortedCons.get(cs).size()]));
				logger.finest("Added sub-constraints " + sortedCons.get(cs) + " (but DELAYED propagation)");
			}
		}

		if (!instantiateLiftedConstraints(c)) {
			for (ConstraintSolver cs1 : sortedConsRetract.keySet()) 
				cs1.removeConstraints(sortedConsRetract.get(cs1).toArray(new Constraint[sortedConsRetract.get(cs1).size()]));
			logger.finest("Failed to instantiate lifted constraints " + Arrays.toString(c));
			return false;
		}
		return true;
	
	}

	private boolean instantiateLiftedConstraints(Constraint[] c) {
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
					ArrayList<Constraint> toRemoveFromNewConstraintMapping = new ArrayList<Constraint>();
					for (Constraint oldConstr : newConstraintMapping.keySet()) {
						for (Constraint newConstr : toRetract) {
							if (newConstraintMapping.get(oldConstr).equals(newConstr)) {
								//newConstraintMapping.remove(oldConstr);
								toRemoveFromNewConstraintMapping.add(oldConstr);
								break;
							}
						}
					}
					for (Constraint toRemoveFromMapping : toRemoveFromNewConstraintMapping)
						newConstraintMapping.remove(toRemoveFromMapping);
				}
			}
			return false;
		}
		return true;
		
	}
	
//	@Override
//	protected final boolean addConstraintsSub(Constraint[] c) {
//		
//		Vector<Vector<Constraint>> newToAdd = new Vector<Vector<Constraint>>();
//		Vector<Vector<Constraint>> added = new Vector<Vector<Constraint>>();
//
//		for(int i = 0; i < this.constraintTypes.length; ++i) {
//			newToAdd.add(new Vector<Constraint>());
//			added.add(new Vector<Constraint>());			
//		}
//		
//		for (Constraint constr : c) {
//			for (int i = 0; i < this.constraintTypes.length; i++) {				
//				Vector<Variable> internalScope = new Vector<Variable>(); 
//				if (this.constraintTypes[i].isInstance(constr)) {
//					Variable[] scope = constr.getScope();
//					for (Variable v : scope) {
//						MultiVariable mv = (MultiVariable)v;
//						internalScope.add(mv.getInternalVariables()[i]);
//					}
//					Variable[] internalScopeArray = internalScope.toArray(new Variable[internalScope.size()]);
//					Constraint newConstraint = (Constraint)constr.clone();
//					newConstraint.setScope(internalScopeArray);
//					newToAdd.elementAt(i).add(newConstraint);
//					newConstraintMapping.put(constr, newConstraint);
//					break;
//				}
//			}
//		}
//		
//		boolean retract = false;
//		for (int i = 0; i < this.constraintTypes.length && !retract; i++) {
//			Vector<Constraint> newCons = newToAdd.elementAt(i);
//			//	if there is something to insert...
//			if (!newCons.isEmpty()) {
//				Constraint[] newConsArray = newCons.toArray(new Constraint[newCons.size()]);
//				if (!this.constraintSolvers[i].addConstraints(newConsArray)) {
//					/* if (!allowInconsistencies) */ retract = true;
//				}
//				else {
//					added.set(i, newCons);
//				}
//			}
//		}
//		if (retract) {
//			for (int i = 0; i < added.size(); i++) {
//				Vector<Constraint> toRetract = added.elementAt(i);
//				if (!toRetract.isEmpty()) {
//					this.constraintSolvers[i].removeConstraints(toRetract.toArray(new Constraint[toRetract.size()]));
//					for (Constraint oldConstr : newConstraintMapping.keySet()) {
//						for (Constraint newConstr : toRetract) {
//							if (newConstraintMapping.get(oldConstr).equals(newConstr)) newConstraintMapping.remove(oldConstr);
//							break;
//						}
//					}
//				}
//			}
//			return false;
//		}
//		return true;
//	}
	

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
			logger.finest("Removing " + solvers.get(cs).size() + " internal variables (" + cs.getClass().getSimpleName() + ")");
			cs.removeVariables(solvers.get(cs).toArray(new Variable[solvers.get(cs).size()]));
		}
	}
		
	private Variable[][] createInternalVariables(int[] ingredients, int num) {
		Vector<Vector<Variable>> ret = new Vector<Vector<Variable>>();
		for (int k = 0; k < this.getConstraintSolvers().length; k++) {
			Variable[] oneType = this.getConstraintSolvers()[k].createVariables(ingredients[k]*num);
			logger.finest("Created " + ingredients[k]*num + " internal variables for " + this.getConstraintSolvers()[k].getClass().getSimpleName());
			for (int i = 0; i < num; i++) {
				Vector<Variable> oneVar = null;
				if (ret.size() > i) oneVar = ret.elementAt(i);
				else {
					oneVar = new Vector<Variable>();
					ret.add(oneVar);
				}
				for (int j = i*ingredients[k]; j < (i+1)*ingredients[k]; j++) {
					oneVar.add(oneType[j]);
				}
			}
		}
		Variable[][] retArray = new Variable[ret.size()][];
		for (int i = 0; i < num; i++) retArray[i] = ret.elementAt(i).toArray(new Variable[ret.elementAt(i).size()]);
		return retArray;
	}
	
	/**
	 * This method creates {@code num} {@link MultiVariable}s. The creation of internal variables is
	 * taken care of automatically, given a specification of the number of internal variables to create
	 * for each internal solver.
	 * @param ingredients The number of internal variables to create for each internal solver.   
	 * @param num The number of {@link MultiVariable}s for which internal variables are to be created.
	 * @param component The label (component) the created variables should be associated with.
	 * @return The {@link Variable}s for {@code num} {@link MultiVariable}s.
	 */
	protected Variable[] createVariablesSub(int[] ingredients, int num, String component) {
		Variable[][] internalVars = createInternalVariables(ingredients, num);
		Variable[] ret = (Variable[]) java.lang.reflect.Array.newInstance(this.variableType, num);
		HashMap<ConstraintSolver,Vector<Constraint>> solvers2Constraints = new HashMap<ConstraintSolver, Vector<Constraint>>();
		for (int i = 0; i < num; i++) {
			try {
				ret[i] = (Variable) this.variableType.getConstructor(new Class[] {ConstraintSolver.class, int.class, ConstraintSolver[].class, Variable[].class}).newInstance(new Object[] {this, this.IDs++, this.constraintSolvers, internalVars[i]});
				if (component != null) {
					ret[i].getConstraintSolver().setComponent(component, ret[i]);
					logger.finest("Set component of " + ret[i] + " to " + component);
					for (Variable internalVar : internalVars[i]) {
						internalVar.getConstraintSolver().setComponent(component, internalVar);
						logger.finest("Set component of " + internalVar + " to " + component);
					}
				}
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (ret[i] instanceof MultiVariable) {
				Constraint[] internalCons = ((MultiVariable)ret[i]).getInternalConstraints();
				if (internalCons != null) {
					logger.finest("Adding internal constraints for " + ret[i]);
					for (Constraint con : internalCons) {
						if (!solvers2Constraints.containsKey(con.getScope()[0].getConstraintSolver()))
							solvers2Constraints.put(con.getScope()[0].getConstraintSolver(), new Vector<Constraint>());
						solvers2Constraints.get(con.getScope()[0].getConstraintSolver()).add(con);
					}
				}
			}
		}
		for (Entry<ConstraintSolver, Vector<Constraint>> es : solvers2Constraints.entrySet()) {
			//if we are called from addconstraintsnoprop, call addconsnoprop on the internals, otherwise call normal addconstraints
			if (!es.getKey().addConstraintsNoPropagation(es.getValue().toArray(new Constraint[es.getValue().size()])))
				throw new Error("Malformed internal constraints: " + es.getValue());
			else logger.finest("Added " + es.getValue().size() + " internal constraints to " + es.getKey().getClass().getSimpleName() + " (but DELAYED propagation)");
		}
		return ret;
	}

	@Override
	protected final Variable[] createVariablesSub(int num) {
		return createVariablesSub(ingredients, num, null);
	}

	@Override
	protected final Variable[] createVariablesSub(int num, String component) {
		return createVariablesSub(ingredients, num, component);
	}
	
	@Override
	public abstract boolean propagate();
	
	@Override
	protected final void removeConstraintsSub(Constraint[] c) {
		HashMap<ConstraintSolver,ArrayList<Constraint>> internalCons = new HashMap<ConstraintSolver, ArrayList<Constraint>>();
		//gather internal constraints
		for (Constraint con : c) {
			/**/
			if (con instanceof MultiConstraint) {
				MultiConstraint mc = (MultiConstraint)con;
				MultiVariable mv = (MultiVariable)mc.getScope()[0];
				for (ConstraintSolver cs : mv.getInternalConstraintSolvers()) {
					if (!internalCons.containsKey(cs)) internalCons.put(cs,new ArrayList<Constraint>());
					if (mc.getInternalConstraints() != null) for (Constraint c1 : mc.getInternalConstraints()) internalCons.get(cs).add(c1);
				}
			}
			/**/
		}
		
		//get rid of internal constraints
		for (ConstraintSolver cs : internalCons.keySet()) {
			cs.removeConstraints(internalCons.get(cs).toArray(new Constraint[internalCons.get(cs).size()]));
		}

		uninstantiateLiftedConstraints(c);
	}

	
	private void uninstantiateLiftedConstraints(Constraint[] c) {
		Vector<Vector<Constraint>> newToRemove = new Vector<Vector<Constraint>>();
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
		ret += this.variableType.getSimpleName();
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
	
	@Override
	public void registerValueChoiceFunctions() {
		// TODO Auto-generated method stub	
	}

}
