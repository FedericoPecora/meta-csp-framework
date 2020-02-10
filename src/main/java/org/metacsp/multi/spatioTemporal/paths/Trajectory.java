package org.metacsp.multi.spatioTemporal.paths;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.Gaussian;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * Represents a trajectory, namely, a path (sequence of {@link PoseSteering}s) plus a temporal profile.
 * The latter is represented as fixed durations between between successive {@link PoseSteering}s along the path.
 * 
 * @author Federico Pecora
 *
 */
public class Trajectory implements Serializable {
	
	private static final long serialVersionUID = 1314671681021590724L;
	private PoseSteering[] psa;
	private double[] dts;
	private static double MAX_SPEED = 3.0;
	private static double MAX_ACCELERATION = 0.3;
	private int sequenceNumberStart = -1;
	private int sequenceNumberEnd = -1;
	
	/**
	 * Create a new {@link Trajectory} given a list of {@link Pose}s. The
	 * temporal profile of the trajectory is computed via naive numeric resolution
	 * assuming constant acceleration and a maximum speed.
	 * @param pa The list of {@link Pose}s used to build the path.
	 */
	public Trajectory(Pose[] pa) {
		this.psa = new PoseSteering[pa.length];
		for (int i = 0; i < pa.length; i++) {
			PoseSteering ps = new PoseSteering(pa[i], 0.0);
			this.psa[i] = ps;
		}
		this.dts = new double[psa.length];
		this.updateDts();
		this.sequenceNumberStart = 0;
		this.sequenceNumberEnd = pa.length-1;
	}
	
	/**
	 * Update the sequence numbers of this trajectory (used when this is
	 * a trajectory of a sub-trajectory envelope).
	 * @param start The start sequence number in the super {@link TrajectoryEnvelope}.
	 * @param end The end sequence number in the super {@link TrajectoryEnvelope}.
	 */
	public void updateSequenceNumbers(int start, int end) {
		this.sequenceNumberStart = start;
		this.sequenceNumberEnd = end;
	}
	
	/**
	 * Get the start sequence number of this trajectory.
	 * @return The start sequence number of this trajectory.
	 */
	public int getSequenceNumberStart() {
		return this.sequenceNumberStart;
	}

	/**
	 * Get the end sequence number of this trajectory.
	 * @return The end sequence number of this trajectory.
	 */
	public int getSequenceNumberEnd() {
		return this.sequenceNumberEnd;
	}

	/**
	 * Create a new {@link Trajectory} given a list of {@link PoseSteering}s. The
	 * temporal profile of the trajectory is computed via naive numeric resolution
	 * assuming constant acceleration and a maximum speed.
	 * @param psa The list of {@link PoseSteering}s used to build the path.
	 */
	public Trajectory(PoseSteering[] psa) {
		this.psa = psa;
		this.dts = new double[psa.length];
		this.updateDts();
		this.sequenceNumberStart = 0;
		this.sequenceNumberEnd = psa.length-1;
	}

	/**
	 * Create a new {@link Trajectory} given a list of {@link PoseSteering}s and
	 * the temporal profile of the {@link Trajectory}.
	 * @param pa The list of {@link PoseSteering}s used to build the path.
	 * @param dts The temporal profile of the {@link Trajectory} (times between successive points
	 * along the path). The zero-th element is assumed to represent the time to transition from the
	 * first point along the path to the second point along the path. The last element of this parameter
	 * should be zero.
	 */
	public Trajectory(Pose[] pa, double[] dts) {
		this.psa = new PoseSteering[pa.length];
		for (int i = 0; i < pa.length; i++) {
			PoseSteering ps = new PoseSteering(pa[i], 0.0);
			this.psa[i] = ps;
		}
		this.dts = new double[psa.length];
		this.dts = dts;
		this.sequenceNumberStart = 0;
		this.sequenceNumberEnd = pa.length-1;
	}
	
