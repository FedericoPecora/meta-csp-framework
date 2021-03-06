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

public class TestTrajectoryEnvelopeAsObstacle {
	
	
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
		TrajectoryEnvelope var0 = (TrajectoryEnvelope)vars[0];
		TrajectoryEnvelope var1 = (TrajectoryEnvelope)vars[1];
		TrajectoryEnvelope var2 = (TrajectoryEnvelope)vars[2];
		TrajectoryEnvelope var3 = (TrajectoryEnvelope)vars[3];
		
		Trajectory traj0 = new Trajectory("paths/newpath1.path");
		var0.setFootprint(1.3, 3.5, 0.0, 0.0);
		var0.setTrajectory(traj0);

		Trajectory traj1 = new Trajectory("paths/newpath2.path");
		var1.setFootprint(1.3, 3.5, 0.0, 0.0);
		var1.setTrajectory(traj1);

		Trajectory traj2 = new Trajectory("paths/newpath3.path");
		var2.setFootprint(1.3, 3.5, 0.0, 0.0);
		var2.setTrajectory(traj2);
		
		// This is an obstacle!
		var3.setFootprint(1, 1, 0, 0);
		Pose obsPosition = new Pose(-10, -2, 0.0);
		Trajectory traj3 = new Trajectory(new Pose[] {obsPosition});
		var3.setTrajectory(traj3);
		var3.setRefinable(false);
		
		// When the obstacle should appear
		AllenIntervalConstraint obstacleRelease = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(5000,5000));
		obstacleRelease.setFrom(var3);
		obstacleRelease.setTo(var3);
		AllenIntervalConstraint obstacleDeadline = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(solver.getHorizon(),solver.getHorizon()));
		obstacleDeadline.setFrom(var3);
		obstacleDeadline.setTo(var3);
		solver.addConstraints(obstacleRelease,obstacleDeadline);
		
		var0.setRobotID(1);
		var1.setRobotID(2);
		var2.setRobotID(3);
		
		System.out.println(var0 + " has domain " + var0.getDomain());
		System.out.println(var1 + " has domain " + var1.getDomain());
		System.out.println(var2 + " has domain " + var2.getDomain());
		System.out.println(var3 + " has domain " + var3.getDomain());
				
		Map map = new Map(null, null);		
		metaSolver.addMetaConstraint(map);
		
		ConstraintNetwork refined1 = metaSolver.refineTrajectoryEnvelopes();
		System.out.println("REFINED 1: "+  refined1);

		ConstraintNetwork refined2 = metaSolver.refineTrajectoryEnvelopes();
		System.out.println("REFINED 2: "+  refined2);
		
		boolean solved = metaSolver.backtrack();
		System.out.println("Solved? " + solved);
		if (solved) System.out.println("Added resolvers:\n" + Arrays.toString(metaSolver.getAddedResolvers()));

		// Try a geofence-like polygon...
		Coordinate[] gfence = new Coordinate[12];
		gfence[0] = new Coordinate(-40.33803932001092,-8.154954385842458);
		gfence[1] = new Coordinate(-34.64588573670587,-18.70218014314299);
		gfence[2] = new Coordinate(-22.089664597062377,-33.43481294699136);
		gfence[3] = new Coordinate(-10.705357430452281,-39.46179909402023);
		gfence[4] = new Coordinate(10.389094084148788,-36.61572230236771);
		gfence[5] = new Coordinate(19.4295733046921,-25.063998853895693);
		gfence[6] = new Coordinate(22.777898941930374,-8.154954385842458);
		gfence[7] = new Coordinate(7.375601010634355,15.45074135668731);
		gfence[8] = new Coordinate(-15.058180758862022,20.47322981254471);
		gfence[9] = new Coordinate(-33.30655548181056,19.63614840323514);
		gfence[10] = new Coordinate(-42.01220213863005,5.405764444972519);
		gfence[11] = new Coordinate(-40.33803932001092,-8.154954385842458);
		GeometryFactory gf = new GeometryFactory();
		Geometry geofence = gf.createPolygon(gfence);
		
		TrajectoryEnvelopeAnimator tea = new TrajectoryEnvelopeAnimator("This is a test");
		tea.addTrajectoryEnvelopes(var0, var1, var2, var3);
		tea.addMarkers(new String[] {"DT1","DT2","DT3"}, new Pose[] {var0.getTrajectory().getPose()[0], var1.getTrajectory().getPose()[0], var2.getTrajectory().getPose()[0]});
		tea.addExtraGeometries(geofence);

	}
	
}
