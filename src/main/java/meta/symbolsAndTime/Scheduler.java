package meta.symbolsAndTime;

import multi.activity.ActivityNetworkSolver;
import multi.allenInterval.AllenIntervalConstraint;
import symbols.SymbolicValueConstraint;
import framework.ConstraintNetwork;
import framework.meta.MetaConstraintSolver;
import framework.meta.MetaVariable;

public class Scheduler extends MetaConstraintSolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8551829132754804513L;

	public Scheduler(long origin, long horizon, long animationTime) {
		super(new Class[] {AllenIntervalConstraint.class, SymbolicValueConstraint.class}, animationTime, new ActivityNetworkSolver(origin, horizon, 500));
	}

	public Scheduler(long origin, long horizon, long animationTime, int numActivities) {
		super(new Class[] {AllenIntervalConstraint.class, SymbolicValueConstraint.class}, animationTime, new ActivityNetworkSolver(origin, horizon, numActivities));
	}

	@Override
	public void preBacktrack() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void retractResolverSub(ConstraintNetwork metaVariable, ConstraintNetwork metaValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void addResolverSub(ConstraintNetwork metaVariable, ConstraintNetwork metaValue) {
		// TODO Auto-generated method stub
		
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
