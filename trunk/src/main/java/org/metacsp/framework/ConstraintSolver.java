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
package org.metacsp.framework;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.logging.Logger;

import org.metacsp.framework.multi.MultiConstraint;
import org.metacsp.framework.multi.MultiConstraintSolver;
import org.metacsp.throwables.ConstraintNotFound;
import org.metacsp.throwables.IllegalVariableRemoval;
import org.metacsp.throwables.VariableNotFound;
import org.metacsp.utility.logging.MetaCSPLogging;

/**
 * This class provides common infrastructure and functionality for
 * all constraint solvers. All constraint solvers should implement either
 * this class or the {@link MultiConstraint} class.  The latter is in fact an extension of this class,
 * as it implements the, addConstraintsSub() and
 * removeConstraintsSub() methods.
 * 
 * This abstract class also maintains a {@link ConstraintNetwork}, which is
 * represented internally as a graph.  Any specific solver must implement this abstract class
 * to provide methods to create/remove variables, add/remove constraints, and perform
 * propagation.
 * 
 * @author Federico Pecora
 */
public abstract class ConstraintSolver implements Serializable {
	
	public static int numcalls = 0;
	protected Class<?>[] constraintTypes = {};
	protected Class<?> variableType = null;
	protected static int nesting = 0;
	protected static String spacing = "  ";
	private static final long serialVersionUID = 7526472295622776147L;
	
	protected int IDs = 0;
	
	protected String name;
	
	/**
	 * Access to the underlying constraint network.
	 */
	protected ConstraintNetwork theNetwork;
	
	/**
	 * General class options. Options currently available:
	 * 
	 * <ul>
	 * <li> {@code AUTO_PROPAGATE}: if set, the constraint solver will call (user implemented) propagate method
	 * automatically.  Set {@code MANUAL_PROPAGATE} if propagations must be dealt with in a more sophisticated way (e.g., incremental propagators).
	 * Default is {@code false}.
	 * </li>
	 * <li> {@code MANUAL_PROPAGATE}: if set, the constraint solver will NOT call (user implemented) propagate method
	 * automatically.  Set {@code AUTO_PROPAGATE} if propagation should happen always in the same way.
	 * Default is {@code false}.
	 * </li>
	 * <li> {@code NO_PROP_ON_VAR_CREATION}: if set, the constraint solver will NOT call (user implemented) propagate method
	 * automatically when variables are created.  This is useful with {@code AUTO_PROPAGATE}, so that propagation is automatic, but only when constraints are added.
	 * Default is {@code false} (propagation occurs when variables are created).
	 * </li>
	 * <li> {@code DOMAINS_AUTO_INSTANTIATED}: if set, the constraint solver will not check whether domains
	 * are instantiated before propagation. Default is {@code false}.
	 * </li>
	 * </ul>
	 */
	public static enum OPTIONS {AUTO_PROPAGATE,NO_PROP_ON_VAR_CREATION,MANUAL_PROPAGATE,DOMAINS_AUTO_INSTANTIATED,DOMAINS_MANUALLY_INSTANTIATED};

	//internal options
	protected boolean autoprop = false;
	protected boolean noPropOnVarCreation = false;
	private boolean domainsAutoInstantiated = false;
	protected boolean skipPropagation = false;
	
	//have domains been instantiated? if not, propagation will be delayed...
	private boolean domainsInstantiated = false;
	
	protected HashMap<String,ArrayList<Variable>> components = new HashMap<String,ArrayList<Variable>>();

	protected transient Logger logger = MetaCSPLogging.getLogger(this.getClass());
	
	public void setName(String name) { this.name = name; }
	
	/**
	 * Default constructor for this class.  Calls to subclass constructors will
	 * call this by default.
	 */
	protected ConstraintSolver(Class<?>[] constraintTypes, Class<?> variableType) {
		this.theNetwork = new ConstraintNetwork(this);
		this.constraintTypes = constraintTypes;
		this.variableType = variableType;
		this.registerValueChoiceFunctions();
	}
	
