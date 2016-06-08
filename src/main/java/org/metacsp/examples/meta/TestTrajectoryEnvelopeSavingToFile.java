package org.metacsp.examples.meta;
import java.util.Arrays;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.meta.spatioTemporal.paths.Map;
import org.metacsp.meta.spatioTemporal.paths.TrajectoryEnvelopeScheduler;
import org.metacsp.multi.spatioTemporal.paths.Trajectory;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelope;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelopeSolver;
import org.metacsp.utility.UI.TrajectoryEnvelopeAnimator;

import com.vividsolutions.jts.geom.Coordinate;

public class TestTrajectoryEnvelopeSavingToFile {
	
	public static void main(String[] args) {
		
		TrajectoryEnvelopeScheduler metaSolver = new TrajectoryEnvelopeScheduler(0, 100000);
		TrajectoryEnvelopeSolver solver = (TrajectoryEnvelopeSolver)metaSolver.getConstraintSolvers()[0];
		Variable[] vars = solver.createVariables(2);
		TrajectoryEnvelope var0 = (TrajectoryEnvelope)vars[0];
		TrajectoryEnvelope var1 = (TrajectoryEnvelope)vars[1];
		
		Coordinate frontLeft = new Coordinate(1.8, 0.7);
		Coordinate frontRight = new Coordinate(1.8, -0.7);
		Coordinate backRight = new Coordinate(-1.8, -0.7);
		Coordinate backLeft = new Coordinate(-1.8, 0.7);

		Trajectory traj0 = new Trajectory("paths/path1.path");
		var0.setFootprint(backLeft,backRight,frontLeft,frontRight);
		var0.setTrajectory(traj0);
		
		Trajectory traj1 = new Trajectory("paths/path3.path");
		var1.setFootprint(backLeft,backRight,frontLeft,frontRight);
		var1.setTrajectory(traj1);
		
		var0.setRobotID(1);
		var1.setRobotID(2);
		
		System.out.println(var0 + " has domain " + var0.getDomain());
		System.out.println(var1 + " has domain " + var1.getDomain());
		
		Map map = new Map(null, null);		
		metaSolver.addMetaConstraint(map);
		
		ConstraintNetwork refined1 = metaSolver.refineTrajectoryEnvelopes();
		System.out.println("REFINED: "+  refined1);
		
		boolean solved = metaSolver.backtrack();
		System.out.println("Solved? " + solved);
		if (solved) System.out.println("Added resolvers:\n" + Arrays.toString(metaSolver.getAddedResolvers()));

		ConstraintNetwork.saveConstraintNetwork(solver.getConstraintNetwork(),"savedConstraintNetworks/example.cn");
		TrajectoryEnvelopeAnimator tea = new TrajectoryEnvelopeAnimator("Trajectory Envelopes - after solving");
		tea.addTrajectoryEnvelopes(solver.getConstraintNetwork());

	}
	
}
