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

package booleanSAT;

import framework.ConstraintNetwork;
import framework.ConstraintSolver;

/**
 * Class for maintaining netowrks of {@link BooleanConstraint}s.
 * Since {@link BooleanConstraint}s represent disjunctive Boolean clauses,
 * a {@link BooleanConstraintNetwork} represents a well-formed-formula (wff)
 * in Conjunctive Normal Form (CNF). Note that {@link BooleanConstraint}s can be
 * instantiated with a factory method from non-CNF formulas
 * (see {@link BooleanConstraint#createBooleanConstraints(BooleanVariable[], String)}.
 * 
 * @author Federico Pecora
 */
public class BooleanConstraintNetwork extends ConstraintNetwork {

	public BooleanConstraintNetwork(ConstraintSolver sol) {
		super(sol);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 7423941525196210640L;

}