	/**
	 * Create a new {@link Trajectory} given a list of {@link PoseSteering}s and
	 * the temporal profile of the {@link Trajectory}.
	 * @param psa The list of {@link PoseSteering}s used to build the path.
	 * @param dts The temporal profile of the {@link Trajectory} (times between successive points
	 * along the path). The zero-th element is assumed to represent the time to transition from the
	 * first point along the path to the second point along the path. The last element of this parameter
	 * should be zero.
	 */
	public Trajectory(PoseSteering[] psa, double[] dts) {
		this.psa = psa;
		this.dts = new double[psa.length];
		this.dts = dts;
		this.sequenceNumberStart = 0;
		this.sequenceNumberEnd = psa.length-1;
	}
	
	/**
	 * Create a new {@link Trajectory} given list of {@link PoseSteering}s that is read
	 * from a file. The temporal profile of the trajectory is computed via naive numeric resolution
	 * assuming constant acceleration and a maximum speed.
	 * @param fileName The path of a file pointing to the list of {@link PoseSteering}s used to build the path.
	 */	
	public Trajectory(String fileName) {
		this.psa = readPath(fileName);
		this.dts = new double[psa.length];
		this.updateDts();
		this.sequenceNumberStart = 0;
		this.sequenceNumberEnd = psa.length-1;
	}
	
	/**
	 * Create a new {@link Trajectory} given a list files containing paths.
	 * The temporal profile of the trajectory is computed via naive numeric resolution
	 * assuming constant acceleration and a maximum speed.
	 * @param fileNames The paths of files, each containing a list of {@link PoseSteering}s used to build the path.
	 */	
	public Trajectory(String ... fileNames) {
		ArrayList<PoseSteering> combined = new ArrayList<PoseSteering>();
		for (int i = 0; i < fileNames.length; i++) {
			PoseSteering[] onePath = readPath(fileNames[i]);
			if (i == 0) combined.add(onePath[0]);
			for (int j = 1; j < onePath.length; j++) {
				combined.add(onePath[j]);
			}
		}
		this.psa = combined.toArray(new PoseSteering[combined.size()]);
		this.dts = new double[psa.length];
		this.updateDts();
		this.sequenceNumberStart = 0;
		this.sequenceNumberEnd = psa.length-1;
	}
	
	/**
	 * Get the length in meters of this {@link Trajectory}'s path. This is the sum of distances between path poses.
	 * @return The length in meters of this {@link Trajectory}'s path. This is the sum of distances between path poses.
	 */
	public double getPathLength() {
		double length = 0.0;
		for (int i = 0; i < this.getPoseSteering().length-1; i++) {
			length += this.getPoseSteering()[i].getPose().distanceTo(this.getPoseSteering()[i+1].getPose());
		}
		return length;
	}
	
	/**
	 * Get the temporal profile of this {@link Trajectory}.
	 * @return The temporal profile of this {@link Trajectory}.
	 */
	public double[] getDTs() {
		return dts;
	}

	/**
	 * Get the temporal profile of a portion of this this {@link Trajectory}.
	 * @param from The index of the starting path point.
	 * @param to The index of the last path point.
	 * @return The temporal profile of a portion of this this {@link Trajectory}.
	 */
	public double[] getDts(int from, int to) {
		double[] ret = new double[to-from];
		for (int i = from; i < to; i++) {
			ret[i-from] = this.dts[i];
		}
		return ret;
	}

	private double getConstantAcceleration(double percentComplete) {
//		if (percentComplete <= 0.5) return Gaussian.Phi(percentComplete, 0.25, 0.3)*MAX_ACCELERATION;
//		return -Gaussian.Phi(percentComplete, 0.75, 0.3)*MAX_ACCELERATION;
		if (percentComplete <= 0.25) return MAX_ACCELERATION;
		if (percentComplete <= 0.78) return 0.0;
		return -MAX_ACCELERATION;
	}

	private double getLinearAcceleration(double percentComplete) {
//		if (percentComplete <= 0.5) return Gaussian.Phi(percentComplete, 0.25, 0.3)*MAX_ACCELERATION;
//		return -Gaussian.Phi(percentComplete, 0.75, 0.3)*MAX_ACCELERATION;
		if (percentComplete <= 0.5) return 2*percentComplete*MAX_ACCELERATION;
		return -(percentComplete-0.5)*MAX_ACCELERATION;
	}

