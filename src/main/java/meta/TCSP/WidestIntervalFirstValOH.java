package meta.TCSP;

import multi.TCSP.DistanceConstraint;
import time.APSPSolver;
import time.SimpleDistanceConstraint;
import framework.Constraint;
import framework.ConstraintNetwork;
import framework.ValueOrderingH;

public class WidestIntervalFirstValOH extends ValueOrderingH {

	@Override
	public int compare(ConstraintNetwork arg0, ConstraintNetwork arg1) {
		Constraint[] cons0 = arg0.getConstraints();
		Constraint[] cons1 = arg1.getConstraints();
		SimpleDistanceConstraint con0 = (SimpleDistanceConstraint)((DistanceConstraint)cons0[0]).getInternalConstraints()[0];
		SimpleDistanceConstraint con1 = (SimpleDistanceConstraint)((DistanceConstraint)cons1[0]).getInternalConstraints()[0];
		int min0 = 0, max0 = 0, min1 = 0, max1 = 0;
		if (con0.getMinimum() == APSPSolver.INF) min0 = Integer.MAX_VALUE;
		else min0 = (int)con0.getMinimum();
		if (con0.getMaximum() == APSPSolver.INF) max0 = Integer.MAX_VALUE;
		else max0 = (int)con0.getMaximum();
		if (con1.getMinimum() == APSPSolver.INF) min1 = Integer.MAX_VALUE;
		else min1 = (int)con1.getMinimum();
		if (con1.getMaximum() == APSPSolver.INF) max1 = Integer.MAX_VALUE;
		else max1 = (int)con1.getMaximum();
		int distance0 = Math.abs(max0-min0); 
		int distance1 = Math.abs(max1-min1);
		return distance0-distance1;
	}

}
