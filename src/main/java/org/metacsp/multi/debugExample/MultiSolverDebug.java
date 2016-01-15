package org.metacsp.multi.debugExample;

import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.multi.MultiConstraintSolver;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.allenInterval.AllenIntervalNetworkSolver;
import org.metacsp.multi.spatial.DE9IM.DE9IMRelation;
import org.metacsp.multi.spatial.DE9IM.DE9IMRelationSolver;
import org.metacsp.multi.spatial.DE9IM.GeometricShapeVariable;
import org.metacsp.multi.symbols.SymbolicValueConstraint;

public class MultiSolverDebug extends MultiConstraintSolver {

	private static final long serialVersionUID = -2577775428011352871L;

	public MultiSolverDebug(long origin, long horizon) {
		super(new Class[] {
				AllenIntervalConstraint.class, SymbolicValueConstraint.class,
				DE9IMRelation.class}, 
				MultiVarDebug.class,
				createConstraintSolvers(origin, horizon, -1),
				new int[] {1,1});
	}
	
	private static ConstraintSolver[] createConstraintSolvers(long origin, long horizon, int maxFluents) {	
		ConstraintSolver[] ret = new ConstraintSolver[2];
		if (maxFluents != -1) {
			ret[0] = new ActivityNetworkSolver(origin, horizon, maxFluents);
		} else {
			ret[0] = new ActivityNetworkSolver(origin, horizon);
		}
		ret[1] = new DE9IMRelationSolver();
		return ret;
	}

	@Override
	public boolean propagate() {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	/**
	 * Returns the {@link ActivityNetworkSolver} which propagates the temporal and symbolic constraints.
	 * @return The activity network solver.
	 */
	public AllenIntervalNetworkSolver getActivityNetworkSolver() {
		return (AllenIntervalNetworkSolver)this.getConstraintSolvers()[0];
	}

	/**
	 * Returns the {@link DE9IMRelationSolver} which propagates spatial constraints (of type {@link DE9IMRelation})
	 * among the spatial parts ({@link GeometricShapeVariable}s) of {@link MultiVarDebug}s.
	 * @return The spatial solver responsible for propagating the spatial constraints among
	 * the spatial parts of {@link MultiVarDebug}s.
	 */
	public DE9IMRelationSolver getSpatialSolver() {
		return (DE9IMRelationSolver)this.getConstraintSolvers()[1];
	}

}