	/**
	 * Set options for this {@link ConstraintSolver}.
	 * @param ops Options to set (see {@link OPTIONS}).
	 */
	public void setOptions(OPTIONS ...ops) {
		for (OPTIONS op : ops)
			if (op.equals(OPTIONS.AUTO_PROPAGATE)) autoprop = true;
			else if (op.equals(OPTIONS.NO_PROP_ON_VAR_CREATION)) noPropOnVarCreation = true;
			else if (op.equals(OPTIONS.MANUAL_PROPAGATE)) autoprop = false;
		    else if (op.equals(OPTIONS.DOMAINS_AUTO_INSTANTIATED)) domainsAutoInstantiated = true;
			else if (op.equals(OPTIONS.DOMAINS_MANUALLY_INSTANTIATED)) domainsAutoInstantiated = false;
	}

	/**
	 * Get option value for this {@link ConstraintSolver}.
	 * @param op The option to get (see {@link OPTIONS}).
	 */
	public boolean getOption(OPTIONS op) {
		if (op.equals(OPTIONS.AUTO_PROPAGATE)) return autoprop;
		else if (op.equals(OPTIONS.NO_PROP_ON_VAR_CREATION)) return noPropOnVarCreation;
		else if (op.equals(OPTIONS.MANUAL_PROPAGATE)) return !autoprop;
		else if (op.equals(OPTIONS.DOMAINS_AUTO_INSTANTIATED)) return domainsAutoInstantiated;
		else if (op.equals(OPTIONS.DOMAINS_MANUALLY_INSTANTIATED)) return !domainsAutoInstantiated;
		return false;
	}
	
	/**
	 * Assess whether a {@link Constraint} is compatible with this {@link ConstraintSolver}.  This is used
	 * internally to ignore processing of incompatible constraints by {@link ConstraintSolver}s that are aggregated into
	 * {@link MultiConstraintSolver}s.
	 * @param c The {@link Constraint} to check.
	 * @return <code>true</code> iff the given {@link Constraint} is compatible with this {@link ConstraintSolver}.
	 */
	public boolean isCompatible(Constraint c) {
		for (Class<?> conType : constraintTypes) if (conType.isInstance(c)) return true;
		return false;
	}
		
	/**
	 * Propagate the constraint network.
	 * @return A boolean value indicating the results of the propagation.
	 */
	public abstract boolean propagate();

	/**
	 * Add a constraint between {@link Variable}s.
	 * @param c The constraint to add.
	 * @return <code>true</code> iff the constraint was added successfully.
	 */
	public final boolean addConstraint(Constraint c) {
		 return this.addConstraints(c);
	}
	
	/**
	 * Add a constraint between {@link Variable}s, without propagation.
	 * @param c The constraint to add.
	 * @return <code>true</code>.
	 */
	public final boolean addConstraintNoPropagation(Constraint c) {
		this.skipPropagation = true;
		this.addConstraints(c);
		this.skipPropagation = false;
		return true;
	}

	
	/**
	 * Add a batch of constraints between {@link Variable}s.  This method is
	 * NOT implemented so as to perform only one propagation, rather it adds all constraints
	 * sequentially, and returns the first constraint that fails, {@code null} if no constraint
	 * fails. 
	 * @param c The constraints to add.
	 * @return The first constraint that fails, {@code null} if no constraint
	 * fails. 
	 */
	public final Constraint addConstraintsDebug(Constraint... c) {
		ArrayList<Constraint> addedSoFar = new ArrayList<Constraint>(c.length);
		for (Constraint con : c) {
			if (!this.addConstraint(con)) {
				this.removeConstraints(addedSoFar.toArray(new Constraint[addedSoFar.size()]));
				return con;
			}
			else { addedSoFar.add(con); }
		}
		return null;
	}

