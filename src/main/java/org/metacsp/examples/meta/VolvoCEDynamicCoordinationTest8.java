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

public class VolvoCEDynamicCoordinationTest8 {
	
	
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
		
//		String[] paths = new String[] { "paths/newpath1.path", "paths/newpath2.path", "paths/newpath3.path"};
//		HashMap<Integer,ArrayList<TrajectoryEnvelope>> tes = solver.createEnvelopes(paths);
//		
//		AllenIntervalConstraint robot1StartsAt = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(1500,APSPSolver.INF));
//		robot1StartsAt.setFrom(tes.get(1).get(1));
//		robot1StartsAt.setTo(tes.get(1).get(1));
//		solver.addConstraint(robot1StartsAt);
				
		Map map = new Map(null, null);
		metaSolver.addMetaConstraint(map);
		
		TrajectoryEnvelopeAnimator tea = new TrajectoryEnvelopeAnimator("Coordinated trajecotries of three XA15s ");
		tea.setTrajectoryEnvelopeScheduler(metaSolver);
		tea.addTrajectoryEnvelopes(solver.getConstraintNetwork());

	}
	
}
