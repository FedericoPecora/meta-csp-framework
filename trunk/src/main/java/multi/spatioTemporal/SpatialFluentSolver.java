package multi.spatioTemporal;

import multi.activity.ActivityNetworkSolver;
import multi.allenInterval.AllenIntervalConstraint;
import multi.spatial.rectangleAlgebra.RectangleConstraint;
import multi.spatial.rectangleAlgebra.RectangleConstraintSolver;
import multi.spatial.rectangleAlgebra.UnaryRectangleConstraint;
import symbols.SymbolicValueConstraint;
import framework.ConstraintNetwork;
import framework.ConstraintSolver;
import framework.multi.MultiConstraintSolver;

public class SpatialFluentSolver extends MultiConstraintSolver{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 593944303888996893L;
	protected int IDs = 0;
	
	public SpatialFluentSolver(long origin, long horizon) {
		super(new Class[] {RectangleConstraint.class, UnaryRectangleConstraint.class, AllenIntervalConstraint.class, SymbolicValueConstraint.class}, 
				//new Class[] {RectangularRegion2.class, Activity.class},
				new Class[] {SpatialFluent.class},
				createConstraintSolvers(origin, horizon, -1),
				new int[] {1,1});
	}

	public SpatialFluentSolver(long origin, long horizon, int maxFluent) {
		super(new Class[] {RectangleConstraint.class, UnaryRectangleConstraint.class, AllenIntervalConstraint.class, SymbolicValueConstraint.class}, 
				//new Class[] {RectangularRegion2.class, Activity.class},
				new Class[] {SpatialFluent.class},
				createConstraintSolvers(origin, horizon, maxFluent),
				new int[] {1,1});
	}
	
	private static ConstraintSolver[] createConstraintSolvers(long origin, long horizon, int maxFluents) {		
		ConstraintSolver[] ret = maxFluents != -1 ? new ConstraintSolver[] {new RectangleConstraintSolver(origin, horizon, maxFluents), new ActivityNetworkSolver(origin, horizon, maxFluents)}
			:new ConstraintSolver[] {new RectangleConstraintSolver(origin, horizon), new ActivityNetworkSolver(origin, horizon)};	
		return ret;
	}
	
	@Override
	protected ConstraintNetwork createConstraintNetwork() {
		return new SpatialFluentNetwork(this);
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
