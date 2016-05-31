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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class TestTrajectoryEnvelopeDebug {
	
	
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
		
		TrajectoryEnvelopeScheduler metaSolver = new TrajectoryEnvelopeScheduler(0, 10000000);
		TrajectoryEnvelopeSolver solver = (TrajectoryEnvelopeSolver)metaSolver.getConstraintSolvers()[0];
		Variable[] vars = solver.createVariables(6);
		TrajectoryEnvelope var0 = (TrajectoryEnvelope)vars[0];
		TrajectoryEnvelope var1 = (TrajectoryEnvelope)vars[1];
		TrajectoryEnvelope var2 = (TrajectoryEnvelope)vars[2];
		TrajectoryEnvelope var3 = (TrajectoryEnvelope)vars[3];
		TrajectoryEnvelope var4 = (TrajectoryEnvelope)vars[4];
		TrajectoryEnvelope var5 = (TrajectoryEnvelope)vars[5];
		
		// Footprint coordinates (reference point in (0,0), as in SemRob)
		Coordinate frontLeft = new Coordinate(8.100, 4.125);
		Coordinate frontRight = new Coordinate(8.100, -3.430);
		Coordinate backRight = new Coordinate(-6.920, -3.430);
		Coordinate backLeft = new Coordinate(-6.920, 4.125);

		Trajectory traj0 = new Trajectory("paths/debugPaths/test0.path");
		var0.setFootprint(backLeft,backRight,frontLeft,frontRight);
		var0.setTrajectory(traj0);
		var0.setRobotID(1);

		Trajectory traj1 = new Trajectory("paths/debugPaths/test1.path");
		var1.setFootprint(backLeft,backRight,frontLeft,frontRight);
		var1.setTrajectory(traj1);
		var1.setRobotID(1);

		Trajectory traj2 = new Trajectory("paths/debugPaths/test2.path");
		var2.setFootprint(backLeft,backRight,frontLeft,frontRight);
		var2.setTrajectory(traj2);
		var2.setRobotID(1);

		Trajectory traj3 = new Trajectory("paths/debugPaths/test3.path");
		var3.setFootprint(backLeft,backRight,frontLeft,frontRight);
		var3.setTrajectory(traj3);
		var3.setRobotID(1);

		Trajectory traj4 = new Trajectory("paths/debugPaths/test4.path");
		var4.setFootprint(backLeft,backRight,frontLeft,frontRight);
		var4.setTrajectory(traj4);
		var4.setRobotID(1);

		Trajectory traj5 = new Trajectory("paths/debugPaths/test5.path");
		var5.setFootprint(backLeft,backRight,frontLeft,frontRight);
		var5.setTrajectory(traj5);
		var5.setRobotID(1);

		AllenIntervalConstraint meets0 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets);
		meets0.setFrom(var4);
		meets0.setTo(var5);
		
		AllenIntervalConstraint meets1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets);
		meets1.setFrom(var5);
		meets1.setTo(var2);
		
		AllenIntervalConstraint meets2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets);
		meets2.setFrom(var2);
		meets2.setTo(var1);
		
		AllenIntervalConstraint meets3 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets);
		meets3.setFrom(var1);
		meets3.setTo(var0);
		
		AllenIntervalConstraint meets4 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets);
		meets4.setFrom(var0);
		meets4.setTo(var3);
		
		System.out.println(solver.addConstraints(meets0,meets1,meets2,meets3,meets4));
		
		Map map = new Map(null, null);		
		metaSolver.addMetaConstraint(map);
		
		ConstraintNetwork refined1 = metaSolver.refineTrajectoryEnvelopes();
		System.out.println("REFINED 1: "+  refined1);
//
//		ConstraintNetwork refined2 = metaSolver.refineTrajectoryEnvelopes();
//		System.out.println("REFINED 2: "+  refined2);
//		
//		boolean solved = metaSolver.backtrack();
//		System.out.println("Solved? " + solved);
//		if (solved) System.out.println("Added resolvers:\n" + Arrays.toString(metaSolver.getAddedResolvers()));

		TrajectoryEnvelopeAnimator tea = new TrajectoryEnvelopeAnimator("This is a test");
		tea.addTrajectoryEnvelopes(var0,var1,var2,var3,var4,var5);

//		JTSDrawingPanel.drawConstraintNetwork("Geometries after refinement",refined1);
//		ConstraintNetwork.draw(solver.getConstraintNetwork());

	}
	
}
