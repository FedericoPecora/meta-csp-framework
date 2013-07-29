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

import java.util.Arrays;
import java.util.logging.Logger;

import org.metacsp.utility.logging.MetaCSPLogging;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;

/**
 * This class is used to represent binay constraints among {@link MultiVariable}s.
 * 
 * @author Federico Pecora
 *
 */
public abstract class MultiBinaryConstraint extends MultiConstraint {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5679258233083583893L;
	private transient Logger logger = MetaCSPLogging.getLogger(this.getClass());

	/**
	 * Instantiates a new {@link MultiBinaryConstraint}, sets its scope to have size 2.
	 */
	public MultiBinaryConstraint() {
		this.scope = new Variable[2];
	}

	@Override
	protected Constraint[] createInternalConstraints(Variable[] variables) {
		Constraint[] ret = this.createInternalConstraints(variables[0], variables[1]);
		//logger.finest("Created internal constraints for " + this + ":\n\t" + Arrays.toString(ret));
		return ret;
	}

	/**
	 * This method must be defined to create the internal constraints underlying 
	 * this {@link MultiBinaryConstraint}.
	 * @param from The source {@link Variable} of the {@link MultiConstraint}.
	 * @param to The destination {@link Variable} of the {@link MultiConstraint}.
	 * @return An array of lower-level constraints which "implement" this {@link MultiBinaryConstraint}.
	 */
	protected abstract Constraint[] createInternalConstraints(Variable from, Variable to);

	/**
	 * Get the source {@link Variable} of this {@link MultiBinaryConstraint}.
	 * @return The source {@link Variable} of this {@link MultiBinaryConstraint}.
	 */
	public Variable getFrom() {
		return this.scope[0];
	}

	/**
	 * Get the destination {@link Variable} of this {@link MultiBinaryConstraint}.
	 * @return The destination {@link Variable} of this {@link MultiBinaryConstraint}.
	 */
	public Variable getTo() {
		return this.scope[1];
	}

	/**
	 * Set the source {@link Variable} of this {@link MultiBinaryConstraint}.
	 * @param mv The source {@link Variable} of this {@link MultiBinaryConstraint}.
	 */
	public void setFrom(Variable mv) {
		this.scope[0] = mv;
	}

	/**
	 * Set the destination {@link Variable} of this {@link MultiBinaryConstraint}.
	 * @param mv The destination {@link Variable} of this {@link MultiBinaryConstraint}.
	 */
	public void setTo(Variable mv) {
		this.scope[1] = mv;
	}

	@Override
	public String toString() {
		Variable fromVar = this.getFrom();
		Variable toVar = this.getTo();
		return "(" + fromVar + ") --" + this.getEdgeLabel() + "--> (" + toVar + ")";
	}


}
