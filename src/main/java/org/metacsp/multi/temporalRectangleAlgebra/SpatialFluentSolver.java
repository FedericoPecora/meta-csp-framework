package org.metacsp.multi.temporalRectangleAlgebra;

import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.multi.MultiConstraintSolver;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangleConstraint;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangleConstraintSolver;
import org.metacsp.multi.spatial.rectangleAlgebra.UnaryRectangleConstraint;
import org.metacsp.multi.symbols.SymbolicValueConstraint;
import org.metacsp.spatial.reachability.ReachabilityConstraint;
import org.metacsp.spatial.reachability.ReachabilityContraintSolver;

public class SpatialFluentSolver extends MultiConstraintSolver{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 593944303888996893L;
	protected int IDs = 0;
	
	public SpatialFluentSolver(long origin, long horizon) {
		super(new Class[] {RectangleConstraint.class, UnaryRectangleConstraint.class, AllenIntervalConstraint.class, SymbolicValueConstraint.class, ReachabilityConstraint.class}, 
				SpatialFluent.class,
				createConstraintSolvers(origin, horizon, -1),
				new int[] {1,1,1});
	}

	public SpatialFluentSolver(long origin, long horizon, int maxFluent) {
		super(new Class[] {RectangleConstraint.class, UnaryRectangleConstraint.class, AllenIntervalConstraint.class, SymbolicValueConstraint.class, ReachabilityConstraint.class}, 
				SpatialFluent.class,
				createConstraintSolvers(origin, horizon, maxFluent),
				new int[] {1,1,1});
	}
	
	private static ConstraintSolver[] createConstraintSolvers(long origin, long horizon, int maxFluents) {		
		ConstraintSolver[] ret = maxFluents != -1 ? new ConstraintSolver[] {new RectangleConstraintSolver(origin, horizon, maxFluents), new ActivityNetworkSolver(origin, horizon, maxFluents),
				new ReachabilityContraintSolver()}
			:new ConstraintSolver[] {new RectangleConstraintSolver(origin, horizon), new ActivityNetworkSolver(origin, horizon), new ReachabilityContraintSolver()};	
		return ret;
	}
	
//	@Override
//	protected Variable[] createVariablesSub(int num) {
//		Variable[] ret = new Variable[num];
//		for (int i = 0; i < num; i++)
//			ret[i] = new SpatialFluent(this, IDs++, this.constraintSolvers);
//		return ret;
//	}

	@Override
	public boolean propagate() {
		// TODO Auto-generated method stub
		return false;
	}

}
