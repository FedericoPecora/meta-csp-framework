package org.metacsp.multi.spatioTemporal;

import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.multi.MultiConstraintSolver;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.spatial.blockAlgebra.BlockAlgebraConstraint;
import org.metacsp.multi.spatial.blockAlgebra.BlockConstraintSolver;
import org.metacsp.multi.spatial.blockAlgebra.UnaryBlockConstraint;
import org.metacsp.multi.symbols.SymbolicValueConstraint;

public class SpatialFluentSolver2 extends MultiConstraintSolver{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1449731304165896873L;
	protected int IDs = 0;
	
	public SpatialFluentSolver2(long origin, long horizon) {
		super(new Class[] {BlockAlgebraConstraint.class, UnaryBlockConstraint.class, AllenIntervalConstraint.class, SymbolicValueConstraint.class}, 
				SpatialFluent2.class,
				createConstraintSolvers(origin, horizon, -1),
				new int[] {1,1});
	}

	public SpatialFluentSolver2(long origin, long horizon, int maxFluent) {
		super(new Class[] {BlockAlgebraConstraint.class, UnaryBlockConstraint.class, AllenIntervalConstraint.class, SymbolicValueConstraint.class}, 
				SpatialFluent2.class,
				createConstraintSolvers(origin, horizon, maxFluent),
				new int[] {1,1});
	}
	
	private static ConstraintSolver[] createConstraintSolvers(long origin, long horizon, int maxFluents) {		
		ConstraintSolver[] ret = maxFluents != -1 ? new ConstraintSolver[] {new BlockConstraintSolver(origin, horizon, maxFluents), 
				new ActivityNetworkSolver(origin, horizon, maxFluents)}
			:new ConstraintSolver[] {new BlockConstraintSolver(origin, horizon), new ActivityNetworkSolver(origin, horizon)};	
		return ret;
	}
	


	@Override
	public boolean propagate() {
		return false;
	}

}
