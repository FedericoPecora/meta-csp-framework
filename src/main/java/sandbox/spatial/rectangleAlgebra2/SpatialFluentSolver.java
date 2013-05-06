package sandbox.spatial.rectangleAlgebra2;

import multi.activity.Activity;
import multi.activity.ActivityNetwork;
import multi.activity.ActivityNetworkSolver;
import multi.allenInterval.AllenIntervalConstraint;
import multi.allenInterval.AllenIntervalNetworkSolver;
import symbols.SymbolicValueConstraint;
import symbols.SymbolicVariableConstraintSolver;
import framework.ConstraintNetwork;
import framework.ConstraintSolver;
import framework.Variable;
import framework.multi.MultiConstraintSolver;

public class SpatialFluentSolver extends MultiConstraintSolver{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 593944303888996893L;
	protected int IDs = 0;
	
	public SpatialFluentSolver(long origin, long horizon) {
		super(new Class[] {RectangleConstraint2.class, UnaryRectangleConstraint2.class, AllenIntervalConstraint.class, SymbolicValueConstraint.class}, 
				new Class[] {RectangularRegion2.class, Activity.class}, 
				createConstraintSolvers(origin, horizon, -1));
	}

	public SpatialFluentSolver(long origin, long horizon, int maxFluent) {
		super(new Class[] {RectangleConstraint2.class, UnaryRectangleConstraint2.class, AllenIntervalConstraint.class, SymbolicValueConstraint.class}, 
				new Class[] {RectangularRegion2.class, Activity.class}, 
				createConstraintSolvers(origin, horizon, maxFluent));
	}
	
	
	private static ConstraintSolver[] createConstraintSolvers(long origin, long horizon, int maxFluents) {		
		ConstraintSolver[] ret = maxFluents != -1 ? new ConstraintSolver[] {new RectangleConstraintSolver2(origin, horizon, maxFluents), new ActivityNetworkSolver(origin, horizon, maxFluents)}
			:new ConstraintSolver[] {new AllenIntervalNetworkSolver(origin, horizon), new SymbolicVariableConstraintSolver()};	
		return ret;
	}

	
	@Override
	protected ConstraintNetwork createConstraintNetwork() {
		return new SpatialFluentNetwork(this);
	}

	@Override
	protected Variable[] createVariablesSub(int num) {
		Variable[] ret = new Variable[num];
		for (int i = 0; i < num; i++)
			ret[i] = new SpatialFeunt(this, IDs++, this.constraintSolvers);
		return ret;
	}

	@Override
	public boolean propagate() {
		// TODO Auto-generated method stub
		return false;
	}

}
