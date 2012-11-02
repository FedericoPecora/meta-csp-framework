package multi.allenInterval;

import time.APSPSolver;
import time.Bounds;
import time.TimePoint;
import framework.ConstraintNetwork;
import framework.ConstraintSolver;
import framework.multi.MultiConstraintSolver;

public class AllenIntervalNetworkSolver extends MultiConstraintSolver {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2059523989033941914L;
	private int IDs = 0;
	
	public AllenIntervalNetworkSolver(long origin, long horizon, int maxActivities) {
		super(new Class[]{AllenIntervalConstraint.class}, new Class[]{AllenInterval.class}, createConstraintSolvers(origin, horizon, maxActivities));
	}
	
	public AllenIntervalNetworkSolver(long origin, long horizon) {
		super(new Class[]{AllenIntervalConstraint.class}, new Class[]{AllenInterval.class}, createConstraintSolvers(origin, horizon, -1));
	}
	
	private static ConstraintSolver[] createConstraintSolvers(long origin, long horizon, int maxActivities) {
		APSPSolver stpSolver;
		if (maxActivities >= 1)
			stpSolver = new APSPSolver(origin, horizon, 2*maxActivities);
		else
			stpSolver = new APSPSolver(origin, horizon);
		return new ConstraintSolver[] {stpSolver};
	}

	/**
	 * Gets the minimum and maximum allowed distance between a pair of {@link TimePoint}s.
	 * @param tpFrom the {@link TimePoint} that is considered as the "origin"/source.  
	 * @param tpTo the {@link TimePoint} that is considered as the "target".
	 * @return Returns the bounds of the distance between the two {@link TimePoint}s
	 * or <code>null</code> if there is no constraint between the two.
	 */
	public final Bounds getAdmissibleDistanceBounds(TimePoint tpFrom, TimePoint tpTo) {
		final APSPSolver stpSolver = (APSPSolver) constraintSolvers[0];
		return stpSolver.getDistanceBounds(tpFrom, tpTo);
	}
	
	/**
	 * Get the minimum and maximum allowed distance between the global source and a {@link TimePoint}. 
	 * Calling this function is equivalent to calling 
	 * {@link #getAdmissibleDistanceBounds(TimePoint, TimePoint)}
	 * With the parameters <code>(<i>Source</i>, tpTo)<code>.
	 * @param tpTo the {@link TimePoint} whose temporal placement should be queried.
	 * @return The allowed bounds of the {@link TimePoint}.
	 */
	public final Bounds getAdmissibleDistanceBounds(TimePoint tpTo) {
		final APSPSolver stpSolver = (APSPSolver) constraintSolvers[0];
		return stpSolver.getDistanceBounds(stpSolver.getSource(), tpTo);
	}
	
	/********************/

	@Override
	protected ConstraintNetwork createConstraintNetwork() {
		return new AllenIntervalNetwork(this);
	}

	@Override
	protected AllenInterval createVariableSub() {
		return new AllenInterval(this, IDs++, this.constraintSolvers);
	}

	@Override
	protected AllenInterval[] createVariablesSub(int num) {
		AllenInterval[] ret = new AllenInterval[num];
		for (int i = 0; i < num; i++) {
			ret[i] = new AllenInterval(this, IDs++, this.constraintSolvers);
		}
		return ret;
	}

	@Override
	public boolean propagate() {
		// Do nothing, APSPSolver takes care of propagation...
		return true;
	}
	
}
