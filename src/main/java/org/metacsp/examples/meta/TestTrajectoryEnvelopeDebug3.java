package org.metacsp.examples.meta;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.meta.spatioTemporal.paths.Map;
import org.metacsp.meta.spatioTemporal.paths.TrajectoryEnvelopeScheduler;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.spatial.DE9IM.DE9IMRelationSolver;
import org.metacsp.multi.spatial.DE9IM.GeometricShapeDomain;
import org.metacsp.multi.spatioTemporal.paths.Trajectory;
import org.metacsp.multi.spatioTemporal.paths.PoseSteering;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelope;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelopeSolver;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.UI.JTSDrawingPanel;
import org.metacsp.utility.UI.TrajectoryEnvelopeAnimator;

import com.sun.corba.se.spi.ior.MakeImmutable;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class TestTrajectoryEnvelopeDebug3 {
	
	
	public static void printInfo(TrajectoryEnvelope te) {
		
		double[] teDTs = te.getTrajectory().getDTs();
		double[] teCTs = te.getCTs();

		DecimalFormat df = new DecimalFormat("#0.00");
		df.setRoundingMode(RoundingMode.HALF_DOWN);
		System.out.println("------------------------------------------\n" + te + "\nGround env: " + te.getGroundEnvelopes()  + "\nDTs and CTs\n------------------------------------------");
		for (int i = 0; i < teDTs.length; i++) {
			System.out.println(i + ": " + df.format(teDTs[i]) + " \t " + df.format(teCTs[i]));
		}
		
	}
	
	public static TrajectoryEnvelope[] makeTrajectoryEnvelopes(TrajectoryEnvelopeSolver solver, int robotID, String ... files) {
		// Footprint coordinates (reference point in (0,0), as in SemRob)
		Coordinate frontLeft = new Coordinate(8.100, 4.125);
		Coordinate frontRight = new Coordinate(8.100, -3.430);
		Coordinate backRight = new Coordinate(-6.920, -3.430);
		Coordinate backLeft = new Coordinate(-6.920, 4.125);

		Variable[] vars = solver.createVariables(files.length);
		TrajectoryEnvelope[] ret = new TrajectoryEnvelope[files.length];
		for (int i = 0; i < files.length; i++) {
			ret[i] = (TrajectoryEnvelope)vars[i];
			Trajectory traj = new Trajectory(files[i]);
			ret[i].setFootprint(backLeft,backRight,frontLeft,frontRight);
			ret[i].setTrajectory(traj);
			ret[i].setRobotID(robotID);
		}
		return ret;	
	}
	
	public static void main(String[] args) {
		
		TrajectoryEnvelopeScheduler metaSolver = new TrajectoryEnvelopeScheduler(0, 10000000);
		TrajectoryEnvelopeSolver solver = (TrajectoryEnvelopeSolver)metaSolver.getConstraintSolvers()[0];
		
		TrajectoryEnvelope[] robot1Envelopes = makeTrajectoryEnvelopes(solver, 1,
				"paths/debugPaths/new/test0_1.path",
				"paths/debugPaths/new/test1_1.path",
				"paths/debugPaths/new/test2_1.path",
				"paths/debugPaths/new/test3_1.path",
				"paths/debugPaths/new/test4_1.path",
				"paths/debugPaths/new/test5_1.path"
				);

		TrajectoryEnvelope[] robot2Envelopes = makeTrajectoryEnvelopes(solver, 2,
				"paths/debugPaths/new/test6_2.path",
				"paths/debugPaths/new/test7_2.path",
				"paths/debugPaths/new/test8_2.path",
				"paths/debugPaths/new/test9_2.path"
				);


		AllenIntervalConstraint[] meetsRobot1 = new AllenIntervalConstraint[robot1Envelopes.length-1];		
		for (int i = 0; i < robot1Envelopes.length-1; i++) {
			AllenIntervalConstraint meets = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets);
			meets.setFrom(robot1Envelopes[i]);
			meets.setTo(robot1Envelopes[i+1]);
			meetsRobot1[i] = meets;
		}
		
		System.out.println(solver.addConstraints(meetsRobot1));
		
//		Map map = new Map(null, null);		
//		metaSolver.addMetaConstraint(map);
//		
//		ConstraintNetwork refined1 = metaSolver.refineTrajectoryEnvelopes();
//		System.out.println("REFINED: "+  refined1);
//
//		boolean solved = metaSolver.backtrack();
//		System.out.println("Solved? " + solved);
//		if (solved) System.out.println("Added resolvers:\n" + Arrays.toString(metaSolver.getAddedResolvers()));

		TrajectoryEnvelopeAnimator tea = new TrajectoryEnvelopeAnimator("This is a test");
		tea.addTrajectoryEnvelopes(robot1Envelopes);

//		JTSDrawingPanel.drawConstraintNetwork("Geometries after refinement",refined1);
//		ConstraintNetwork.draw(solver.getConstraintNetwork());

	}
	
}
