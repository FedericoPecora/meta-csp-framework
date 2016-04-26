package org.metacsp.examples.meta;
import java.util.ArrayList;
import java.util.Arrays;

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
		
		System.out.println("Trajectory 0 has " + var0.getDomain());
		System.out.println("Trajectory 1 has " + var1.getDomain());
		
		boolean addedDurations = solver.addConstraints(var0.getDurationConstriant(), var1.getDurationConstriant());
		System.out.println("Added durations? " + addedDurations);
		
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

		TrajectoryEnvelope[] refined = computeIntersection(var0, var1, solver);
		Map map = new Map(null, null);
		map.setUsage(refined);
		metaSolver.addMetaConstraint(map);
		
		boolean solved = metaSolver.backtrack();
		System.out.println("Solved? " + solved);
		if (solved) System.out.println("Added resolvers:\n" + Arrays.toString(metaSolver.getAddedResolvers()));

		JTSDrawingPanel.drawVariables(refined);
		
		for (TrajectoryEnvelope te : refined) {
			System.out.println("Trajectory DTs: " + Arrays.toString(te.getTrajectory().getDts()));
		}
		
	}
	
	private static TrajectoryEnvelope[] computeIntersection(TrajectoryEnvelope var1, TrajectoryEnvelope var2, TrajectoryEnvelopeSolver solver) {
		TrajectoryEnvelope[] ret = new TrajectoryEnvelope[6];
		GeometryFactory gf = new GeometryFactory();
		Geometry se1 = ((GeometricShapeDomain)var1.getEnvelopeVariable().getDomain()).getGeometry();
		Geometry se2 = ((GeometricShapeDomain)var2.getEnvelopeVariable().getDomain()).getGeometry();
		Geometry intersectionse1se2 = se1.intersection(se2);
		ArrayList<PoseSteering> var1sec1 = new ArrayList<PoseSteering>();
		ArrayList<PoseSteering> var1sec2 = new ArrayList<PoseSteering>();
		ArrayList<PoseSteering> var1sec3 = new ArrayList<PoseSteering>();
		ArrayList<PoseSteering> var2sec1 = new ArrayList<PoseSteering>();
		ArrayList<PoseSteering> var2sec2 = new ArrayList<PoseSteering>();
		ArrayList<PoseSteering> var2sec3 = new ArrayList<PoseSteering>();
		for (int i = 0; i < var1.getPathLength(); i++) {
			Coordinate coord = var1.getTrajectory().getPositions()[i];
			PoseSteering ps = var1.getTrajectory().getPoseSteering()[i];
			Point point = gf.createPoint(coord);
			if (!intersectionse1se2.contains(point) && var1sec2.isEmpty()) {
				var1sec1.add(ps);
			}
			else if (intersectionse1se2.contains(point)) {
				var1sec2.add(ps);
			}
			else if (!intersectionse1se2.contains(point) && !var1sec2.isEmpty()) {
				var1sec3.add(ps);
			}
		}
		//add a little to the intersection
		for (int i = 0; i < 2; i++) {
			var1sec2.add(var1sec3.get(0));
			var1sec3.remove(0);
		}

		for (int i = 0; i < var2.getPathLength(); i++) {
			Coordinate coord = var2.getTrajectory().getPositions()[i];
			PoseSteering ps = var2.getTrajectory().getPoseSteering()[i];
			Point point = gf.createPoint(coord);
			if (!intersectionse1se2.contains(point) && var2sec2.isEmpty()) {
				var2sec1.add(ps);
			}
			else if (intersectionse1se2.contains(point)) {
				var2sec2.add(ps);
			}
			else if (!intersectionse1se2.contains(point) && !var2sec2.isEmpty()) {
				var2sec3.add(ps);
			}
		}
		//add a little to the intersection
		for (int i = 0; i < 2; i++) {
			var2sec2.add(var2sec3.get(0));
			var2sec3.remove(0);
		}
		
		System.out.println("Lengths: " + var1sec1.size() + " " + var1sec2.size() + " " + var1sec3.size() );

		Trajectory newPath1sec1 = new Trajectory(var1sec1.toArray(new PoseSteering[var1sec1.size()]));
		Trajectory newPath1sec2 = new Trajectory(var1sec2.toArray(new PoseSteering[var1sec2.size()]));
		Trajectory newPath1sec3 = new Trajectory(var1sec3.toArray(new PoseSteering[var1sec3.size()]));
		Trajectory newPath2sec1 = new Trajectory(var2sec1.toArray(new PoseSteering[var2sec1.size()]));
		Trajectory newPath2sec2 = new Trajectory(var2sec2.toArray(new PoseSteering[var2sec2.size()]));
		Trajectory newPath2sec3 = new Trajectory(var2sec3.toArray(new PoseSteering[var2sec3.size()]));

		Variable[] newVars = solver.createVariables(6);
		TrajectoryEnvelope newVar1sec1 = (TrajectoryEnvelope)newVars[0];
		TrajectoryEnvelope newVar1sec2 = (TrajectoryEnvelope)newVars[1];
		TrajectoryEnvelope newVar1sec3 = (TrajectoryEnvelope)newVars[2];
		TrajectoryEnvelope newVar2sec1 = (TrajectoryEnvelope)newVars[3];
		TrajectoryEnvelope newVar2sec2 = (TrajectoryEnvelope)newVars[4];
		TrajectoryEnvelope newVar2sec3 = (TrajectoryEnvelope)newVars[5];
		newVar1sec1.setTrajectory(newPath1sec1);
		newVar1sec2.setTrajectory(newPath1sec2);
		newVar1sec3.setTrajectory(newPath1sec3);
		newVar2sec1.setTrajectory(newPath2sec1);
		newVar2sec2.setTrajectory(newPath2sec2);
		newVar2sec3.setTrajectory(newPath2sec3);
		
		ret[0] = newVar1sec1;
		ret[1] = newVar1sec2;
		ret[2] = newVar1sec3;
		ret[3] = newVar2sec1;
		ret[4] = newVar2sec2;
		ret[5] = newVar2sec3;
		
		AllenIntervalConstraint starts1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Starts);
		starts1.setFrom(newVar1sec1);
		starts1.setTo(var1);		
		AllenIntervalConstraint finishes1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Finishes);
		finishes1.setFrom(newVar1sec3);
		finishes1.setTo(var1);
		AllenIntervalConstraint meets1sec1sec2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets);
		meets1sec1sec2.setFrom(newVar1sec1);
		meets1sec1sec2.setTo(newVar1sec2);
		AllenIntervalConstraint meets1sec2sec3 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets);
		meets1sec2sec3.setFrom(newVar1sec2);
		meets1sec2sec3.setTo(newVar1sec3);
		
		AllenIntervalConstraint starts2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Starts);
		starts2.setFrom(newVar2sec1);
		starts2.setTo(var2);		
		AllenIntervalConstraint finishes2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Finishes);
		finishes2.setFrom(newVar2sec3);
		finishes2.setTo(var2);
		AllenIntervalConstraint meets2sec1sec2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets);
		meets2sec1sec2.setFrom(newVar2sec1);
		meets2sec1sec2.setTo(newVar2sec2);
		AllenIntervalConstraint meets2sec2sec3 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets);
		meets2sec2sec3.setFrom(newVar2sec2);
		meets2sec2sec3.setTo(newVar2sec3);
		
		System.out.println("ADDED???? " + solver.addConstraints(starts1,starts2,finishes1,finishes2,meets1sec1sec2,meets1sec2sec3,meets2sec1sec2,meets2sec2sec3));

		return ret;

	}
}
