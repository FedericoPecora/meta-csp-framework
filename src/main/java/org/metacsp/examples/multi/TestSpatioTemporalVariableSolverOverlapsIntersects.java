package org.metacsp.examples.multi;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import org.metacsp.framework.Variable;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.spatial.DE9IM.DE9IMRelation;
import org.metacsp.multi.spatial.DE9IM.DE9IMRelationSolver;
import org.metacsp.multi.spatial.DE9IM.LineStringDomain;
import org.metacsp.multi.spatial.DE9IM.PolygonalDomain;
import org.metacsp.multi.spatioTemporal.SpatioTemporalVariable;
import org.metacsp.multi.spatioTemporal.SpatioTemporalVariableSolver;
import org.metacsp.multi.spatioTemporal.paths.Trajectory;
import org.metacsp.multi.spatioTemporal.paths.PoseSteering;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelope;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelopeSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.UI.JTSDrawingPanel;

import com.vividsolutions.jts.geom.Coordinate;

public class TestSpatioTemporalVariableSolverOverlapsIntersects {
	
	public static void main(String[] args) {
		
		TrajectoryEnvelopeSolver solver = new TrajectoryEnvelopeSolver(0, 1000);
		Variable[] vars = solver.createVariables(2);
		TrajectoryEnvelope var0 = (TrajectoryEnvelope)vars[0];
		TrajectoryEnvelope var1 = (TrajectoryEnvelope)vars[1];
		
		Trajectory path0 = new Trajectory("/home/fpa/paths/path1.path");
		var0.setTrajectory(path0);

		Trajectory path1 = new Trajectory("/home/fpa/paths/path3.path");
		var1.setTrajectory(path1);
		
		AllenIntervalConstraint relVar0 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(10,10));
		relVar0.setFrom(var0);
		relVar0.setTo(var0);

		AllenIntervalConstraint deadVar0 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(20,20));
		deadVar0.setFrom(var0);
		deadVar0.setTo(var0);
		
		AllenIntervalConstraint relVar1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(15,15));
		relVar1.setFrom(var1);
		relVar1.setTo(var1);

		AllenIntervalConstraint deadVar1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(25,25));
		deadVar1.setFrom(var1);
		deadVar1.setTo(var1);

		System.out.println("Added constraints? " + solver.addConstraints(relVar0,relVar1,deadVar0,deadVar1));

		System.out.println("path 0 has " + var0.getDomain());
		System.out.println("path 1 has " + var1.getDomain());
		
		System.out.println(Arrays.toString(((DE9IMRelationSolver)solver.getConstraintSolvers()[1]).getAllImplicitRelations()));
		JTSDrawingPanel.drawVariables(var0.getEnvelopeVariable(), var1.getEnvelopeVariable(), var0.getReferencePathVariable(), var1.getReferencePathVariable());
//		JTSDrawingPanel.drawVariables(var0.getEnvelopeVariable());
//		JTSDrawingPanel.drawVariables(var1.getEnvelopeVariable());
		
	}
}