	/**
	 * Add a batch of constraints between {@link Variable}s, but do not propagate. This method always returns {@code true},
	 * hence if the constraint(s) to add are inconsistent, this will result in propagation failure the next time
	 * a constraint is added and propagation occurs.
	 * @param c The constraints to add.
	 * @return {@code true}
	 */
	public final boolean addConstraintsNoPropagation(Constraint... c) {
		this.skipPropagation = true;
		this.addConstraints(c);
		this.skipPropagation = false;
		return true;
	}
	/**
	 * Add a batch of constraints between {@link Variable}s.  This method is
	 * implemented so as to perform only one propagation, thus being more convenient than
	 * performing {@code c.length} invocations of {@link #addConstraint(Constraint c)}.
	 * In addition, all constraints are either accepted or rejected together.
	 * @param c The constraints to add.
	 * @return A boolean value indicating the success of adding the constraints.
	 */
	public final boolean addConstraints(Constraint... c) {
		if (c == null || c.length == 0){ 
			return true;
		}
		ArrayList<Constraint> incomp = new ArrayList<Constraint>(c.length);
		for (Constraint con : c) {
			if (isCompatible(con) && !con.isSkippableSolver(this)) { }
			else 
				incomp.add(con);
		}
		ArrayList<Constraint> toAdd = new ArrayList<Constraint>(c.length);

		for (Constraint con : c) if (!incomp.contains(con)) toAdd.add(con);
		
		//if there is no constraint left to add, just return successfully
		if (toAdd.size() == 0) return true;
		
		Constraint[] toAddArray = toAdd.toArray(new Constraint[toAdd.size()]);

		//call solver-specific procedures for adding constraints, then propagate
		if (addConstraintsSub(toAddArray)) {
			//NOTE: must add new cons before attempting propagation, because some solvers call
			//constraint network methods in their implementation of propagate()... 
			for (Constraint con : toAddArray) this.theNetwork.addConstraint(con);
			if (!skipPropagation && autoprop && checkDomainsInstantiated()) { 
				if (this.propagate()) {
					logger.finest("Added and propagated constraints " + Arrays.toString(toAddArray));
					return true;
				}
				//... and then remove new cons if propagation not successful...
				this.removeConstraints(toAddArray);
//				for (Constraint con : toAddArray) {
//					this.theNetwork.removeConstraint(con);
//				}
				logger.finest("Failed to add constraints " + Arrays.toString(toAddArray));
			}
			else {
				logger.finest("Added constraints " + Arrays.toString(toAddArray) + " BUT DELAYED PROPAGATION (autoprop = " + autoprop + ")");
				return true;
			}
		}
		//... and finally re-propagate if not successful to put everything back the way it was
		if (!skipPropagation && autoprop && checkDomainsInstantiated()) this.propagate();
		return false;
	}	

	/**
	 * This method must be implemented by the developer of the specific {@link ConstraintSolver}
	 * class.  It should implement all operations necessary to add multiple constraints, and should return
	 * <code>true</code> upon success, <code>false</code> otherwise. 
	 * @param c  The constraints to add.
	 * @return <code>true</code> iff the constraints were added to the {@link ConstraintNetwork}. 
	 */
	protected abstract boolean addConstraintsSub(Constraint[] c);
		
	/**
	 * Retract a constraint between {@link Variable}s.
	 * @param c The constraint to retract.
	 */
	public final void removeConstraint(Constraint c) throws ConstraintNotFound {
		if (c != null) this.removeConstraints(new Constraint[] {c});
	}
	
	/**
	 * Retract a batch of constraints between {@link Variable}s.  This method should
	 * be implemented so as to perform only one propagation, thus being more convenient than
	 * performing {@code c.length} invocations of {@link #removeConstraint(Constraint c)}.
	 * In addition, all constraints should be either accepted or rejected together.
	 * @param c The constraints to add.
	 */
	public final void removeConstraints(Constraint[] c) throws ConstraintNotFound {
		if (c != null && c.length != 0) {
			Vector<Constraint> incomp = new Vector<Constraint>();
			for (Constraint con : c) {
				if (isCompatible(con) && !con.isSkippableSolver(this)) {
					if (!this.theNetwork.containsConstraint(con)) {
						logger.info("Gonna fail - the constraint type is " + con.getClass().getSimpleName());
						throw new ConstraintNotFound(con);
					}
				}
				else incomp.add(con);
			}
			
			//now filter out the incompatible constraints (we don't wanna fail, just let them pass silently)
			Vector<Constraint> toRemove = new Vector<Constraint>();
			for (Constraint con : c) if (!incomp.contains(con)) toRemove.add(con);
			Constraint[] toRemoveArray = toRemove.toArray(new Constraint[toRemove.size()]);
			
			removeConstraintsSub(toRemoveArray);
			for (Constraint con : toRemove) this.theNetwork.removeConstraint(con);
			if (!skipPropagation && autoprop && checkDomainsInstantiated()) this.propagate();
			logger.finest("Removed constraints " + toRemove);
		}
	}
	
