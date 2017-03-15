package org.metacsp.multi.spatioTemporal.paths;

import java.util.ArrayList;
import java.util.HashMap;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiConstraintSolver;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.activity.SymbolicVariableActivity;
import org.metacsp.multi.allenInterval.AllenInterval;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.allenInterval.AllenIntervalNetworkSolver;
import org.metacsp.multi.spatial.DE9IM.DE9IMRelation;
import org.metacsp.multi.spatial.DE9IM.DE9IMRelationSolver;
import org.metacsp.multi.spatial.DE9IM.GeometricShapeVariable;
import org.metacsp.multi.symbols.SymbolicVariableConstraintSolver;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * A {@link MultiConstraintSolver} for {@link TrajectoryEnvelope}s. Constraints of type {@link AllenIntervalConstraint} and
 * {@link DE9IMRelation} can be added to {@link TrajectoryEnvelope}s.
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
		super(new Class[]{AllenIntervalConstraint.class,DE9IMRelation.class}, TrajectoryEnvelope.class, createInternalConstraintSolvers(origin, horizon, -1), new int[]{1,3});
	}

	/**
	 * Create a {@link TrajectoryEnvelopeSolver} with given temporal origin and horizon.
	 * @param origin The origin of the temporal solver underlying this {@link TrajectoryEnvelopeSolver}.
	 * @param horizon The horizon of the temporal solver underlying this {@link TrajectoryEnvelopeSolver}.
	 * @param maxTrajectories The maximum number of {@link TrajectoryEnvelope}s that can be created with this solver. 
	 */
	public TrajectoryEnvelopeSolver(long origin, long horizon, int maxTrajectories) {
		super(new Class[]{AllenIntervalConstraint.class,DE9IMRelation.class}, TrajectoryEnvelope.class, createInternalConstraintSolvers(origin, horizon, maxTrajectories), new int[]{1,3});
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
			//ret[0] = new AllenIntervalNetworkSolver(origin, horizon, maxTrajectories);
			ret[0] = new ActivityNetworkSolver(origin, horizon, maxTrajectories);
		}
		else {
			//ret[0] = new AllenIntervalNetworkSolver(origin, horizon);
			ret[0] = new ActivityNetworkSolver(origin, horizon);
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
	 * among the temporal parts ({@link AllenInterval}s) of a trajectory envelope.
	 * @return The temporal solver responsible for propagating the temporal constraints among
	 * the temporal parts of {@link TrajectoryEnvelope}s.
	 */
	public AllenIntervalNetworkSolver getTemporalSolver() {
		return ((ActivityNetworkSolver)this.getConstraintSolvers()[0]).getAllenIntervalNetworkSolver();
	}

	/**
	 * Returns the {@link DE9IMRelationSolver} which propagates spatial constraints (of type {@link DE9IMRelation})
	 * among the spatial parts ({@link GeometricShapeVariable}s) of {@link TrajectoryEnvelope}s.
	 * @return The spatial solver responsible for propagating the spatial constraints among
	 * the spatial parts of {@link TrajectoryEnvelope}s.
	 */
	public DE9IMRelationSolver getSpatialSolver() {
		return (DE9IMRelationSolver)this.getConstraintSolvers()[1];
	}
	
	private ArrayList<TrajectoryEnvelope> makeEnvelope(int robotID, long firstParkingDuration, long lastParkingDuration, Trajectory trajRobot, Coordinate ... footprintCoords) {
		ArrayList<TrajectoryEnvelope> ret = new ArrayList<TrajectoryEnvelope>();
		
		TrajectoryEnvelope te = (TrajectoryEnvelope)this.createVariable();
		TrajectoryEnvelope parkingStart = (TrajectoryEnvelope)this.createVariable();
		TrajectoryEnvelope parkingEnd = (TrajectoryEnvelope)this.createVariable();

		te.setComponent("Robot" + robotID);
		te.getSymbolicVariableActivity().setSymbolicDomain("Driving");
		parkingStart.setComponent("Robot" + robotID);
		parkingStart.getSymbolicVariableActivity().setSymbolicDomain("Parking (initial)");
		parkingStart.setRobotID(robotID);
		parkingEnd.setComponent("Robot" + robotID);
		parkingEnd.getSymbolicVariableActivity().setSymbolicDomain("Parking (final)");
		parkingEnd.setRobotID(robotID);
		
		ArrayList<AllenIntervalConstraint> consToAdd = new ArrayList<AllenIntervalConstraint>();
		TrajectoryEnvelope trajEnvelopeRobot = (TrajectoryEnvelope)te;
		//frontLeft, frontRight, backRight, backLeft
		//trajEnvelopeRobot.setFootprint(footprintCoords[3],footprintCoords[2],footprintCoords[0],footprintCoords[1]);
		trajEnvelopeRobot.setFootprint(footprintCoords);
		trajEnvelopeRobot.setTrajectory(trajRobot);
		trajEnvelopeRobot.setRobotID(robotID);
		
		Pose parkingStartPose = trajEnvelopeRobot.getTrajectory().getPoseSteering()[0].getPose();
		Trajectory trajStart = new Trajectory(new Pose[] {parkingStartPose});
		parkingStart.setFootprint(footprintCoords);
		parkingStart.setTrajectory(trajStart);
		parkingStart.setRefinable(false);

		Pose parkingEndPose = trajEnvelopeRobot.getTrajectory().getPoseSteering()[trajEnvelopeRobot.getTrajectory().getPoseSteering().length-1].getPose();
		Trajectory trajEnd = new Trajectory(new Pose[] {parkingEndPose});
		parkingEnd.setFootprint(footprintCoords);
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
		
		AllenIntervalConstraint durFirst = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(firstParkingDuration, APSPSolver.INF));
		durFirst.setFrom(parkingStart);
		durFirst.setTo(parkingStart);
		consToAdd.add(durFirst);
		
		AllenIntervalConstraint durLast = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(lastParkingDuration, APSPSolver.INF));
		durLast.setFrom(parkingEnd);
		durLast.setTo(parkingEnd);
		consToAdd.add(durLast);
		
		ret.add(parkingStart);
		ret.add(trajEnvelopeRobot);
		ret.add(parkingEnd);

		this.addConstraints(consToAdd.toArray(new AllenIntervalConstraint[consToAdd.size()]));

		return ret;
	}
	
	public TrajectoryEnvelope createEnvelopeNoParking(int robotID, String pathFile, String symbol, Coordinate ... footprintCoords) {		
		Trajectory trajRobot = new Trajectory(pathFile);
		TrajectoryEnvelope te = (TrajectoryEnvelope)this.createVariable();
		te.setComponent("Robot" + robotID);
		te.getSymbolicVariableActivity().setSymbolicDomain(symbol);
		TrajectoryEnvelope trajEnvelopeRobot = (TrajectoryEnvelope)te;
		trajEnvelopeRobot.setFootprint(footprintCoords);
		trajEnvelopeRobot.setTrajectory(trajRobot);
		trajEnvelopeRobot.setRobotID(robotID);
		return trajEnvelopeRobot;
	}
	
	public TrajectoryEnvelope createParkingEnvelope(int robotID, long parkingDuration, Pose parkingPose, Coordinate ... footprintCoords) {
		
		TrajectoryEnvelope parking = (TrajectoryEnvelope)this.createVariable();

		parking.setComponent("Robot" + robotID);
		parking.getSymbolicVariableActivity().setSymbolicDomain("Parking (final)");
		parking.setRobotID(robotID);
				
		Trajectory trajParking = new Trajectory(new Pose[] {parkingPose});
		parking.setFootprint(footprintCoords);
		parking.setTrajectory(trajParking);
		parking.setRefinable(false);
				
		AllenIntervalConstraint durParking = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(parkingDuration, APSPSolver.INF));
		durParking.setFrom(parking);
		durParking.setTo(parking);
	
		this.addConstraints(durParking);

		return parking;
	}
	

	/**
	 * Create a trajectory envelope for each given {@link Trajectory}. Robot IDs are assigned starting from the given
	 * integer. This method creates three envelopes for each {@link Trajectory}:
	 * the main {@link TrajectoryEnvelope} covering the path; one {@link TrajectoryEnvelope} for the starting position
	 * of the robot; and one one {@link TrajectoryEnvelope} for the final parking position of the robot. The three envelopes
	 * are constrained with {@link AllenIntervalConstraint.Type#Meets} constraints. The two parking envelopes have a given duration.
	 * @param firstRobotID The starting robot ID.
	 * @param durationFirstParking The duration of the first parking {@link TrajectoryEnvelope}.
	 * @param durationLastParking The duration of the final parking {@link TrajectoryEnvelope}.
	 * @param footprint Coordinates representing the footprint of the robot.
	 * @param trajectories The {@link Trajectory}s over which to create the {@link TrajectoryEnvelope}s.
	 * @return A mapping between robot IDs and the newly created sets of {@link TrajectoryEnvelope}s.
	 */
	public HashMap<Integer,ArrayList<TrajectoryEnvelope>> createEnvelopes(int firstRobotID, long durationFirstParking, long durationLastParking, Coordinate[] footprint, Trajectory ... trajectories) {

		HashMap<Integer,ArrayList<TrajectoryEnvelope>> ret = new HashMap<Integer, ArrayList<TrajectoryEnvelope>>();
		
		for (int i = 0; i < trajectories.length; i++) {
			ArrayList<TrajectoryEnvelope> oneRobot = makeEnvelope(i+firstRobotID, durationFirstParking, durationLastParking, trajectories[i], footprint);
			ret.put(i+firstRobotID, oneRobot);
		}		
		return ret;
	}
	
	/**
	 * Create a trajectory envelope for each given reference to a file containing a path.
	 * Robot IDs are assigned starting from the given integer. This method creates three envelopes for each
	 * path: the main {@link TrajectoryEnvelope} covering the path; one {@link TrajectoryEnvelope} for the
	 * starting position of the robot; and one one {@link TrajectoryEnvelope} for the final parking position
	 * of the robot. The three envelopes are constrained with {@link AllenIntervalConstraint.Type#Meets} constraints.
	 * The two parking envelopes have a given duration.
	 * @param firstRobotID The starting robot ID.
	 * @param durationFirstParking The duration of the first parking {@link TrajectoryEnvelope}.
	 * @param durationLastParking The duration of the final parking {@link TrajectoryEnvelope}.
	 * @param footprint Coordinates representing the footprint of the robot.
	 * @param pathFiles The files containing paths over which to create the {@link TrajectoryEnvelope}s.
	 * @return A mapping between robot IDs and the newly created sets of {@link TrajectoryEnvelope}s.
	 */
	public HashMap<Integer,ArrayList<TrajectoryEnvelope>> createEnvelopes(int firstRobotID, long durationFirstParking, long durationLastParking, Coordinate[] footprint, String ... pathFiles) {

		Trajectory[] trajectories = new Trajectory[pathFiles.length];
		for (int i = 0; i < pathFiles.length; i++) {
			trajectories[i] = new Trajectory(pathFiles[i]);
		}
		return createEnvelopes(firstRobotID, durationFirstParking, durationLastParking, footprint, trajectories);
	}

	/**
	 * Create a trajectory envelope for each given reference to a file containing a path.
	 * Robot IDs are assigned starting from the given integer. This method creates three envelopes for each
	 * path: the main {@link TrajectoryEnvelope} covering the path; one {@link TrajectoryEnvelope} for the
	 * starting position of the robot; and one one {@link TrajectoryEnvelope} for the final parking position
	 * of the robot. The three envelopes are constrained with {@link AllenIntervalConstraint.Type#Meets} constraints.
	 * The two parking envelopes have a duration of 3000 ms each. A default footprint of size 2.7 (w) x 6.6 (l) is used. 
	 * @param firstRobotID The starting robot ID.
	 * @param pathFiles The files containing paths over which to create the {@link TrajectoryEnvelope}s.
	 * @return A mapping between robot IDs and the newly created sets of {@link TrajectoryEnvelope}s.
	 */	
	public HashMap<Integer,ArrayList<TrajectoryEnvelope>> createEnvelopes(int firstRobotID, String ... pathFiles) {
		//Default footprint, 2.7 (w) x 6.6 (l)
		Coordinate frontLeft = new Coordinate(5.3, 1.35);
		Coordinate frontRight = new Coordinate(5.3, -1.35);
		Coordinate backRight = new Coordinate(-1.3, -1.35);
		Coordinate backLeft = new Coordinate(-1.3, 1.35);
		Coordinate[] footprint = new Coordinate[] {frontLeft,frontRight,backLeft,backRight};
		long durationFirstParking = 3000;
		long durationLastParking = 3000;
		return createEnvelopes(firstRobotID, durationFirstParking, durationLastParking, footprint, pathFiles);
	}

	/**
	 * Create a trajectory envelope for each given {@link Trajectory}. Robot IDs are assigned starting from the given
	 * integer. This method creates three envelopes for each {@link Trajectory}:
	 * the main {@link TrajectoryEnvelope} covering the path; one {@link TrajectoryEnvelope} for the starting position
	 * of the robot; and one one {@link TrajectoryEnvelope} for the final parking position of the robot. The three envelopes
	 * are constrained with {@link AllenIntervalConstraint.Type#Meets} constraints. The two parking envelopes have a duration of 3000 ms each.
	 * A default footprint of size 2.7 (w) x 6.6 (l) is used.
	 * @param firstRobotID The starting robot ID.
	 * @param trajectories The {@link Trajectory}s over which to create the {@link TrajectoryEnvelope}s.
	 * @return A mapping between robot IDs and the newly created sets of {@link TrajectoryEnvelope}s.
	 */
	public HashMap<Integer,ArrayList<TrajectoryEnvelope>> createEnvelopes(int firstRobotID, Trajectory ... trajectories) {
		//Default footprint, 2.7 (w) x 6.6 (l)
		Coordinate frontLeft = new Coordinate(5.3, 1.35);
		Coordinate frontRight = new Coordinate(5.3, -1.35);
		Coordinate backRight = new Coordinate(-1.3, -1.35);
		Coordinate backLeft = new Coordinate(-1.3, 1.35);
		Coordinate[] footprint = new Coordinate[] {frontLeft,frontRight,backLeft,backRight};
		long durationFirstParking = 3000;
		long durationLastParking = 3000;
		return createEnvelopes(firstRobotID, durationFirstParking, durationLastParking, footprint, trajectories);
	}



}
