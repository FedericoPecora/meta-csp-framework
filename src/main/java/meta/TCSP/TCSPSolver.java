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
