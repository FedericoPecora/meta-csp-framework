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

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;

/**
 * A special constraint network which is used to represent terminal nodes
 * in the search space of the {@link MetaConstraintSolver}.
 * 
 * @author Federico Pecora
 *
 */
public class NullConstraintNetwork extends ConstraintNetwork {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4572644031938139796L;

	/**
	 * Instantiates a new terminal node for the meta-CSP search space.
	 * @param sol The meta-CSP solver which has reached a terminal node in its search space.
	 */
	public NullConstraintNetwork(ConstraintSolver sol) {
		super(sol);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Get a {@link String} representation of the terminal node.
	 */
	public String toString() {
		return "conflicting";
	}

}
