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
package meta.TCSP;

import multi.TCSP.DistanceConstraint;
import multi.TCSP.DistanceConstraintSolver;
import framework.ConstraintNetwork;
import framework.meta.MetaConstraintSolver;
import framework.meta.MetaVariable;
import framework.multi.MultiConstraint;
import framework.multi.MultiConstraintSolver;

public class TCSPSolver extends MetaConstraintSolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9151396838845303080L;

	public TCSPSolver(long origin, long horizon, long animationTime) {
		super(new Class[] {DistanceConstraint.class}, animationTime, new DistanceConstraintSolver(origin, horizon));
	}
	
	@Override
	public void preBacktrack() {
		((MultiConstraintSolver)this.getConstraintSolvers()[0]).setOptions(OPTIONS.FORCE_CONSISTENCY);
	}

	@Override
	protected void retractResolverSub(ConstraintNetwork metaVariable, ConstraintNetwork metaValue) {
		MultiConstraint dc = (MultiConstraint)metaVariable.getConstraints()[0];
		dc.setPropagateLater();
	}

	@Override
	protected void addResolverSub(ConstraintNetwork metaVariable, ConstraintNetwork metaValue) {
		MultiConstraint dc = (MultiConstraint)metaValue.getConstraints()[0];
		dc.setPropagateImmediately();
	}

	@Override
	public void postBacktrack(MetaVariable mv) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected double getUpperBound() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void setUpperBound() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected double getLowerBound() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void setLowerBound() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean hasConflictClause(ConstraintNetwork metaValue) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void resetFalseClause() {
		// TODO Auto-generated method stub
		
	}
	
}