	private double getAcceleration(double percentComplete) {
//		return getLinearAcceleration(percentComplete);
		return getConstantAcceleration(percentComplete);
	}
	
	/**
	 * Get the sequence number of the path point that is closest to a 
	 * given coordinate.
	 * @param coord The coordinate to which the closest sequence number should be found.
	 * @return The sequence number of the path point that is closest to the given coordinate.
	 */
	public int getSequenceNumber(Coordinate coord) {
		int minIndex = 0;
		double minDist = Double.MAX_VALUE;
		for (int i = 0; i < this.getPositions().length; i++) {
			if (this.getPositions()[i].distance(coord) < minDist) {
				minDist = this.getPositions()[i].distance(coord);
				minIndex = i;
			}
		}
		return minIndex;
	}

	/**
	 * Get an estimate of the time left to move on this {@link Trajectory}
	 * given the current position. The estimate is based on this {@link Trajectory}'s
	 * temporal profile.
	 * @param positionNow The position from which the estimate should be computed.
	 * @return An estimate of the time left to move on this {@link Trajectory}
	 * given the current position.
	 */
	public double getTimeLeftEstimate(Coordinate positionNow) {
		int minIndex = 0;
		double minDist = Double.MAX_VALUE;
		for (int i = 0; i < this.getPositions().length; i++) {
			if (this.getPositions()[i].distance(positionNow) < minDist) {
				minDist = this.getPositions()[i].distance(positionNow);
				minIndex = i;
			}
		}
		return getTimeLeftEstimate(minIndex);
	}

	
	/**
	 * Get an estimate of the time left to move on this {@link Trajectory}
	 * between two path indices. The estimate is based on this {@link Trajectory}'s
	 * temporal profile.
	 * @param sequenceNumNow The path index from which the estimate should be computed.
	 * @param sequenceNumTo The path index to which the estimate should be computed.
	 * @return An estimate of the time left to move on this {@link Trajectory}
	 * given the current path index.
	 */
	public double getTimeToEstimate(int sequenceNumNow, int sequenceNumTo) {
		double timeCounter = 0.0;
		for (int i = sequenceNumNow-this.sequenceNumberStart; i <= sequenceNumTo; i++) {
			timeCounter += this.getDTs()[i];
		}
		return timeCounter;
	}
	
	/**
	 * Get an estimate of the time left to move on this {@link Trajectory}
	 * given the current path index. The estimate is based on this {@link Trajectory}'s
	 * temporal profile.
	 * @param sequenceNum The path index from which the estimate should be computed.
	 * @return An estimate of the time left to move on this {@link Trajectory}
	 * given the current path index.
	 */
	public double getTimeLeftEstimate(int sequenceNum) {
		double timeCounter = 0.0;
		for (int i = sequenceNum-this.sequenceNumberStart; i < this.getDTs().length; i++) {
			timeCounter += this.getDTs()[i];
		}
		return timeCounter;
	}
	
//	public double getTimeLeftEstimate(int sequenceNum, int endIndex) {
//		double timeCounter = 0.0;
//		for (int i = sequenceNum; i < endIndex; i++) {
//			timeCounter += this.getDTs()[i];
//		}
//		return timeCounter;
//	}

	private void updateDts() {
		double dt = 0.1;
		ArrayList<Double> u = new ArrayList<Double>();
		ArrayList<Double> s = new ArrayList<Double>();
		//u = u + a * dt;
		//s = s + u * dt;
		s.add(0.0);
		u.add(0.0);
		double totDistance = 0.0;
		for (int i = 1; i < dts.length; i++) {
			Coordinate prev = getPositions()[i-1];
			Coordinate current = getPositions()[i];
			totDistance += prev.distance(current);
		}
		while (s.get(s.size()-1) < totDistance) {
			double percentComplete = 0.01;
			double newPercent = s.get(s.size()-1)/totDistance;
			if (newPercent > percentComplete) percentComplete = newPercent;
//			System.out.println(percentComplete*100 + "% --> " + getAcceleration(percentComplete));
			u.add(u.get(u.size()-1)+getAcceleration(percentComplete)*dt);
			s.add(s.get(s.size()-1)+u.get(u.size()-1)*dt);
		}
		totDistance = 0.0;
		dts[0] = 0.0;
		double prevSum = 0.0;
		for (int i = 1; i < dts.length; i++) {
			Coordinate prev = getPositions()[i-1];
			Coordinate current = getPositions()[i];
			totDistance += prev.distance(current);
			int countDts = 0;
			for (int j = 0; j < s.size(); j++) {
				if (s.get(j) > totDistance) {
					countDts = j-1;
					break;
				}
			}
			prevSum += dts[i-1];
			dts[i] = Math.max(countDts*dt-prevSum,0.001);
		}
	}
	
