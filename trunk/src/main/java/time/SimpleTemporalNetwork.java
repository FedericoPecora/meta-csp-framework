package time;

import framework.ConstraintNetwork;
import framework.ConstraintSolver;

public class SimpleTemporalNetwork extends ConstraintNetwork {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6006895701929725047L;

	public SimpleTemporalNetwork(ConstraintSolver sol) {
		super(sol);
		// TODO Auto-generated constructor stub
	}
	
	public long getOrigin() {
		return ((APSPSolver)this.solver).getO();
	}
	
	public long getHorizon() {
		return ((APSPSolver)this.solver).getH();
	}
}
