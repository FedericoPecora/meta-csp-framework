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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class TestTrajectoryEnvelopeScheduler {
	
	public static void main(String[] args) {
		
		TrajectoryEnvelopeScheduler metaSolver = new TrajectoryEnvelopeScheduler(0, 100000);
		TrajectoryEnvelopeSolver solver = (TrajectoryEnvelopeSolver)metaSolver.getConstraintSolvers()[0];
		Variable[] vars = solver.createVariables(2);
		TrajectoryEnvelope var0 = (TrajectoryEnvelope)vars[0];
		TrajectoryEnvelope var1 = (TrajectoryEnvelope)vars[1];
		
		Trajectory traj0 = new Trajectory("/home/fpa/paths/path1.path");
		var0.setTrajectory(traj0);

		Trajectory traj1 = new Trajectory("/home/fpa/paths/path3.path");
		var1.setTrajectory(traj1);
		
		var0.setRobotID(1);
		var1.setRobotID(2);
		
		System.out.println(var0 + " has domain " + var0.getDomain());
		System.out.println(var1 + " has domain " + var1.getDomain());
		
//		boolean addedDurations = solver.addConstraints(var0.getDurationConstriant(), var1.getDurationConstriant());
//		System.out.println("Added durations? " + addedDurations);
		
//		//Visualize intersection
//		System.out.println(Arrays.toString(((DE9IMRelationSolver)solver.getConstraintSolvers()[1]).getAllImplicitRelations()));
//		Geometry intersection = ((GeometricShapeDomain)var0.getEnvelopeVariable().getDomain()).getGeometry().intersection(((GeometricShapeDomain)var1.getEnvelopeVariable().getDomain()).getGeometry());
//		JTSDrawingPanel.drawVariables(var0.getEnvelopeVariable(), var1.getEnvelopeVariable(), var0.getReferencePathVariable(), var1.getReferencePathVariable(),intersection);
		
//		double[] dts0 = traj0.getDts();
//		for (int i = 1; i < dts0.length; i++) {
//			System.out.println(""+(dts0[i]-dts0[i-1]));
//		}
//		System.out.println("Trajectory 0 DTs: " + Arrays.toString(traj0.getDts()));
//		System.out.println("Trajectory 1 DTs: " + Arrays.toString(traj1.getDts()));

		Map map = new Map(null, null);		
		metaSolver.addMetaConstraint(map);

		ConstraintNetwork refined1 = metaSolver.refineTrajectoryEnvelopes();
		System.out.println("REFINED 1: "+  refined1);

		ConstraintNetwork refined2 = metaSolver.refineTrajectoryEnvelopes();
		System.out.println("REFINED 2: "+  refined2);

		double[] var0DTs = ((TrajectoryEnvelope)var0).getTrajectory().getDts();
		double[] var0CTs = ((TrajectoryEnvelope)var0).getCTs();
		double[] var1DTs = ((TrajectoryEnvelope)var1).getTrajectory().getDts();
		double[] var1CTs = ((TrajectoryEnvelope)var1).getCTs();

		DecimalFormat df = new DecimalFormat("#0.00");
		df.setRoundingMode(RoundingMode.HALF_DOWN);
		System.out.println("====================\n== BEFORE SOLVING ==\n====================");
		System.out.println("---------------------------------------\n" + var0 + " DTs and CTs\n---------------------------------------");
		for (int i = 0; i < var0DTs.length; i++) {
			System.out.println(i + ": " + df.format(var0DTs[i]) + " \t " + df.format(var0CTs[i]));
		}
		System.out.println("---------------------------------------\n" + var1 + " DTs and CTs\n---------------------------------------");
		for (int i = 0; i < var1DTs.length; i++) {
			System.out.println(i + ": " + df.format(var1DTs[i]) + " \t " + df.format(var1CTs[i]));
		}
		
		boolean solved = metaSolver.backtrack();
		System.out.println("Solved? " + solved);
		if (solved) System.out.println("Added resolvers:\n" + Arrays.toString(metaSolver.getAddedResolvers()));

		var0CTs = ((TrajectoryEnvelope)var0).getCTs();
		var1CTs = ((TrajectoryEnvelope)var1).getCTs();
		
		System.out.println("===================\n== AFTER SOLVING ==\n===================");
		System.out.println("---------------------------------------\n" + var0 + " DTs and CTs\n---------------------------------------");
		for (int i = 0; i < var0DTs.length; i++) {
			System.out.println(i + ": " + df.format(var0DTs[i]) + " \t " + df.format(var0CTs[i]));
		}
		System.out.println("---------------------------------------\n" + var1 + " DTs and CTs\n---------------------------------------");
		for (int i = 0; i < var1DTs.length; i++) {
			System.out.println(i + ": " + df.format(var1DTs[i]) + " \t " + df.format(var1CTs[i]));
		}

		JTSDrawingPanel.drawConstraintNetwork(refined1);

		
		
//		System.out.println(var0 + " DTs: " + Arrays.toString(((TrajectoryEnvelope)var0).getTrajectory().getDts()));
//		System.out.println(var0 + " CTs: " + Arrays.toString(((TrajectoryEnvelope)var0).getCTs()));
//		System.out.println(var1 + " DTs: " + Arrays.toString(((TrajectoryEnvelope)var1).getTrajectory().getDts()));
//		System.out.println(var1 + " CTs: " + Arrays.toString(((TrajectoryEnvelope)var1).getCTs()));

//		for (Variable v : refined1.getVariables()) {
//			System.out.println(v + " DTs: " + Arrays.toString(((TrajectoryEnvelope)v).getTrajectory().getDts()));
//			System.out.println(v + " CTs: " + Arrays.toString(((TrajectoryEnvelope)v).getCTs()));
//		}	
	}
	
}