	/**
	 * This method must be implemented by the developer of the specific {@link ConstraintSolver}
	 * class.  Should implement all operations necessary to remove a batch of constraints. 
	 * @param c  The constraints to remove. 
	 */
	protected abstract void removeConstraintsSub(Constraint[] c);

	/**
	 * Create a new {@link Variable} for this {@link ConstraintSolver}, and
	 * assign it to the given component label.
	 * @param component The component label to which assign the new variable.
	 * @return A new {@link Variable}.
	 */
	public final Variable createVariable(String component) {
		return createVariables(1, component)[0];
	}
	
	/**
	 * Factory method for creating a new {@link Variable} for this {@link ConstraintSolver}.
	 * @return A new {@link Variable}.
	 */
	public final Variable createVariable() {
		return createVariables(1)[0];
	}
	
	/**
	 * Used to set the component of a variable.  Components are "tags" that are used, e.g., in
	 * timeline plotting.
	 * @param component The component to set.
	 * @param vars The variables that should be tagged under this component.
	 */
	public void setComponent(String component, Variable ... vars) {
		if (!components.containsKey(component)) components.put(component, new ArrayList<Variable>());
		for (Variable var : vars) components.get(component).add(var);
	}

	/**
	 * Create a batch of new {@link Variable}s for this {@link ConstraintSolver}.
	 * @param num The number of variables to create.
	 * @return A batch of new {@link Variable}s.
	 */
	public final Variable[] createVariables(int num) {
		return this.createVariables(num, null);
	}

	/**
	 * Create a batch of new {@link Variable}s for this {@link ConstraintSolver}.
	 * @param num The number of variables to create.
	 * @param component The component tag to associate to these new variables.
	 * @return A batch of new {@link Variable}s.
	 */
	public final Variable[] createVariables(int num, String component) {
		Variable[] ret = createVariablesSub(num, component);
		if (ret == null) return null;
		//need to add all to network so if sth goes wrong I can delete all of them concurrently
		for (Variable v : ret) this.theNetwork.addVariable(v);
		if (!skipPropagation && autoprop && checkDomainsInstantiated() && !noPropOnVarCreation) this.propagate();
		logger.finest("Created variables " + Arrays.toString(ret));
		return ret;
	}

	private boolean checkDomainsInstantiated() {
		if (domainsInstantiated) return true;
		if (this.theNetwork.checkDomainsInstantiated() == null) {
			domainsInstantiated = true;
			return true;
		}
		return false;
	}

	/**
	 * This method must be implemented by the developer of the specific {@link ConstraintSolver}
	 * class.  It should implement all operations necessary to create a batch of variable for the specific
	 * type of {@link ConstraintSolver}. 
	 * @return  A batch of new {@link Variable} for this {@link ConstraintSolver}.
	 */
	protected abstract Variable[] createVariablesSub(int num);

	/**
	 * This method must be implemented by the developer of the specific {@link ConstraintSolver}
	 * class.  It should implement all operations necessary to create a batch of variable for the specific
	 * type of {@link ConstraintSolver}. 
	 * @param component The component label to associate to the new {@link Variable}s.
	 * @param num The number of {@link Variable}s to create.
	 * @return A batch of new {@link Variable} for this {@link ConstraintSolver}.
	 */
	protected Variable[] createVariablesSub(int num, String component) {
		Variable[] ret = createVariablesSub(num);
		if (component != null) {
			logger.finest("Set component of " + Arrays.toString(ret) + " to " + component);
			this.setComponent(component, ret);
		}
		return ret;
	}

