package multi.TCSP;

import time.APSPSolver;
import framework.ConstraintNetwork;
import framework.ConstraintSolver;
import framework.Variable;
import framework.multi.MultiConstraintSolver;

public class DistanceConstraintSolver extends MultiConstraintSolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8474292073131422005L;
	private int IDs = 0;
	private MultiTimePoint source = null;
	private MultiTimePoint sink = null;
	
	public DistanceConstraintSolver(long origin, long horizon) {
		super(new Class[]{DistanceConstraint.class}, new Class[]{MultiTimePoint.class}, createConstraintSolvers(origin, horizon));	
		//Create source and sink as wrappers of APSPSolver's source and sink
		APSPSolver internalSolver = (APSPSolver)this.constraintSolvers[0];
		source = new MultiTimePoint(this, IDs++, internalSolver, internalSolver.getSource());
		sink = new MultiTimePoint(this, IDs++, internalSolver, internalSolver.getSink());
		this.theNetwork.addVariable(source);
		this.theNetwork.addVariable(sink);
		this.setOptions(OPTIONS.ALLOW_INCONSISTENCIES);		
	}

	private static ConstraintSolver[] createConstraintSolvers(long origin, long horizon) {
		APSPSolver stpSolver = new APSPSolver(origin, horizon);
		return new ConstraintSolver[] {stpSolver};
	}
	
	@Override
	protected ConstraintNetwork createConstraintNetwork() {
		return new DistanceConstraintNetwork(this);
	}
	
	@Override
	protected Variable createVariableSub() {
		return new MultiTimePoint(this, IDs++, this.constraintSolvers);
	}

	@Override
	protected Variable[] createVariablesSub(int num) {
		MultiTimePoint[] ret = new MultiTimePoint[num];
		for (int i = 0; i < num; i++) {
			ret[i] = new MultiTimePoint(this, IDs++, this.constraintSolvers);
		}
		return ret;
	}
	
	@Override
	public boolean propagate() {
		// APSPSolver will propagate what it can...
		// this solver does not know how to propagate (yet)
		return true;
	}

	public MultiTimePoint getSource() {
		return source;
	}

	public MultiTimePoint getSink() {
		return sink;
	}

	
}
