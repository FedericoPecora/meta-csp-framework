package org.metacsp.meta.hybridPlanner;

import org.metacsp.framework.ConstraintSolver;
import org.metacsp.meta.simplePlanner.SimpleDomain;
import org.metacsp.multi.spatioTemporal.SpatialFluentSolver;


public class FluentBasedSimpleDomain extends SimpleDomain {
	
	private long timeNow = -1;
	public FluentBasedSimpleDomain(int[] capacities, String[] resourceNames,
			String domainName) {
		super(capacities, resourceNames, domainName);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 8380363685271158262L;
	
	@Override
	public ConstraintSolver getGroundSolver() {
		return ((SpatialFluentSolver)metaCS.getConstraintSolvers()[0]).getConstraintSolvers()[1];
	}
	

	public void updateTimeNow(long timeNow) {
		this.timeNow = timeNow;
	}
}
