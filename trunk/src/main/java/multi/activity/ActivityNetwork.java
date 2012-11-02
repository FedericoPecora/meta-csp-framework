package multi.activity;

import framework.ConstraintNetwork;
import framework.ConstraintSolver;

public class ActivityNetwork extends ConstraintNetwork {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1628887638592525043L;

	public ActivityNetwork(ConstraintSolver sol) {
		super(sol);
		// TODO Auto-generated constructor stub
	}
		
	public long getOrigin() {
		return ((ActivityNetworkSolver)this.solver).getOrigin();
	}
	
	public long getHorizon() {
		return ((ActivityNetworkSolver)this.solver).getHorizon();
	}
	
}
