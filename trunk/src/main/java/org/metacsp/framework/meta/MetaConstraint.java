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
package org.metacsp.framework.meta;

import java.util.Arrays;
import java.util.logging.Logger;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.VariableOrderingH;
import org.metacsp.utility.logging.MetaCSPLogging;

/**
 * A {@link MetaConstraint} is a generalization of the concept of constraint in classical CSPs.  Like variables in a metaCSP,
 * constraints are often implicit - e.g., a resource in scheduling, whose principal feature is a capacity; or an operator in planning,
 * whose principal features are expressed as preconditions and effects.  All {@link MetaConstraint}s have in common a number of
 * features:
 * <ul>
 * <li> They represent a set of requirements which must be upheld in the particular application domain (e.g., no over-consumption of
 * resources in scheduling). </li>
 * <li> They subsume one or more concrete constraints that are relevant for the ground solvers - e.g., n-ary
 * nogoods in resource scheduling.</li>
 * <li> They can be used to synthesize values for the {@link MetaVariable}s in the meta-CSP - e.g., ordering constraints
 * for the activities in a scheduling problem. </li> 
 * </ul> 
 * Often it is also the case that value and variable ordering heuristics for meta-CSP search are related to the meta-CSP's {@link MetaConstraint}s.
 * For instance, in scheduling, a variable ordering heuristic may use the capacity of a resource to assess how critical a particular
 * conflict set is.
 * 
 * @author Federico Pecora
 *
 */
public abstract class MetaConstraint extends Constraint {

	private static final long serialVersionUID = 3972541057627681253L;
	protected VariableOrderingH varOH;
	protected ValueOrderingH valOH;
	protected MetaConstraintSolver metaCS = null;
	protected boolean independentMC = false;
	protected Logger logger = MetaCSPLogging.getLogger(this.getClass());
	
	/**
	 * Creates a {@link MetaConstraint} with given variable and value ordering heuristics (one or both of these can be <code>null</code>).
	 * This constructor must be called by the implementing class. 
	 * @param varOH The variable ordering heuristic to be used by the meta-CSP search when dealing with this {@link MetaConstraint}.
	 * @param valOH The value ordering heuristic to be used by the meta-CSP search when dealing with this {@link MetaConstraint}.
	 */
	public MetaConstraint(VariableOrderingH varOH, ValueOrderingH valOH) {
		this.varOH = varOH;
		this.valOH = valOH;
	}
	
	/**
	 * Set the {@link MetaConstraintSolver} of this {@link MetaConstraint}.
	 * @param metaCS The {@link MetaConstraintSolver} of this {@link MetaConstraint}.
	 */
	public void setMetaSolver(MetaConstraintSolver metaCS) { 
		this.metaCS = metaCS;
		logger.finest("Set MetaConstraintSolver to " + metaCS.getDescription());
	}
	
	/**
	 * Get one {@link MetaVariable} according to this {@link MetaConstraint}.  This returns the
	 * highest-priority {@link MetaVariable} according to this {@link MetaConstraint}'s variable ordering heuristic.
	 * @return A {@link MetaVariable} according to this {@link MetaConstraint}.
	 */
	public ConstraintNetwork getMetaVariable() {
		ConstraintNetwork[] vars = this.getMetaVariables();
		if (vars != null && vars.length > 0) {
			if (varOH != null) {
				varOH.collectData(vars);
				Arrays.sort(vars, varOH);
			}
			return vars[0];
		}
		return null;
	}
	
	/**
	 * Get all {@link MetaVariable}s according to this {@link MetaConstraint}.  This method must be implemented by the
	 * designer of the implementing {@link MetaConstraint} class.
	 * @return All {@link MetaVariable}s according to this {@link MetaConstraint}.
	 */
	public abstract ConstraintNetwork[] getMetaVariables();
	
	//public abstract boolean metaPropagation(ConstraintNetwork metaValue);
	
	/**
	 * Get one meta value for a given {@link MetaVariable}. Note that this calls
	 * the getMetaValues method (which must be implemented in the specific
	 * {@link MetaConstraint}) and returns the value with highest score as determined
	 * by this {@link MetaConstraint}'s {@link ValueOrderingH}.
	 * @param metaVariable The {@link MetaVariable} for which we seek a meta value.
	 * @return The best meta value for the given {@link MetaVariable}.
	 */
	public ConstraintNetwork getMetaValue(MetaVariable metaVariable) {
		ConstraintNetwork[] vals = this.getMetaValues(metaVariable);
		if (valOH != null) Arrays.sort(vals, valOH);
		return vals[0];
	}
	
	/**
	 * Get all meta values for a given {@link MetaVariable}.  This method must be
	 * implemented in the specific {@link MetaConstraint}. 
	 * @param metaVariable The {@link MetaVariable} for which we seek meta values.
	 * @return All meta values for the given {@link MetaVariable}.
	 */
	public abstract ConstraintNetwork[] getMetaValues(MetaVariable metaVariable);

	/**
	 * Method to mark a {@link MetaVariable} as solved. This method is called by the
	 * {@link MetaConstraintSolver} and must be implemented by the developer of the
	 * {@link MetaConstraint}.  
	 * @param metaVariable The {@link MetaVariable} that is to be considered solved by
	 * the given meta value.
	 * @param metaValue The solving meta value.
	 */
	public abstract void markResolvedSub(MetaVariable metaVariable, ConstraintNetwork metaValue);
	
	/**
	 * Method to draw the metaCSP's constraint network according to the rationale of
	 * this {@link MetaConstraint} (e.g., for a resource, this could be the resource usage profile
	 * as it is determined by the given constraint network).  This method must be implemented by the
	 * developer of the {@link MetaConstraint}.
	 * @param network The constraint network to draw.
	 */
	public abstract void draw(final ConstraintNetwork network);
	
	@Override
	public String getDescription() {
		String ret = "[";
		ret += this.getClass().getSimpleName() + " varOH: " + (this.varOH != null ? this.varOH.getClass().getSimpleName() : "null") + " valOH: " + (this.valOH != null ? this.varOH.getClass().getSimpleName() : "null") + "]";
		return ret;
	}
	
	/**
	 * Method to get the Variable Ordering function
	 */
	public VariableOrderingH getVarOH() {
		return varOH;
	}
	/**
	 * Method to set the Variable Ordering function
	 */

	public void setVarOH(VariableOrderingH varOH) {
		this.varOH = varOH;
	}
	/**
	 * Method to get the Value Ordering function
	 */
	public ValueOrderingH getValOH() {
		return valOH;
	}
	/**
	 * Method to set the Value Ordering function
	 */
	public void setValOH(ValueOrderingH valOH) {
		this.valOH = valOH;
	}
	
	/**
	 * Provides the definition of how to get the/a groundSolver for this {@link MetaConstraint}. 
	 * @return The/a groundSolver for this {@link MetaConstraint}.
	 */
	public abstract ConstraintSolver getGroundSolver();
	
}
