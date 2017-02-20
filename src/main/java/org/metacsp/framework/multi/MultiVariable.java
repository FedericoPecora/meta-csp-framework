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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;
import org.metacsp.multi.allenInterval.AllenInterval;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.allenInterval.AllenIntervalNetworkSolver;
import org.metacsp.time.TimePoint;
import org.metacsp.utility.UI.VariableHierarchyFrame;

import cern.colt.Arrays;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.util.TreeUtils;

/**
 * A multi-variable is a variable "implemented" by several underlying lower-level {@link Variable}s.
 * This class is used by {@link MultiConstraintSolver}s along with {@link MultiConstraint}s to
 * maintain and propagate multiple CSPs defined by aggregations of variables and constraints.
 * An example is the {@link AllenInterval}, an aggregation of two {@link TimePoint}s, which can be
 * bound by {@link MultiConstraint}s of the type {@link AllenIntervalConstraint} and reasoned upon
 * by an {@link AllenIntervalNetworkSolver}.
 * 
 * Defining a {@link MultiVariable} is reduced to creating the internal lower-level {@link Variable}s
 * (method  createVariablesSub(int num)). Also, a {@link MultiVariable} may have "implementing constraints".
 * This is the case, e.g., in the {@link AllenInterval}, which, in addition to the two {@link TimePoint}s,
 * also possesses a {@link AllenIntervalConstraint} of type Duration.  For this reason, the designer of a
 * {@link MultiVariable} must also define the createInternalConstraints() method.
 *  
 * @author Federico Pecora
 *
 */
public abstract class MultiVariable extends Variable {

