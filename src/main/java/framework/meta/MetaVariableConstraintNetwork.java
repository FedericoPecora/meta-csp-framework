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
package framework.meta;

import framework.ConstraintNetwork;
import framework.ConstraintSolver;

/**
 * A constraint network for meta-CSPs.  This is used to maintain the search
 * space of the {@link MetaConstraintSolver}.
 * 
 * @author Federico Pecora
 *
 */
public class MetaVariableConstraintNetwork extends ConstraintNetwork {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1761603965354453570L;

	/**
	 * Instantiates a constraint network for use by a {@link MetaConstraintSolver}.
	 * @param sol The meta-CSP solver maintaining this network.
	 */
	public MetaVariableConstraintNetwork(ConstraintSolver sol) {
		super(sol);
		// TODO Auto-generated constructor stub
	}

}
