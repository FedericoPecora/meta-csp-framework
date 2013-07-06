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

import java.util.Arrays;
import java.util.logging.Logger;

import multi.allenInterval.AllenIntervalConstraint;
import utility.logging.MetaCSPLogging;
import framework.Constraint;
import framework.ConstraintSolver;
import framework.Variable;

/**
 * A {@link MultiConstraint} is a constraint among {@link Variable}s that could be {@link MultiVariable}s.
 * Every {@link MultiConstraint} is "implemented" one or more lower-level
 * constraints - see, e.g., the {@link AllenIntervalConstraint}.
 *  
 * @author Federico Pecora
 *
 */
public abstract class MultiConstraint extends Constraint {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2743945338930729256L;

	protected Constraint[] constraints;

	private transient Logger logger = MetaCSPLogging.getLogger(this.getClass());
	
	private boolean propagateImmediately = true;

	/**
	 * Instantiates a {@link MultiConstraint}.  This constructor must be called by the
	 * constructor of the implementing class.
	 */
	public MultiConstraint() {
		this.constraints = null;
	}

	/**
	 * This method must be implemented by the implementing class, and should instantiate
	 * the internal constraints underlying this {@link MultiConstraint}. 
	 * @param variables The {@link Variable}s that are in the scope of this {@link MultiConstraint}.
	 * @return An array of lower-level constraints which "implement" this {@link MultiConstraint}.
	 */
	protected abstract Constraint[] createInternalConstraints(Variable[] variables);

	/**
	 * Get the lower-level constraints underlying of this {@link MultiConstraint}.
	 * @return An array of lower-level constraints underlying of this {@link MultiConstraint}.
	 */
	public Constraint[] getInternalConstraints() {
		if (constraints == null) constraints = this.createInternalConstraints(this.scope);
		return constraints;
	}
	
	/**
	 * A {@link MultiConstraint} must be cloneable.  This is used by the {@link MultiConstraintSolver}
	 * class to instantiate proper constraints to delegate its underlying {@link ConstraintSolver}s.
	 */
	public abstract Object clone();
	
	/**
	 * Delays the propagation of this {@link MultiConstraint}.
	 */
	public void setPropagateLater() {
		propagateImmediately = false;
	}

	/**
	 * Schedules this {@link MultiConstraint} for immediate propagation.
	 */
	public void setPropagateImmediately() {
		propagateImmediately = true;
	}

	/**
	 * A {@link MultiConstraint} can be scheduled for propagation immediately (as soon as it is added)
	 * or later.  This is used internally by the {@link ConstraintSolver} to delay propagation
	 * of a {@link MultiConstraint} in cases where the underlying constraints should be propagated first.
	 * By default, {@link MultiConstraint}s are propagated immediately.
	 * @return <code>true</code> iff this constraint should be propagated as soon as possible.
	 */
	public boolean propagateImmediately() {
		return propagateImmediately;
	}
	
	@Override
	public String getDescription() {
		String ret = this.getClass().getSimpleName() + ": [";
		if (this.getInternalConstraints() != null) {
			for (int i = 0; i < this.getInternalConstraints().length; i++) {
				ret += this.getInternalConstraints()[i].getClass().getSimpleName();
				if (i != this.getInternalConstraints().length-1) ret += ",";
			}
		}
		return ret + "]";
	}
}