	/**
	 * Set the minimum transition times between path poses
	 * of this {@link Trajectory}. This is essentially the fastest
	 * time profile of this {@link Trajectory}.
	 * @param dts A vector containing the minimum transition times between path poses
	 * of this {@link Trajectory}. Values are assumed to be in seconds. 
	 */
	public void setDTs(double[] dts) {
		this.dts = dts;
	}
	
//	public double getMinTraversalTime() {
//		return dts[dts.length-1];
//	}

	/**
	 * Get the path (list of {@link PoseSteering}s) of this {@link Trajectory}.
	 * @return The path (list of {@link PoseSteering}s) of this {@link Trajectory}.
	 */
	public PoseSteering[] getPoseSteering() {
		return psa;
	}
	
	/**
	 * Get the path (list of {@link Pose}s) of this {@link Trajectory}.
	 * @return The path (list of {@link Pose}s) of this {@link Trajectory}.
	 */
	public Pose[] getPose() {
		Pose[] ret = new Pose[psa.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = psa[i].getPose();
		}
		return ret;
	}
	
	/**
	 * Get the list of positions in this {@link Trajectory}'s path.
	 * @return The list of positions in this {@link Trajectory}'s path.
	 */
	public Coordinate[] getPositions() {
		ArrayList<Coordinate> ret = new ArrayList<Coordinate>();
		for (PoseSteering ps : psa) {
			ret.add(ps.getPose().getPosition());
		}
		return ret.toArray(new Coordinate[ret.size()]);
	}
	
	private PoseSteering[] readPath(String fileName) {
		ArrayList<PoseSteering> ret = new ArrayList<PoseSteering>();
		try {
			Scanner in = new Scanner(new FileReader(fileName));
			while (in.hasNextLine()) {
				String line = in.nextLine().trim();
				if (line.length() != 0) {
					String[] oneline = line.split(" ");
					PoseSteering ps = null;
					//x,y,z,r,p,y,s
					if (oneline.length == 7) {
					ps = new PoseSteering(
							new Double(oneline[0]).doubleValue(),
							new Double(oneline[1]).doubleValue(),
							new Double(oneline[2]).doubleValue(),
							new Double(oneline[3]).doubleValue(),
							new Double(oneline[4]).doubleValue(),
							new Double(oneline[5]).doubleValue(),
							new Double(oneline[6]).doubleValue());
					}
					//x,y,z,r,p,y
					else if (oneline.length == 6) {
					ps = new PoseSteering(
							new Double(oneline[0]).doubleValue(),
							new Double(oneline[1]).doubleValue(),
							new Double(oneline[2]).doubleValue(),
							new Double(oneline[3]).doubleValue(),
							new Double(oneline[4]).doubleValue(),
							new Double(oneline[5]).doubleValue(),
							0.0);
					}
					//x,y,z,th,s
					else if (oneline.length == 4) {
						ps = new PoseSteering(
								new Double(oneline[0]).doubleValue(),
								new Double(oneline[1]).doubleValue(),
								new Double(oneline[2]).doubleValue(),
								new Double(oneline[3]).doubleValue());
					}
					//x,y,z,th
					else {
						ps = new PoseSteering(
								new Double(oneline[0]).doubleValue(),
								new Double(oneline[1]).doubleValue(),
								new Double(oneline[2]).doubleValue(),
								0.0);					
					}
					ret.add(ps);
				}
			}
			in.close();
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		return ret.toArray(new PoseSteering[ret.size()]);
	}
		
}
