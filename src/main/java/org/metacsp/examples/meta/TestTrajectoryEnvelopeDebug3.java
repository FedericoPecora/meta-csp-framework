package org.metacsp.examples.meta;
import java.io.File;
import java.io.FilenameFilter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.meta.spatioTemporal.paths.Map;
import org.metacsp.meta.spatioTemporal.paths.TrajectoryEnvelopeScheduler;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.spatioTemporal.paths.Trajectory;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelope;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelopeSolver;
import org.metacsp.utility.UI.TrajectoryEnvelopeAnimator;

import com.vividsolutions.jts.geom.Coordinate;

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
	
	private static class PathSpecification implements Comparable {
		private int seqNo = -1;
		private String filename = null;
		private int robotID = -1;
		
		public PathSpecification(int seqNo, String filename, int robotID) {
			this.robotID = robotID;
			this.filename = filename;
			this.seqNo = seqNo;
		}

		public String getFilename() {
			return filename;
		}
		
		public int getSeqNo() {
			return seqNo;
		}
		
		public int getRobotID() {
			return robotID;
		}
		
		@Override
		public int compareTo(Object o) {
			if (!(o instanceof PathSpecification)) return 0;
			return this.getSeqNo()-((PathSpecification)o).getSeqNo();
		}
	}
	
	public static TrajectoryEnvelope[] makeTrajectoryEnvelopes(Map map, TrajectoryEnvelopeSolver solver, ArrayList<PathSpecification> pss) {
		// Footprint coordinates (reference point in (0,0), as in SemRob)
		Coordinate frontLeft = new Coordinate(8.100, 4.125);
		Coordinate frontRight = new Coordinate(8.100, -3.430);
		Coordinate backRight = new Coordinate(-6.920, -3.430);
		Coordinate backLeft = new Coordinate(-6.920, 4.125);

		Variable[] vars = solver.createVariables(pss.size()*3);
		TrajectoryEnvelope[] ret = new TrajectoryEnvelope[pss.size()];
		for (int i = 0; i < pss.size(); i++) {
			ret[i] = (TrajectoryEnvelope)vars[i];
			Trajectory traj = new Trajectory(pss.get(i).getFilename());
			ret[i].setFootprint(backLeft,backRight,frontLeft,frontRight);
			ret[i].setTrajectory(traj);
			ret[i].setRobotID(pss.get(i).getRobotID());
//			map.setUsage(ret[i]);
		}
		
		AllenIntervalConstraint[] meetsConstraints = new AllenIntervalConstraint[ret.length-1];		
		for (int i = 0; i < ret.length-1; i++) {
			AllenIntervalConstraint meets = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets);
			meets.setFrom(ret[i]);
			meets.setTo(ret[i+1]);
			meetsConstraints[i] = meets;
		}
		
		System.out.println("Added meets constraints for robot " + pss.get(0).getRobotID() + "? " + solver.addConstraints(meetsConstraints));
		
		return ret;	
	}
	
	public static void main(String[] args) {
		
		TrajectoryEnvelopeScheduler metaSolver = new TrajectoryEnvelopeScheduler(0, 10000000);
		TrajectoryEnvelopeSolver solver = (TrajectoryEnvelopeSolver)metaSolver.getConstraintSolvers()[0];

		File dir = new File("paths/debugPaths/npe");
		File[] files = dir.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.toLowerCase().endsWith(".path");
		    }
		});

		Map map = new Map(null, null);		
		metaSolver.addMetaConstraint(map);

		String prefix = "test";
		String separator = "_";
		String suffix = ".path";
		HashMap<Integer,ArrayList<PathSpecification>> paths = new HashMap<Integer, ArrayList<PathSpecification>>();
		for (File file : files) {
			String filename = file.getName();
			String fullPath = file.getPath();
			String pathID = filename.substring(prefix.length(), filename.indexOf(separator));
			String robotID = filename.substring(filename.indexOf(separator)+separator.length(),filename.indexOf(suffix));
			int pathIDInt = Integer.parseInt(pathID);
			int robotIDInt = Integer.parseInt(robotID);
			PathSpecification ps = new PathSpecification(pathIDInt, fullPath, robotIDInt);
			if (!paths.containsKey(robotIDInt)) paths.put(robotIDInt, new ArrayList<PathSpecification>());
			paths.get(robotIDInt).add(ps);
		}
		
		HashMap<Integer,TrajectoryEnvelope[]> tes = new HashMap<Integer, TrajectoryEnvelope[]>();
		for (Entry<Integer,ArrayList<PathSpecification>> e : paths.entrySet()) {
			Collections.sort(e.getValue());
			tes.put(e.getKey(), makeTrajectoryEnvelopes(map, solver, e.getValue()));
		}
		
		ConstraintNetwork refined1 = metaSolver.refineTrajectoryEnvelopes();
		System.out.println("REFINED: "+  refined1);

		boolean solved = metaSolver.backtrack();
		System.out.println("Solved? " + solved);
		if (solved) System.out.println("Added resolvers:\n" + Arrays.toString(metaSolver.getAddedResolvers()));

		ArrayList<TrajectoryEnvelope> allTes = new ArrayList<TrajectoryEnvelope>();
		for (Entry<Integer,TrajectoryEnvelope[]> e : tes.entrySet()) {
			for (TrajectoryEnvelope te : e.getValue()) allTes.add(te);
		}

		TrajectoryEnvelopeAnimator tea = new TrajectoryEnvelopeAnimator("This is a test");
		tea.addTrajectoryEnvelopes(allTes.toArray(new TrajectoryEnvelope[allTes.size()]));

//		JTSDrawingPanel.drawConstraintNetwork("Geometries after refinement",refined1);
//		ConstraintNetwork.draw(solver.getConstraintNetwork());

	}
	
}
