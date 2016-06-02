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
import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.metacsp.multi.spatioTemporal.paths.Trajectory;
import org.metacsp.multi.spatioTemporal.paths.PoseSteering;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelope;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelopeSolver;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.UI.JTSDrawingPanel;
import org.metacsp.utility.UI.TrajectoryEnvelopeAnimator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class VolvoCEDynamicCoordinationTest5 {
	
	
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
	
	public static void main(String[] args) {
		
		TrajectoryEnvelopeScheduler metaSolver = new TrajectoryEnvelopeScheduler(0, 100000);
		TrajectoryEnvelopeSolver solver = (TrajectoryEnvelopeSolver)metaSolver.getConstraintSolvers()[0];
		Variable[] vars = solver.createVariables(4);
		TrajectoryEnvelope trajEnvelopeRobot1 = (TrajectoryEnvelope)vars[0];
		TrajectoryEnvelope trajEnvelopeRobot2 = (TrajectoryEnvelope)vars[1];
		TrajectoryEnvelope trajEnvelopeRobot3 = (TrajectoryEnvelope)vars[2];
		TrajectoryEnvelope trajEnvelopeObstacle1 = (TrajectoryEnvelope)vars[3];
		
		//XA15 footprint, 2.7 (w) x 6.6 (l)
		Coordinate frontLeft = new Coordinate(5.3, 1.35);
		Coordinate frontRight = new Coordinate(5.3, -1.35);
		Coordinate backRight = new Coordinate(-1.3, -1.35);
		Coordinate backLeft = new Coordinate(-1.3, 1.35);
		
		Trajectory trajRobot1 = new Trajectory("paths/newpath1.path");
		trajEnvelopeRobot1.setFootprint(backLeft,backRight,frontLeft,frontRight);
		trajEnvelopeRobot1.setTrajectory(trajRobot1);
		trajEnvelopeRobot1.setRobotID(1);

		Trajectory trajRobot2 = new Trajectory("paths/newpath2.path");
		trajEnvelopeRobot2.setFootprint(backLeft,backRight,frontLeft,frontRight);
		trajEnvelopeRobot2.setTrajectory(trajRobot2);
		trajEnvelopeRobot2.setRobotID(2);

		Trajectory trajRobot3 = new Trajectory("paths/newpath3.path");
		trajEnvelopeRobot3.setFootprint(backLeft,backRight,frontLeft,frontRight);
		trajEnvelopeRobot3.setTrajectory(trajRobot3);
		trajEnvelopeRobot3.setRobotID(3);
		
		AllenIntervalConstraint robot1StartsAt = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(1500,APSPSolver.INF));
		robot1StartsAt.setFrom(trajEnvelopeRobot1);
		robot1StartsAt.setTo(trajEnvelopeRobot1);
		solver.addConstraint(robot1StartsAt);
		
		// This is an obstacle!
		trajEnvelopeObstacle1.setFootprint(3, 3, 0, 0);
		Pose obstacle1Position = new Pose(-10, -4, 0.0);
		Trajectory traj3 = new Trajectory(new Pose[] {obstacle1Position});
		trajEnvelopeObstacle1.setTrajectory(traj3);
		trajEnvelopeObstacle1.setRefinable(false);
		
		// When the obstacle should appear and disappear
		AllenIntervalConstraint obstacle1AppearsAt = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(5000,5000));
		obstacle1AppearsAt.setFrom(trajEnvelopeObstacle1);
		obstacle1AppearsAt.setTo(trajEnvelopeObstacle1);
		AllenIntervalConstraint obstacle1DisappearsAt = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(15000,15000));
		obstacle1DisappearsAt.setFrom(trajEnvelopeObstacle1);
		obstacle1DisappearsAt.setTo(trajEnvelopeObstacle1);
		solver.addConstraints(obstacle1AppearsAt,obstacle1DisappearsAt);
				
		System.out.println(trajEnvelopeRobot1 + " has domain " + trajEnvelopeRobot1.getDomain());
		System.out.println(trajEnvelopeRobot2 + " has domain " + trajEnvelopeRobot2.getDomain());
		System.out.println(trajEnvelopeRobot3 + " has domain " + trajEnvelopeRobot3.getDomain());
		System.out.println(trajEnvelopeObstacle1 + " has domain " + trajEnvelopeObstacle1.getDomain());
				
		Map map = new Map(null, null);		
		metaSolver.addMetaConstraint(map);
		
		ConstraintNetwork refined1 = metaSolver.refineTrajectoryEnvelopes();
		System.out.println("REFINED 1: "+  refined1);

		boolean solved = metaSolver.backtrack();
		System.out.println("Solved? " + solved);
		if (solved) System.out.println("Added resolvers:\n" + Arrays.toString(metaSolver.getAddedResolvers()));

		TrajectoryEnvelopeAnimator tea = new TrajectoryEnvelopeAnimator("This is a test");
		tea.addTrajectoryEnvelopes(trajEnvelopeRobot1, trajEnvelopeRobot2, trajEnvelopeRobot3, trajEnvelopeObstacle1);

	}
	
}
