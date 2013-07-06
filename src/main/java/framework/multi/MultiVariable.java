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

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.logging.Logger;

import org.apache.commons.collections15.iterators.EntrySetMapIterator;

import multi.allenInterval.AllenInterval;
import multi.allenInterval.AllenIntervalConstraint;
import multi.allenInterval.AllenIntervalNetworkSolver;
import time.TimePoint;
import utility.logging.MetaCSPLogging;
import framework.Constraint;
import framework.ConstraintSolver;
import framework.Domain;
import framework.Variable;

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

	/**
	 * 
	 */
	private static final long serialVersionUID = 5070818185640197097L;
	protected transient ConstraintSolver[] internalSolvers;
	protected Variable[] variables;
	protected Constraint[] constraints;

//	/**
//	 * The constructor of an extensions to {@link MultiVariable} must call this constructor.
//	 * @param cs The {@link ConstraintSolver} of the {@link MultiVariable}.
//	 * @param id The {@link MultiVariable}'s ID.
//	 * @param internalSolvers The internal solvers of this {@link MultiVariable} (to which the
//	 * underlying constraints are added).
//	 */
//	protected MultiVariable(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers) {
//		super(cs, id);
//		this.internalSolvers = internalSolvers;
//		this.variables = this.createInternalVariables();
//		logger.finest("Created internal variables " + this.variables);		
//		this.constraints = this.createInternalConstraints(this.variables);
//		logger.finest("Created internal constraints " + this.constraints);
//	}

	/**
	 * The constructor of an extensions to {@link MultiVariable} must call this constructor.
	 * @param cs The {@link ConstraintSolver} of the {@link MultiVariable}.
	 * @param id The {@link MultiVariable}'s ID.
	 * @param internalSolvers The internal solvers of this {@link MultiVariable} (to which the
	 * @param internalVars The internal {@link Variable}s implementing this {@link MultiVariable}.
	 * @param internalCons The internal {@link Constraint}s implementing this {@link MultiVariable}.
	 */
	protected MultiVariable(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers, Variable[] internalVars) {
		super(cs, id);
		this.internalSolvers = internalSolvers;
		this.variables = internalVars;
		logger.finest("Set internal variables " + this.variables);		
		this.constraints = this.createInternalConstraints(this.variables);
		logger.finest("Created internal constraints " + this.constraints);
	}

	
//	/**
//	 * This method must be implemented to define the internal lower-level {@link Variable}s of this
//	 * {@link MultiVariable}.  
//	 * @return The internal lower-level {@link Variable}s of this
//	 * {@link MultiVariable}.
//	 */
//	protected abstract Variable[] createInternalVariables();

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


}
