package org.metacsp.multi.spatioTemporal.paths;

import java.util.ArrayList;
import java.util.HashMap;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiConstraintSolver;
import org.metacsp.multi.allenInterval.AllenInterval;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.allenInterval.AllenIntervalNetworkSolver;
import org.metacsp.multi.spatial.DE9IM.DE9IMRelation;
import org.metacsp.multi.spatial.DE9IM.DE9IMRelationSolver;
import org.metacsp.multi.spatial.DE9IM.GeometricShapeVariable;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * A {@link MultiConstraintSolver} for {@link SpatioTemporalVariable}s. Constraints of type {@link AllenIntervalConstraint} and
 * {@link DE9IMRelation} can be added to {@link SpatioTemporalVariable}s.
 * 
 * @author Federico Pecora
 *
 */
public class TrajectoryEnvelopeSolver extends MultiConstraintSolver {
	
	private static final long serialVersionUID = 5925181272826836649L;

	protected TrajectoryEnvelopeSolver(Class<?>[] constraintTypes, Class<?> variableType, ConstraintSolver[] internalSolvers, int[] ingredients) {
		super(constraintTypes, variableType, internalSolvers, ingredients);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Get the origin of time.
	 * @return The origin of time.
	 */
	public long getOrigin() {
		return this.getTemporalSolver().getOrigin();
	}

	/**
	 * Get the temporal horizon.
	 * @return The temporal horizon.
	 */
	public long getHorizon() {
		return this.getTemporalSolver().getHorizon();
	}

	/**
	 * Create a {@link TrajectoryEnvelopeSolver} with given temporal origin and horizon.
	 * @param origin The origin of the temporal solver underlying this {@link TrajectoryEnvelopeSolver}.
	 * @param horizon The horizon of the temporal solver underlying this {@link TrajectoryEnvelopeSolver}.
	 */
	public TrajectoryEnvelopeSolver(long origin, long horizon) {
		super(new Class[]{AllenIntervalConstraint.class,DE9IMRelation.class}, TrajectoryEnvelope.class, createInternalConstraintSolvers(origin, horizon, -1), new int[]{1,2});
	}

	/**
	 * Create a {@link TrajectoryEnvelopeSolver} with given temporal origin and horizon.
	 * @param origin The origin of the temporal solver underlying this {@link TrajectoryEnvelopeSolver}.
	 * @param horizon The horizon of the temporal solver underlying this {@link TrajectoryEnvelopeSolver}.
	 * @param maxTrajectories The maximum number of {@link TrajectoryEnvelope}s that can be created with this solver. 
	 */
	public TrajectoryEnvelopeSolver(long origin, long horizon, int maxTrajectories) {
		super(new Class[]{AllenIntervalConstraint.class,DE9IMRelation.class}, TrajectoryEnvelope.class, createInternalConstraintSolvers(origin, horizon, maxTrajectories), new int[]{1,2});
	}

	/**
	 * Get all the {@link TrajectoryEnvelope}s in this solver's {@link ConstraintNetwork} pertaining to a particular robot.
	 * @param robotID The ID of the robot for which the {@link TrajectoryEnvelope}s are desired.
	 * @return All the {@link TrajectoryEnvelope}s in this solver's {@link ConstraintNetwork} pertaining to a particular robot.
	 */
	public TrajectoryEnvelope[] getTrajectoryEnvelopes(int robotID) {
		ArrayList<TrajectoryEnvelope> ret = new ArrayList<TrajectoryEnvelope>();
		for (Variable v : this.getVariables()) {
			TrajectoryEnvelope te = (TrajectoryEnvelope)v;
			if (te.getRobotID() == robotID) ret.add(te);
		}
		return ret.toArray(new TrajectoryEnvelope[ret.size()]);
	}

	/**
	 * Get all {@link TrajectoryEnvelope}s that have no super-envelopes.
	 * @return All {@link TrajectoryEnvelope}s that have no super-envelopes.
	 */
	public TrajectoryEnvelope[] getRootTrajectoryEnvelopes() {
		ArrayList<TrajectoryEnvelope> ret = new ArrayList<TrajectoryEnvelope>();
		for (Variable var : this.getVariables()) {
			TrajectoryEnvelope te = (TrajectoryEnvelope)var;
			if (!te.hasSuperEnvelope()) ret.add(te);
		}
		return ret.toArray(new TrajectoryEnvelope[ret.size()]);
	}
	
	/**
	 * Get all {@link TrajectoryEnvelope}s that have no super-envelopes for a given robot.
	 * @param robotID The ID of the robot for which the {@link TrajectoryEnvelope}s are desired.
	 * @return All {@link TrajectoryEnvelope}s that have no super-envelopes for a given robot.
	 */
	public TrajectoryEnvelope[] getRootTrajectoryEnvelopes(int robotID) {
		ArrayList<TrajectoryEnvelope> ret = new ArrayList<TrajectoryEnvelope>();
		for (Variable var : this.getVariables()) {
			TrajectoryEnvelope te = (TrajectoryEnvelope)var;
			if (!te.hasSuperEnvelope() && te.getRobotID() == robotID) ret.add(te);
		}
		return ret.toArray(new TrajectoryEnvelope[ret.size()]);
	}

	
	private static ConstraintSolver[] createInternalConstraintSolvers(long origin, long horizon, int maxTrajectories) {
		ConstraintSolver[] ret = new ConstraintSolver[2];
		if (maxTrajectories >= 1) {
			ret[0] = new AllenIntervalNetworkSolver(origin, horizon, maxTrajectories);
		}
		else {
			ret[0] = new AllenIntervalNetworkSolver(origin, horizon);
		}
		ret[1] = new DE9IMRelationSolver();
		return ret;
	}
	
	@Override
	public boolean propagate() {
		return true;
	}
	
	/**
	 * Returns the {@link AllenIntervalNetworkSolver} which propagates temporal constraints
	 * (of type {@link AllenIntervalConstraint})
	 * among the temporal parts ({@link AllenInterval}s) of {@link SpatioTemporalVariable}s.
	 * @return The temporal solver responsible for propagating the temporal constraints among
	 * the temporal parts of {@link SpatioTemporalVariable}s.
	 */
	public AllenIntervalNetworkSolver getTemporalSolver() {
		return (AllenIntervalNetworkSolver)this.getConstraintSolvers()[0];
	}

	/**
	 * Returns the {@link DE9IMRelationSolver} which propagates spatial constraints (of type {@link DE9IMRelation})
	 * among the spatial parts ({@link GeometricShapeVariable}s) of {@link SpatioTemporalVariable}s.
	 * @return The spatial solver responsible for propagating the spatial constraints among
	 * the spatial parts of {@link SpatioTemporalVariable}s.
	 */
	public DE9IMRelationSolver getSpatialSolver() {
		return (DE9IMRelationSolver)this.getConstraintSolvers()[1];
	}
	
	private ArrayList<TrajectoryEnvelope> makeEnvelope(int robotID, String path, Coordinate frontLeft, Coordinate frontRight, Coordinate backRight, Coordinate backLeft) {
		ArrayList<TrajectoryEnvelope> ret = new ArrayList<TrajectoryEnvelope>();
		
		TrajectoryEnvelope te = (TrajectoryEnvelope)this.createVariable();
		TrajectoryEnvelope parkingStart = (TrajectoryEnvelope)this.createVariable();
		TrajectoryEnvelope parkingEnd = (TrajectoryEnvelope)this.createVariable();

		ArrayList<AllenIntervalConstraint> consToAdd = new ArrayList<AllenIntervalConstraint>();
		TrajectoryEnvelope trajEnvelopeRobot = (TrajectoryEnvelope)te;
		Trajectory trajRobot = new Trajectory(path);
		
		trajEnvelopeRobot.setFootprint(backLeft,backRight,frontLeft,frontRight);
		trajEnvelopeRobot.setTrajectory(trajRobot);
		trajEnvelopeRobot.setRobotID(robotID);
		
		Pose parkingStartPose = trajEnvelopeRobot.getTrajectory().getPoseSteering()[0].getPose();
		Trajectory trajStart = new Trajectory(new Pose[] {parkingStartPose});
		parkingStart.setFootprint(backLeft,backRight,frontLeft,frontRight);
		parkingStart.setTrajectory(trajStart);
		parkingStart.setRefinable(false);

		Pose parkingEndPose = trajEnvelopeRobot.getTrajectory().getPoseSteering()[trajEnvelopeRobot.getTrajectory().getPoseSteering().length-1].getPose();
		Trajectory trajEnd = new Trajectory(new Pose[] {parkingEndPose});
		parkingEnd.setFootprint(backLeft,backRight,frontLeft,frontRight);
		parkingEnd.setTrajectory(trajEnd);
		parkingEnd.setRefinable(false);
		
		AllenIntervalConstraint parkingMeetsDriving = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets);
		parkingMeetsDriving.setFrom(parkingStart);
		parkingMeetsDriving.setTo(trajEnvelopeRobot);
		consToAdd.add(parkingMeetsDriving);
		
		AllenIntervalConstraint drivingMeetsParking = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets);
		drivingMeetsParking.setFrom(trajEnvelopeRobot);
		drivingMeetsParking.setTo(parkingEnd);
		consToAdd.add(drivingMeetsParking);
		
