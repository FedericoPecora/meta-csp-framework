package org.metacsp.examples.meta;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

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

public class VolvoCEDynamicCoordinationTest7 {
	
	
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
		
	public static HashMap<Integer,ArrayList<TrajectoryEnvelope>> createEnvelopes(TrajectoryEnvelopeSolver solver, String ... paths) {

		//XA15 footprint, 2.7 (w) x 6.6 (l)
		Coordinate frontLeft = new Coordinate(5.3, 1.35);
		Coordinate frontRight = new Coordinate(5.3, -1.35);
		Coordinate backRight = new Coordinate(-1.3, -1.35);
		Coordinate backLeft = new Coordinate(-1.3, 1.35);

		Variable[] tes = solver.createVariables(paths.length);
		Variable[] parkingStarts = solver.createVariables(paths.length);
		Variable[] parkingEnds = solver.createVariables(paths.length);
		
		ArrayList<AllenIntervalConstraint> consToAdd = new ArrayList<AllenIntervalConstraint>();
		
		HashMap<Integer,ArrayList<TrajectoryEnvelope>> ret = new HashMap<Integer, ArrayList<TrajectoryEnvelope>>();
		
		for (int i = 0; i < paths.length; i++) {
			TrajectoryEnvelope trajEnvelopeRobot_i = (TrajectoryEnvelope)tes[i];
			Trajectory trajRobot_i = new Trajectory(paths[i]);
			trajEnvelopeRobot_i.setFootprint(backLeft,backRight,frontLeft,frontRight);
			trajEnvelopeRobot_i.setTrajectory(trajRobot_i);
			trajEnvelopeRobot_i.setRobotID(i);
			
			TrajectoryEnvelope parkingStart_i = (TrajectoryEnvelope)parkingStarts[i];
			Pose parkingStartPose_i = trajEnvelopeRobot_i.getTrajectory().getPoseSteering()[0].getPose();
			Trajectory trajStart = new Trajectory(new Pose[] {parkingStartPose_i});
			parkingStart_i.setFootprint(backLeft,backRight,frontLeft,frontRight);
			parkingStart_i.setTrajectory(trajStart);
			parkingStart_i.setRefinable(false);

			TrajectoryEnvelope parkingEnd_i = (TrajectoryEnvelope)parkingEnds[i];
			Pose parkingEndPose_i = trajEnvelopeRobot_i.getTrajectory().getPoseSteering()[trajEnvelopeRobot_i.getTrajectory().getPoseSteering().length-1].getPose();
			Trajectory trajEnd = new Trajectory(new Pose[] {parkingEndPose_i});
			parkingEnd_i.setFootprint(backLeft,backRight,frontLeft,frontRight);
			parkingEnd_i.setTrajectory(trajEnd);
			parkingEnd_i.setRefinable(false);
			
			AllenIntervalConstraint parkingMeetsDriving = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets);
			parkingMeetsDriving.setFrom(parkingStart_i);
			parkingMeetsDriving.setTo(trajEnvelopeRobot_i);
			consToAdd.add(parkingMeetsDriving);
			
			AllenIntervalConstraint drivingMeetsParking = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets);
			drivingMeetsParking.setFrom(trajEnvelopeRobot_i);
			drivingMeetsParking.setTo(parkingEnd_i);
			consToAdd.add(drivingMeetsParking);
			
			AllenIntervalConstraint parkingEndForever = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Forever);
			parkingEndForever.setFrom(parkingEnd_i);
			parkingEndForever.setTo(parkingEnd_i);
			consToAdd.add(parkingEndForever);
			
			ArrayList<TrajectoryEnvelope> computedTes = new ArrayList<TrajectoryEnvelope>();
			computedTes.add(parkingStart_i);
			computedTes.add(trajEnvelopeRobot_i);
			computedTes.add(parkingEnd_i);
			ret.put(i, computedTes);
		}
		
		solver.addConstraints(consToAdd.toArray(new AllenIntervalConstraint[consToAdd.size()]));
		
		return ret;
	}
	
	public static void main(String[] args) {
		
		TrajectoryEnvelopeScheduler metaSolver = new TrajectoryEnvelopeScheduler(0, 100000);
		TrajectoryEnvelopeSolver solver = (TrajectoryEnvelopeSolver)metaSolver.getConstraintSolvers()[0];
		
		String[] paths = new String[] { "paths/newpath1.path", "paths/newpath2.path", "paths/newpath3.path"};
		HashMap<Integer,ArrayList<TrajectoryEnvelope>> tes = createEnvelopes(solver, paths);
		
		AllenIntervalConstraint robot1StartsAt = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(1500,APSPSolver.INF));
		robot1StartsAt.setFrom(tes.get(1).get(1));
		robot1StartsAt.setTo(tes.get(1).get(1));
		solver.addConstraint(robot1StartsAt);
				
		Map map = new Map(null, null);
		metaSolver.addMetaConstraint(map);
		
		TrajectoryEnvelopeAnimator tea = new TrajectoryEnvelopeAnimator("Coordinated trajecotries of three XA15s ");
		tea.setTrajectoryEnvelopeScheduler(metaSolver);
		tea.addTrajectoryEnvelopes(solver.getConstraintNetwork());

	}
	
}