	/**
	 * Remove a {@link Variable} from this {@link ConstraintSolver}.
	 * @param v The {@link Variable} to remove.
	 */
	public final void removeVariable(Variable v) throws VariableNotFound, IllegalVariableRemoval {
		this.removeVariables(new Variable[] {v});
	} 
		
	
	private Constraint removeDummyConstraint(DummyConstraint c) {
		DummyVariable dv = c.getDummyVariable();
		Constraint toReturn = null;
		for (Entry<Constraint,DummyVariable> e : this.getConstraintNetwork().hyperEdges.entrySet()) {
			if (e.getValue().equals(dv)) {
				toReturn = e.getKey();
				break;
			}
		}
		
//		for (Constraint c1 : this.getConstraintNetwork().getIncidentEdges(dv)) this.getConstraintNetwork().removeConstraint(c1);

		return toReturn;
	}
	
	/**
	 * Remove a batch of {@link Variable}s from this {@link ConstraintSolver}.
	 * @param v The batch of {@link Variable}s to remove.
	 */
	public final void removeVariables(Variable[] v) throws VariableNotFound, IllegalVariableRemoval {
		
		HashSet<Constraint> incidentRevised = new HashSet<Constraint>();
		
		for (Variable var : v) {
			if (!this.theNetwork.containsVariable(var) ) throw new VariableNotFound(var);
			Constraint[] incident = this.theNetwork.getIncidentEdges(var);
			for (Constraint con : incident) {
				if ((!con.isAutoRemovable() && !(con instanceof DummyConstraint))) 
					throw new IllegalVariableRemoval(var, this.theNetwork.getIncidentEdges(var));
				else if (con instanceof DummyConstraint) {
					Constraint toRemove = this.removeDummyConstraint((DummyConstraint)con);
					if (toRemove != null) incidentRevised.add(toRemove);
				}
				else incidentRevised.add(con);
			}
		}
		
		this.removeConstraints(incidentRevised.toArray(new Constraint[incidentRevised.size()]));

		removeVariablesSub(v);
		for (Variable var : v) {
			this.theNetwork.removeVariable(var);
		}
		for (ArrayList<Variable> vec : components.values()) {
			vec.removeAll(Arrays.asList(v));
		}
		if (!skipPropagation && autoprop && checkDomainsInstantiated()) this.propagate();
		logger.finest("Removed variables " + Arrays.toString(v));
	}

	/**
	 * This method must be implemented by the developer of the specific {@link ConstraintSolver}
	 * class.  It should implement all operations necessary to remove a batch of variables for
	 * the specific type of {@link ConstraintSolver}. 
	 * @param v The {@link Variable}s to remove.
	 */
	protected abstract void removeVariablesSub(Variable[] v);
	
	/**
	 * Get the {@link ConstraintNetwork} of this {@link ConstraintSolver}.
	 * @return the {@link ConstraintNetwork} of this {@link ConstraintSolver}.
	 */
	public ConstraintNetwork getConstraintNetwork() { return theNetwork; }
	
	/**
	 * Get a {@link Variable} given its ID. 
	 * @param id The ID of the {@link Variable} to get.
	 * @return The {@link Variable} whose ID is id.
	 */
	public Variable getVariable(int id) {
		return this.theNetwork.getVariable(id);
	}
	
	/**
	 * Get the ID of a {@link Variable}. 
	 * @param var The {@link Variable} of which to get the ID.
	 * @return The ID of the given variable.
	 */
	public int getID(Variable var) {
		Variable[] vars = this.getVariables();
		for (int i = 0; i < vars.length; i++) {
			if (vars[i].equals(var)) return i;
		}
		return -1;
	}
	
	/**
	 * Get all the {@link Variable}s contained in this {@link ConstraintSolver}'s {@link ConstraintNetwork}.
	 * @return all the {@link Variable}s contained in this {@link ConstraintSolver}'s {@link ConstraintNetwork}.
	 */
	public Variable[] getVariables() {
		return this.theNetwork.getVariables();
	}