		AllenIntervalConstraint parkingEndForever = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Forever);
		parkingEndForever.setFrom(parkingEnd);
		parkingEndForever.setTo(parkingEnd);
		consToAdd.add(parkingEndForever);
		
		ret.add(parkingStart);
		ret.add(trajEnvelopeRobot);
		ret.add(parkingEnd);

		this.addConstraints(consToAdd.toArray(new AllenIntervalConstraint[consToAdd.size()]));

		return ret;
	}
	
	public HashMap<Integer,ArrayList<TrajectoryEnvelope>> createEnvelope(int robotID, String path) {

		//XA15 footprint, 2.7 (w) x 6.6 (l)
		Coordinate frontLeft = new Coordinate(5.3, 1.35);
		Coordinate frontRight = new Coordinate(5.3, -1.35);
		Coordinate backRight = new Coordinate(-1.3, -1.35);
		Coordinate backLeft = new Coordinate(-1.3, 1.35);
		
		HashMap<Integer,ArrayList<TrajectoryEnvelope>> ret = new HashMap<Integer, ArrayList<TrajectoryEnvelope>>();
		
		for (TrajectoryEnvelope te : this.getRootTrajectoryEnvelopes()) {
			ArrayList<TrajectoryEnvelope> oneRobot = new ArrayList<TrajectoryEnvelope>(te.getGroundEnvelopes());
			ret.put(te.getRobotID(), oneRobot);
		}
		ArrayList<TrajectoryEnvelope> newRobot = this.makeEnvelope(robotID, path, frontLeft, frontRight, backRight, backLeft);
		ret.put(robotID, newRobot);
		return ret;
	}
	
	public HashMap<Integer,ArrayList<TrajectoryEnvelope>> createEnvelopes(String ... paths) {

		//XA15 footprint, 2.7 (w) x 6.6 (l)
		Coordinate frontLeft = new Coordinate(5.3, 1.35);
		Coordinate frontRight = new Coordinate(5.3, -1.35);
		Coordinate backRight = new Coordinate(-1.3, -1.35);
		Coordinate backLeft = new Coordinate(-1.3, 1.35);
		
		HashMap<Integer,ArrayList<TrajectoryEnvelope>> ret = new HashMap<Integer, ArrayList<TrajectoryEnvelope>>();
		
		for (int i = 0; i < paths.length; i++) {
			ArrayList<TrajectoryEnvelope> oneRobot = makeEnvelope(i, paths[i], frontLeft, frontRight, backRight, backLeft);
			ret.put(i, oneRobot);
		}
		
		return ret;
	}

}
