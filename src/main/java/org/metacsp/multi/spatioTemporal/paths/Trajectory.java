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

public class Trajectory {
	
	private PoseSteering[] psa;
	private double[] dts;
	public static double MAX_SPEED = 3.0;
	public static double MAX_ACCELERATION = 0.3;
	
	public Trajectory(PoseSteering[] psa) {
		this.psa = psa;
		this.dts = new double[psa.length];
		this.updateDts();
	}

	public Trajectory(PoseSteering[] psa, double[] dts) {
		this.psa = psa;
		this.dts = new double[psa.length];
		this.dts[0] = 0.0;
		for (int i = 1; i < this.dts.length; i++) this.dts[i] = dts[i-1];
	}
	
	public Trajectory(String fileName) {
		this.psa = readPath(fileName);
		this.dts = new double[psa.length];
		this.updateDts();
	}
	
	public double[] getDTs() {
		return dts;
	}
	
	public double getStart() {
		return dts[0];
	}

	public double getEnd() {
		return dts[dts.length-1];
	}

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

	public PoseSteering[] getPoseSteering() {
		return psa;
	}
	
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
				String[] oneline = in.nextLine().trim().split(" ");
				PoseSteering ps = new PoseSteering(
						new Double(oneline[0]).doubleValue(),
						new Double(oneline[1]).doubleValue(),
						new Double(oneline[2]).doubleValue(),
						new Double(oneline[3]).doubleValue());
				ret.add(ps);
			}
			in.close();
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		return ret.toArray(new PoseSteering[ret.size()]);
	}
	
}
