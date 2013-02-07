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
package multi.TCSP;

import framework.Constraint;
import framework.ConstraintSolver;
import framework.Domain;
import framework.Variable;
import framework.multi.MultiVariable;

public class MultiTimePoint extends MultiVariable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1769651361635250174L;

	protected MultiTimePoint(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers) {
		super(cs, id, internalSolvers);
		// TODO Auto-generated constructor stub
	}

	protected MultiTimePoint(ConstraintSolver cs, int id, ConstraintSolver internalSolver, time.TimePoint tp) {
		super(cs, id, new ConstraintSolver[] {internalSolver}, new Variable[] {tp}, null);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compareTo(Variable arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected Variable[] createInternalVariables() {
		Variable tp = internalSolvers[0].createVariable();
		return new Variable[] {tp};
	}

	@Override
	protected Constraint[] createInternalConstraints(Variable[] variables) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDomain(Domain d) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String toString() {
		return this.getInternalVariables()[0].toString();
	}
	
	public void setTimePoint(time.TimePoint tp) {
		this.getInternalVariables()[0] = tp;
	}

}
