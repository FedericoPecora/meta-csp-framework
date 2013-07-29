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

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class is used to represent n-ary constraints in the MetaCSP framework.
 * The scope of a constraint is the set of all {@link Variable}s involved in the constraint.
 * An important subclass is {@link BinaryConstraint}, which provides support for constraints
 * whose scope has size two. 
 * 
 * @author Federico Pecora
 *
 */
public abstract class Constraint implements Cloneable, Serializable {
	
	
	private static final long serialVersionUID = 8278654163054138810L;
	private ArrayList<ConstraintSolver> solversToSkip = new ArrayList<ConstraintSolver>();

	/**
	 * Progressive ID of a constraint
	 */
	public static int numIDs = 0;
	
	protected int id;
	
	protected Variable[] scope;
	
	protected Object annotation;
	
	private boolean autoRemovable = false;
	
	/**
	 * Provide a list of solvers that should not process this constraint.
	 * @param solvers Solvers that should not process this constraint.
	 */
	public void skipSolver(ConstraintSolver ... solvers) {
		solversToSkip = new ArrayList<ConstraintSolver>();
		for (ConstraintSolver solver : solvers) solversToSkip.add(solver);
	}
	
	/**
	 * Assess whether a given solver is allowed to process this constraint.
	 * @param cs The {@link ConstraintSolver} to check.
	 * @return <code>true</code> iff the given solver will process this constraint.
	 */
	public boolean isSkippableSolver(ConstraintSolver cs) {
		return solversToSkip.contains(cs);
	}
	
	/**
	 * Get this {@link Constraint}'s annotation (can be any {@link Object} and used in any way).
	 * @return This {@link Constraint}'s annotation.
	 */
	public Object getAnnotation() { return annotation; }

	/**
	 * Set this {@link Constraint}'s annotation (can be any {@link Object} and used in any way).
	 * @param o This {@link Constraint}'s annotation.
	 */
	public void setAnnotation(Object o) { annotation = o; }
	
	/**
	 * Returns the ID of this {@link Constraint}.
	 * @return The ID of this {@link Constraint}.
	 */
	public int getID() {return this.id;}
	
	/**
	 * Every {@link Constraint} should implement a toString method (used by the
	 * {@link ConstraintNetwork} rendering methods).
	 * @return the {@link String} representation of this {@link Constraint}. 
	 */
	public abstract String toString();
	
	/**
	 * Every {@link Constraint} should implement this method - its value is
	 * what is drawn by the {@link ConstraintNetwork} rendering methods.
	 * @return a {@link String} representation of the constraint to be used for
	 * {@link ConstraintNetwork} rendering.
	 */
	public abstract String getEdgeLabel();

	/**
	 * Returns the scope of this {@link Constraint}.
	 * @return the scope of this {@link Constraint} (an array of size 2 if this is a
	 * {@link BinaryConstraint}).
	 */
	public Variable[] getScope() {
		return scope;
	}
	
	/**
	 * Set the scope of this {@link Constraint} to a given array of {@link Variable}s.
	 * @param scope The desired new scope of this {@link Constraint}. 
	 */
	public void setScope(Variable[] scope) {
		this.scope = scope;
	}
	
	@Override
	public abstract Object clone();
	
	/**
	 * Method for assessing the "equivalence" between two constraints.  This is not used
	 * for lookups nor comparisons (hence, it is not the equals method).  
	 * @param c The {@link Constraint} to compare against.
	 * @return <code>true</code> iff the two constraints are considered to be equivalent.
	 */
	public abstract boolean isEquivalent(Constraint c);
	
	/**
	 * Get the description of this {@link Constraint}'s type.
	 * @return The description of this {@link Constraint}'s type.
	 */
	public String getDescription() {
		return this.getClass().getSimpleName();
	}

	public boolean isAutoRemovable() {
		return autoRemovable;
	}

	public void setAutoRemovable(boolean autoRemovable) {
		this.autoRemovable = autoRemovable;
	}

}
