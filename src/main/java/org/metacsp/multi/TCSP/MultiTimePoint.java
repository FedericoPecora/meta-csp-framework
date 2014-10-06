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
package org.metacsp.multi.TCSP;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiVariable;
import org.metacsp.time.TimePoint;

public class MultiTimePoint extends MultiVariable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1769651361635250174L;

//	protected MultiTimePoint(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers) {
//		super(cs, id, internalSolvers);
//		// TODO Auto-generated constructor stub
//	}

	public MultiTimePoint(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers, Variable[] internalVars) {
		super(cs, id, internalSolvers, internalVars);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compareTo(Variable arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

//	@Override
//	protected Variable[] createInternalVariables() {
//		Variable tp = internalSolvers[0].createVariable();
//		return new Variable[] {tp};
//	}

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
	
	public void setTimePoint(TimePoint tp) {
		this.getInternalVariables()[0] = tp;
	}
	
	/**
	 * Get this time point's lower bound.
	 * @return This time point's lower bound.
	 */
	public long getLowerBound(){
		return ((TimePoint)this.getInternalVariables()[0]).getLowerBound();
	}



	/**
	 * Get this time point's upper bound.
	 * @return This time point's upper bound.
	 */
	public long getUpperBound(){
		return ((TimePoint)this.getInternalVariables()[0]).getUpperBound();
	}

}	