	public static HashMap<FieldOfObject,Object> backupForSerialization = new HashMap<FieldOfObject,Object>();
	private class FieldOfObject {
		private Field field;
		private int ID;
		private FieldOfObject(Field field) {
			this.ID = getID();
			this.field = field;
		}
		public boolean equals(Object o) {
			FieldOfObject foo = (FieldOfObject)o;
			return (foo.ID == this.ID && foo.field.getName().equals(this.field.getName()));
		}
		public int hashCode() {
			return this.toString().hashCode();
		}
		public String toString() {
			return "FieldOfObject <" + ID + "," + field.getName() + ">";
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 5070818185640197097L;
	//protected transient ConstraintSolver[] internalSolvers;
	protected ConstraintSolver[] internalSolvers;
	protected Variable[] variables;
	protected Constraint[] constraints;


	/**
	 * The constructor of an extensions to {@link MultiVariable} must call this constructor.
	 * @param cs The {@link ConstraintSolver} of the {@link MultiVariable}.
	 * @param id The {@link MultiVariable}'s ID.
	 * @param internalSolvers The internal solvers of this {@link MultiVariable} (to which the
	 * @param internalVars The internal {@link Variable}s implementing this {@link MultiVariable}.
	 */
	protected MultiVariable(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers, Variable[] internalVars) {
		super(cs, id);
		this.internalSolvers = internalSolvers;
		this.variables = internalVars;
		logger.finest("Set internal variables " + (this.variables == null ? this.variables : Arrays.toString(this.variables)));		
		this.constraints = this.createInternalConstraints(this.variables);
		logger.finest("Created internal constraints " + ((this.constraints == null) ? this.constraints : Arrays.toString(this.constraints)));
	}

	/**
	 * This method must be implemented to define the internal lower-level {@link Constraint}s of this
	 * {@link MultiVariable}.
	 * @param variables The internal variables among which the {@link Constraint}s should be defined.
	 * @return The internal lower-level {@link Constraint}s of this
	 * {@link MultiVariable}.
	 */
	protected abstract Constraint[] createInternalConstraints(Variable[] variables);
	
	/**
	 * Get the internal {@link Variable}s of this {@link MultiVariable}. 
	 * @return The internal {@link Variable}s of this {@link MultiVariable}.
	 */
	public Variable[] getInternalVariables() {
		return this.variables;
	}

	/**
	 * Get the internal {@link Constraint}s of this {@link MultiVariable}. 
	 * @return The internal {@link Constraint}s of this {@link MultiVariable}.
	 */
	public Constraint[] getInternalConstraints() {
		return this.constraints;
	}
	
	/**
	 * Get the internal {@link ConstraintSolver}s of this {@link MultiVariable}. 
	 * @return The internal {@link ConstraintSolver}s of this {@link MultiVariable}.
	 */
	public ConstraintSolver[] getInternalConstraintSolvers() {
		return internalSolvers;
	}
	
	@Override
	public MultiDomain getDomain() {
		Vector<Domain> doms = new Vector<Domain>();
		for (Variable v : this.variables) doms.add(v.getDomain());
		MultiDomain ret = new MultiDomain(this, doms.toArray(new Domain[doms.size()]));
		return ret;
	}
	
	/**
	 * Get the hierarchy of {@link Variable}s underlying this {@link MultiVariable}.
	 * @return The hierarchy of {@link Variable}s underlying this {@link MultiVariable}.
	 */
	public DelegateTree<Variable,String> getVariableHierarchy() {
		DelegateTree<Variable,String> ret = new DelegateTree<Variable, String>();
		ret.setRoot(this);
		Variable[] myVars = this.getInternalVariables();
		for (int i = 0; i < myVars.length; i++) {
			String edgeLabel = i + " (" + this.getConstraintSolver().hashCode() + ")";
			if (myVars[i] instanceof MultiVariable) {
				DelegateTree<Variable,String> subTree = ((MultiVariable)myVars[i]).getVariableHierarchy();
				TreeUtils.addSubTree(ret, subTree, this, edgeLabel);
			}
			else ret.addChild(edgeLabel, this, myVars[i]);
		}
		return ret;
	}
	
	/**
	 * Draw the {@link Variable} hierarchy underlying this {@link MultiVariable}.
	 * @param tree The {@link Variable} hierarchy to draw.
	 */
	public static void drawVariableHierarchy(DelegateTree<Variable,String> tree) {
		new VariableHierarchyFrame(tree, "Variable Hierarchy");
	}
	
	/**
	 * Get the {@link Variable}s of a given type in this {@link MultiVariable}'s variable hierarchy.
	 * @param cl The type of {@link Variable}s to fetch.
	 * @return The {@link Variable}s of the given type in this {@link MultiVariable}'s variable hierarchy.
	 */
	public Variable[] getVariablesFromVariableHierarchy(Class<?> cl) {
		ArrayList<Variable> ret = new ArrayList<Variable>();
		for (Variable v : this.getVariableHierarchy().getVertices()) {
			if (v.getClass().equals(cl)) ret.add(v);
		}
		if (ret.size() == 0) throw new Error(this.getClass().getSimpleName() + " does not have a " + cl.getSimpleName() + " in its hierarchy");
		return ret.toArray(new Variable[ret.size()]);
	}
	
	@Override
	public String getDescription() {
		String ret = this.getClass().getSimpleName() + ": [vars: [";
		Vector<Variable> vars = new Vector<Variable>();
		for (Variable v : this.getInternalVariables()) {
			if (this.getInternalConstraints() != null) {
				for (Constraint con : this.getInternalConstraints()) {
					for (Variable scopeVar : con.getScope()) {
						if (!vars.contains(scopeVar)) vars.add(scopeVar);
					}
					if (!vars.contains(v)) vars.add(v);
				}
			}
			else {
				vars.add(v);
			}
		}
		for (int i = 0; i < vars.size(); i++) {
			ret += vars.elementAt(i).getDescription();
			if (i != vars.size()-1) ret += ",";
		}
		ret += "] constraints: [";
		if (this.getInternalConstraints() != null) {
			for (int i = 0; i < this.getInternalConstraints().length; i++) {
				ret += this.getInternalConstraints()[i].getDescription();
				if (i != this.getInternalConstraints().length-1) ret += ",";
			}
		}
		return ret + "]]";
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		for (Field f : MultiVariable.class.getDeclaredFields()) {
			if (Modifier.isTransient(f.getModifiers())) {
				try { backupForSerialization.put(new FieldOfObject(f), f.get(this)); }
				catch (IllegalArgumentException e) { e.printStackTrace(); }
				catch (IllegalAccessException e) { e.printStackTrace(); }
			}
		}
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		for (Field f : MultiVariable.class.getDeclaredFields()) {
			if (Modifier.isTransient(f.getModifiers())) {
				Object foo = backupForSerialization.get(new FieldOfObject(f));
				try { f.set(this, foo); }
				catch (IllegalArgumentException e) { e.printStackTrace(); }
				catch (IllegalAccessException e) { e.printStackTrace(); }
			}
		}
	}
	
	@Override
	public void setComponent(String component) {
		super.setComponent(component);
		DelegateTree<Variable,String> varTree = this.getVariableHierarchy();
		for (Variable var : varTree.getSuccessors(varTree.getRoot())) var.setComponent(component);
	}

}
