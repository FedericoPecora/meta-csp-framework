package org.metacsp.multi.spatioTemporal.paths;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
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
public class Trajectory {
	
	private PoseSteering[] psa;
	private double[] dts;
	private static double MAX_SPEED = 3.0;
	private static double MAX_ACCELERATION = 0.3;
	
	/**
	 * Create a new {@link Trajectory} given a list of {@link Pose}s. The
	 * temporal profile of the trajectory is computed via naive numeric resolution
	 * assuming constant acceleration and a maximum speed.
	 * @param pa The list of {@link Pose}s used to build the path.
	 */
	public Trajectory(Pose[] pa) {
		this.psa = new PoseSteering[pa.length];
		for (int i = 0; i < pa.length; i++) {
			PoseSteering ps = new PoseSteering(pa[i].getX(), pa[i].getY(), pa[i].getTheta(), 0.0);
			this.psa[i] = ps;
		}
		this.dts = new double[psa.length];
		this.updateDts();
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
			PoseSteering ps = new PoseSteering(pa[i].getX(), pa[i].getY(), pa[i].getTheta(), 0.0);
			this.psa[i] = ps;
		}
		this.dts = new double[psa.length];
		this.dts = dts;
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
	}
	
	/**
	 * Create a new {@link Trajectory} given a list of {@link PoseSteering}s that is read
	 * from a file. The temporal profile of the trajectory is computed via naive numeric resolution
	 * assuming constant acceleration and a maximum speed.
	 * @param psa The path of a file pointing to the list of {@link PoseSteering}s used to build the path.
	 */	
	public Trajectory(String fileName) {
		this.psa = readPath(fileName);
		this.dts = new double[psa.length];
		this.updateDts();
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
	 * given the current path index. The estimate is based on this {@link Trajectory}'s
	 * temporal profile.
	 * @param sequenceNum The path index from which the estimate should be computed.
	 * @return An estimate of the time left to move on this {@link Trajectory}
	 * given the current path index.
	 */
	public double getTimeLeftEstimate(int sequenceNum) {
		double timeCounter = 0.0;
		for (int i = sequenceNum; i < this.getDTs().length; i++) {
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
			dts[i] = countDts*dt-prevSum;
		}
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
	 * Get the list of positions in this {@link Trajectory}'s path.
	 * @return The list of positions in this {@link Trajectory}'s path.
	 */
	public Coordinate[] getPositions() {
		ArrayList<Coordinate> ret = new ArrayList<Coordinate>();
		for (PoseSteering ps : psa) {
			ret.add(new Coordinate(ps.getX(), ps.getY()));
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
					if (oneline.length == 4) {
					ps = new PoseSteering(
							new Double(oneline[0]).doubleValue(),
							new Double(oneline[1]).doubleValue(),
							new Double(oneline[2]).doubleValue(),
							new Double(oneline[3]).doubleValue());
					}
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
