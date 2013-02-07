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
package framework;

/**
 * This is the basic abstract class for representing binary constraints
 * (i.e., constraints whose scope is of size 2). 
 */
public abstract class BinaryConstraint extends Constraint {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -303664629058197492L;

	/**
	 * Creates a new {@link BinaryConstraint}.
	 */
	public BinaryConstraint() {
		this.scope = new Variable[2];
	}
	
	/**
	 * Get the source {@link Variable} of this {@link BinaryConstraint}.
	 * @return The source {@link Variable} of this {@link BinaryConstraint}.
	 */
	public Variable getFrom() { return scope[0]; }

	/**
	 * Get the destination {@link Variable} of this {@link BinaryConstraint}.
	 * @return The destination {@link Variable} of this {@link BinaryConstraint}.
	 */
	public Variable getTo() { return scope[1]; }
	
	/**
	 * Set the source {@link Variable} of this {@link BinaryConstraint}.
	 * @param f The source {@link Variable} of this {@link BinaryConstraint}.
	 */
	public void setFrom(Variable f) {
			this.scope[0] = f;
	}

	/**
	 * Set the destination {@link Variable} of this {@link BinaryConstraint}.
	 * @param t The destination {@link Variable} of this {@link BinaryConstraint}.
	 */
	public void setTo(Variable t) { 
			this.scope[1] = t;
	}
	
	@Override
	public String toString() {
		return "(" + this.getFrom() + ") --" + this.getEdgeLabel() + "--> (" + this.getTo() + ")";
	}


}