	/**
	 * Get all the {@link Variable}s contained in this {@link ConstraintSolver}'s {@link ConstraintNetwork}.
	 * @return all the {@link Variable}s contained in this {@link ConstraintSolver}'s {@link ConstraintNetwork}.
	 */
	public Variable[] getVariables(String component) {
		ArrayList<Variable> ret = this.components.get(component);
		if (ret == null) return new Variable[0];
		return ret.toArray(new Variable[ret.size()]);
	}

	/**
	 * Get all the {@link Constraint}s contained in this {@link ConstraintSolver}'s {@link ConstraintNetwork}.
	 * @return all the {@link Constraint}s contained in this {@link ConstraintSolver}'s {@link ConstraintNetwork}.
	 */
	public Constraint[] getConstraints() {
		return this.theNetwork.getConstraints();
	}
	
	/**
	 * Get all {@link Constraint}s contained in this {@link ConstraintSolver}'s {@link ConstraintNetwork}
	 * with given source and destination {@link Variable}s.
	 * @param from The source {@link Variable} of the {@link Constraint}s to be obtained.
	 * @param to The destination {@link Variable} of the {@link Constraint}s to be obtained.
	 * @return The {@link Constraint}s contained in this {@link ConstraintSolver}'s {@link ConstraintNetwork}
	 * with given source and destination {@link Variable}s.
	 */
	public Constraint[] getConstraints(Variable from, Variable to) {
		Vector<Constraint> ret = new Vector<Constraint>();
		for (Constraint con : this.getConstraints()) {
			if (con.getScope().length == 2) {
				if (con.getScope()[0].equals(from) && con.getScope()[1].equals(to)) ret.add(con);
			}
		}
		return ret.toArray(new Constraint[ret.size()]);
	}
	
	/**
	 * Get the component of a given {@link Variable}.  A component is a {@link String} labeling of {@link Variable}s.
	 * @param v The {@link Variable} of which to get the component.
	 * @return A {@link String} representing the component of the given {@link Variable}.
	 */
	public String getComponent(Variable v) {
		for (String s : components.keySet()) {
			if (components.get(s).contains(v)) return s;
		}
		return null;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " (" + ((this.name == null) ? "" : this.name) + "): "+ Arrays.asList(this.getVariables());
	}
	
	/**
	 * Gets a description of this {@link MultiConstraintSolver} stating which variable and constraint types it supports.  
	 * @return A description of this {@link MultiConstraintSolver} stating which variable and constraint types it supports.
	 */
	public String getDescription() {
		String spacer = "";
		for (int i = 0; i < nesting; i++) spacer += spacing;
		String ret = spacer + "[" + this.getClass().getSimpleName() + " vars: [";
		ret += variableType.getSimpleName();
		ret += "] constraints: [";
		for (Class<?> c : this.constraintTypes) ret += c.getSimpleName();
		return ret + "]]";
	}
	
	/**
	 * Deplenish the network associated to the {@link ConstraintSolver} (remove all constraints and variables).  
	 */
	public void deplenish() {
		this.removeConstraintsSub(this.getConstraints());
		this.removeVariables(this.getVariables());
	}

	/**
	 * Get a {@link HashMap} of all component tags with associated variables. 
	 * @return A {@link HashMap} whose entries are component tags and the associated variables.
	 */
	public HashMap<String, ArrayList<Variable>> getComponents() {
		return components;
	}

	/**
	 * Set the component tags of all variables. 
	 * @param components A {@link HashMap} whose entries are the component tags and the associated variables.
	 */
	public void setComponents(HashMap<String, ArrayList<Variable>> components) {
		this.components = components;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		logger = MetaCSPLogging.getLogger(this.getClass());
	}
	
	/**
	 * Return if the network of this {@link ConstraintSolver} contains a {@link Variable}.  
	 * @param v The {@link Variable} variable to check.
	 * @return <code>true</code> iff the variable is present in the network.
	 */
	public boolean containsVariable(Variable v) {
		return this.theNetwork.containsVariable(v);
	}
	
	public abstract void registerValueChoiceFunctions();
	
	public void setConstraintNetwork(ConstraintNetwork newCS) { this.theNetwork = newCS; } 
	
}
