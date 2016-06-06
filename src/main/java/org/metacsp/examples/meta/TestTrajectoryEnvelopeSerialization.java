package org.metacsp.examples.meta;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

public class TestTrajectoryEnvelopeSerialization {
	
	
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
	
	private static void saveConstraintNetwork(ConstraintNetwork cn, String filename) {
		try {
			FileOutputStream out = new FileOutputStream(filename);
			ObjectOutputStream oos = new ObjectOutputStream(out);
			oos.writeObject(cn);
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }		
	}
	
	private static ConstraintNetwork loadConstraintNetwork(String filename) {
		try {
			FileInputStream in = new FileInputStream(filename);
			ObjectInputStream ois = new ObjectInputStream(in);
			ConstraintNetwork con = (ConstraintNetwork)ois.readObject();
			return con;
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
		catch (ClassNotFoundException e) { e.printStackTrace(); }
		return null;
	}
	
	private static void drawTrajectoryEnvelopes(ConstraintNetwork con, String title) {
		TrajectoryEnvelopeAnimator tea = new TrajectoryEnvelopeAnimator(title);
		ArrayList<TrajectoryEnvelope> toDraw = new ArrayList<TrajectoryEnvelope>();
		for (Variable v : con.getVariables()) {
			if (v instanceof TrajectoryEnvelope) {
				TrajectoryEnvelope te = (TrajectoryEnvelope)v;
				if (!te.hasSubEnvelopes()) {
					toDraw.add(te);
				}
			}
		}
		tea.addTrajectoryEnvelopes(toDraw.toArray(new TrajectoryEnvelope[toDraw.size()]));		
	}
	
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
		
		saveConstraintNetwork(solver.getConstraintNetwork(),"/home/fpa/constraintNetworkBeforeSolving.cn");
		ConstraintNetwork conBefore = loadConstraintNetwork("/home/fpa/constraintNetworkBeforeSolving.cn");
		drawTrajectoryEnvelopes(conBefore, "Trajectory Envelopes - before solving");
		
		ConstraintNetwork refined1 = metaSolver.refineTrajectoryEnvelopes();
		System.out.println("REFINED: "+  refined1);
		
		boolean solved = metaSolver.backtrack();
		System.out.println("Solved? " + solved);
		if (solved) System.out.println("Added resolvers:\n" + Arrays.toString(metaSolver.getAddedResolvers()));

		saveConstraintNetwork(solver.getConstraintNetwork(),"/home/fpa/constraintNetworkAfterSolving.cn");
		ConstraintNetwork conAfter = loadConstraintNetwork("/home/fpa/constraintNetworkAfterSolving.cn");
		drawTrajectoryEnvelopes(conAfter, "Trajectory Envelopes - after solving");

	}
	
}
