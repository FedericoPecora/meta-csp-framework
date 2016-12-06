package org.metacsp.multi.spatioTemporal.paths;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiVariable;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.SymbolicVariableActivity;
import org.metacsp.multi.allenInterval.AllenInterval;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.spatial.DE9IM.DE9IMRelation;
import org.metacsp.multi.spatial.DE9IM.GeometricShapeDomain;
import org.metacsp.multi.spatial.DE9IM.GeometricShapeVariable;
import org.metacsp.multi.spatial.DE9IM.LineStringDomain;
import org.metacsp.multi.spatial.DE9IM.PointDomain;
import org.metacsp.multi.spatial.DE9IM.PolygonalDomain;
import org.metacsp.throwables.NoFootprintException;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;

import com.vividsolutions.jts.JTSVersion;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.util.AffineTransformation;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;
import com.vividsolutions.jts.precision.SimpleGeometryPrecisionReducer;
import com.vividsolutions.jts.util.GeometricShapeFactory;

/**
 * A {@link TrajectoryEnvelope} is a {@link MultiVariable} composed of an {@link AllenInterval} (temporal part)
 * and two {@link GeometricShapeVariable}s. One {@link GeometricShapeVariable} represents the reference path, the other
 * represents the spatial envelope. Constraints of type {@link AllenIntervalConstraint} and
 * {@link DE9IMRelation} can be added to {@link TrajectoryEnvelope}s.
 * 
 * @author Federico Pecora
 */
public class TrajectoryEnvelope extends MultiVariable implements Activity {

	private static final long serialVersionUID = 183736569434737103L;
	public static long RESOLUTION = 1000;
//	private double width = 1.3;
//	private double length= 3.5;
//	private double deltaW = 0.0;
//	private double deltaL = 0.0;
	private Trajectory trajectory = null;
	private boolean refinable = true;
	private TrajectoryEnvelope superEnvelope  = null;
	private ArrayList<TrajectoryEnvelope> subEnvelopes = null;
	private int robotID = -1;
	private Polygon footprint = null;
	private int sequenceNumberStart = -1;
	private int sequenceNumberEnd = -1;
	
	public TrajectoryEnvelope(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers, Variable[] internalVars) {
		super(cs, id, internalSolvers, internalVars);
		// TODO Auto-generated constructor stub
	}
		
	/**
	 * Get a {@link Polygon} representing the footprint of this {@link TrajectoryEnvelope}.
	 * @return A {@link Polygon} representing the footprint of this {@link TrajectoryEnvelope}.
	 */
	public Polygon getFootprint() {
		return footprint;
	}
	
	public String getInfo() {
		
		String ret = "";
		double[] teDTs = this.getTrajectory().getDTs();
		double[] teCTs = this.getCTs();

		DecimalFormat df = new DecimalFormat("#0.00");
		df.setRoundingMode(RoundingMode.HALF_DOWN);
		ret += this + "\n  Ground envelopes:";
		for (TrajectoryEnvelope te : this.getGroundEnvelopes()) {
			ret += "\n    " + te;
		}
		ret += "\n\nSeq\tDT\tCT\n------------------------------------------\n";
		for (int i = 0; i < teDTs.length; i++) {
			ret += i + "\t" + df.format(teDTs[i]) + " \t" + df.format(teCTs[i]);
			if (i < teDTs.length-1) ret += "\n";
		}
		return ret;
	}
	
	public void setFootprint(Polygon footprint) {
		this.footprint = footprint;
	}

	/**
	 * Set the footprint of this {@link TrajectoryEnvelope}, which is used for computing the spatial envelope. Provide
	 * the bounding box of the machine assuming its reference point is in (0,0).
	 * @param coords Coordinates of the footprint.
	 */
	public void setFootprint(Coordinate ... coords) {
		this.initFootprint(coords);
	}

	/**
	 * Set the footprint of this {@link TrajectoryEnvelope}, which is used for computing the spatial envelope. Provide
	 * the bounding box of the machine assuming its reference point is in (0,0).
	 * @param backLeft The rear left coordinate of the bounding box.
	 * @param backRight The rear right coordinate of the bounding box.
	 * @param frontLeft The front left coordinate of the bounding box.
	 * @param frontRight The front right coordinate of the bounding box.
	 */
	public void setFootprint(Coordinate backLeft, Coordinate backRight, Coordinate frontLeft, Coordinate frontRight) {
		this.initFootprint(backLeft, backRight, frontRight, frontLeft);
	}
	
	/**
	 * Set the footprint of this {@link TrajectoryEnvelope}, which is used for computing the spatial envelope.
	 * @param w The width of the robot's footprint (dimension along the perpendicular to the driving direction).
	 * @param l The length of the robot's footprint (dimension along the driving direction).
	 * @param dw Lateral displacement of the reference point of the robot (along the perpendicular to the driving direction).
	 * @param dl Forward displacement of the reference point of the robot (along the driving direction).
	 */
	public void setFootprint(double w, double l, double dw, double dl) {
		this.initFootprint(w, l, dw, dl);
	}
	
	/**
	 * Add a sub-envelope to this {@link TrajectoryEnvelope}.
	 * @param se A sub-envelope to add to this {@link TrajectoryEnvelope}.
	 */
	public void addSubEnvelope(TrajectoryEnvelope se) {
		if (this.subEnvelopes == null) this.subEnvelopes = new ArrayList<TrajectoryEnvelope>();
		this.subEnvelopes.add(se);
	}
	
	/**
	 * Assess whether this {@link TrajectoryEnvelope} has one or more sub-envelopes.
	 * @return <code>true</code> iff this {@link TrajectoryEnvelope} has one or more sub-envelopes.
	 */
	public boolean hasSubEnvelopes() {
		return this.subEnvelopes != null && !this.subEnvelopes.isEmpty();
	}
	
	/**
	 * Get the sub-envelopes of this {@link TrajectoryEnvelope}.
	 * @return The sub-envelopes of this {@link TrajectoryEnvelope}.
	 */
	public ArrayList<TrajectoryEnvelope> getSubEnvelopes() {
		return subEnvelopes;
	}
	
	private double[] createCTVector() {
		double[] ret = new double[this.getPathLength()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = -1.0;
		}
		return ret;
	}
	
	/**
	 * Get an estimate of the time left to move on the reference {@link Trajectory} of this {@link TrajectoryEnvelope}
	 * given the current path index. The estimate is based on the reference {@link Trajectory}'s
	 * temporal profile.
	 * @param seqNum The path index from which the estimate should be computed.
	 * @return An estimate of the time left to move on the reference {@link Trajectory}
	 * of this {@link TrajectoryEnvelope}, given the current path index.
	 */
	public double getTimeLeftEstimate(int seqNum) {
		return this.getTrajectory().getTimeLeftEstimate(seqNum);
	}

	/**
	 * Get an estimate of the time left to move on the reference {@link Trajectory} of this {@link TrajectoryEnvelope}
	 * given the current path index. The estimate is based on the reference {@link Trajectory}'s
	 * temporal profile. This method returns the time left on the ground {@link TrajectoryEnvelope} on which
	 * the given path index lies.
	 * @param seqNum The path index from which the estimate should be computed.
	 * @return An estimate of the time left to move on the reference {@link Trajectory}
	 * of this {@link TrajectoryEnvelope}, given the current path index.
	 */
	public double getPartialTimeLeftEstimate(int seqNum) {
		return this.getGroundEnvelope(seqNum).getTimeLeftEstimate(seqNum);
	}
	
	/**
	 * Get the ground {@link TrajectoryEnvelope}s of this {@link TrajectoryEnvelope}, ordered 
	 * by increasing start time.
	 * @return The ground {@link TrajectoryEnvelope}s of this {@link TrajectoryEnvelope}.
	 */
	public TreeSet<TrajectoryEnvelope> getGroundEnvelopes() {
		TreeSet<TrajectoryEnvelope> ret = new TreeSet<TrajectoryEnvelope>(new Comparator<TrajectoryEnvelope>() {
			@Override
			public int compare(TrajectoryEnvelope o1, TrajectoryEnvelope o2) {
				Bounds o1b = new Bounds(o1.getTemporalVariable().getEST(),o1.getTemporalVariable().getEET());
				Bounds o2b = new Bounds(o2.getTemporalVariable().getEST(),o2.getTemporalVariable().getEET());
				if (o2b.min-o1b.min > 0) return -1;
				else if (o2b.min-o1b.min == 0) return 0;
				return 1;
			}
		});			
		if (this.getSubEnvelopes() != null) {
			for (TrajectoryEnvelope te : this.getSubEnvelopes()) {
				ret.addAll(te.getGroundEnvelopes());
			}
		}
		else ret.add(this);
		return ret;
	}
	
	/**
	 * Get the ground {@link TrajectoryEnvelope} in which the robot will be a the given sequence number along
	 * the trajectory.
	 * @param seqNum The index of sequence number which the returned ground {@link TrajectoryEnvelope} should contain.
	 * @return The ground {@link TrajectoryEnvelope} at the given index in the reference {@link Trajectory}.
	 */
	public TrajectoryEnvelope getGroundEnvelope(int seqNum) {
		Coordinate currentPos = this.getTrajectory().getPositions()[seqNum];
		GeometryFactory gf = new GeometryFactory();
		Point point = gf.createPoint(currentPos);
		for (TrajectoryEnvelope ge : this.getGroundEnvelopes()) {
			if (((GeometricShapeDomain)ge.getEnvelopeVariable().getDomain()).getGeometry().contains(point)) return ge;
		}
		return this;
	}


	/**
	 * Get the ground {@link TrajectoryEnvelope} in which the robot will be
	 * at a given time. Returns <code>null</code> if the robot will not be in this
	 * {@link TrajectoryEnvelope} at the given time, or if the robot is not in any ground envelope
	 * at the given time (e.g., traveling between the end point of one ground envelope and
	 * the starting point of the next one).
	 * @param time The time at which to return the {@link TrajectoryEnvelope}.
	 * @return The ground {@link TrajectoryEnvelope} in which the robot will be
	 * at a given time.
	 */
	public TrajectoryEnvelope getGroundEnvelope(long time) {
		if (this.getTemporalVariable().getEST() > time || this.getTemporalVariable().getEET() < time) return null;
		if (!this.hasSubEnvelopes()) return this;
		for (TrajectoryEnvelope te : this.getGroundEnvelopes()) {
			if (te.getTemporalVariable().getEST() <= time) {
				if (te.getTemporalVariable().getEET() >= time) {
					return te;
				}
			}
		}
		return null;
	}
	
	/**
	 * Get the ground {@link TrajectoryEnvelope} in which the robot will be
	 * at a given time. Returns <code>null</code> if the robot will not be in this
	 * {@link TrajectoryEnvelope} at the given time.
	 * @param time The time at which to return the {@link TrajectoryEnvelope}.
	 * @return The ground {@link TrajectoryEnvelope} in which the robot will be
	 * at a given time, or the ground {@link TrajectoryEnvelope} it is leaving in case
	 * the robot is traveling between the end point of one ground envelope and
	 * the starting point of the next one.
	 */
	public TrajectoryEnvelope getClosestGroundEnvelope(long time) {
		if (this.getTemporalVariable().getEST() > time || this.getTemporalVariable().getEET() < time) return null;
		if (!this.hasSubEnvelopes()) return this;
		for (TrajectoryEnvelope te : this.getGroundEnvelopes()) {
			if (te.getTemporalVariable().getEST() <= time) {
				if (te.getTemporalVariable().getEET() >= time) {
					return te;
				}
				else if (this.getGroundEnvelopes().higher(te).getTemporalVariable().getEST() > time) {
					return te;//this.getGroundEnvelopes().higher(te);
				}
			}
		}
		return null;
	}
	
	/**
	 * Get the super-envelope of this {@link TrajectoryEnvelope}.
	 * @return The super-envelope of this {@link TrajectoryEnvelope}.
	 */
	public TrajectoryEnvelope getSuperEnvelope() {
		return superEnvelope;
	}

	/**
	 * Get the {@link PoseSteering} along the {@link Trajectory} where the robot will be
	 * at a given time. Returns <code>null</code> if the robot will not be in this
	 * {@link TrajectoryEnvelope} at the given time.
	 * @param time The time at which to return the {@link PoseSteering}.
	 * @return The {@link PoseSteering} along the {@link Trajectory} where the robot will be
	 * at a given time.
	 */
	public PoseSteering getPoseSteering(long time) {
		long startTime = this.getTemporalVariable().getEST();
		long endTime = this.getTemporalVariable().getEET();
		if (time  < startTime || time > endTime) {
			return this.getTrajectory().getPoseSteering()[this.getTrajectory().getPoseSteering().length-1];
//			if (this.hasSuperEnvelope()) {
//				return this.getSuperEnvelope().getPoseSteering(time);
//			}
//			return null;
		}
		if (this.getReferencePathVariable().getShapeType().equals(PointDomain.class)) {
			return this.getTrajectory().getPoseSteering()[0];
		}
		
		long total = endTime-startTime;
		long soFar = time-startTime;
		double percent = ((double)soFar)/((double)total);
		
		double totDistance = ((LineStringDomain)this.getReferencePathVariable().getDomain()).getGeometry().getLength();
		double scannedDistance = 0.0;
		PoseSteering previousPS = null;
		PoseSteering currentPS = null;
		double previousRatio = 0.0;
		double currentRatio = 0.0;
		int index = 0;
		while (scannedDistance/totDistance < percent) {
			previousRatio = scannedDistance/totDistance;
			previousPS = this.getTrajectory().getPoseSteering()[index];
			currentPS = this.getTrajectory().getPoseSteering()[++index];
			scannedDistance += new Coordinate(currentPS.getPose().getX(),currentPS.getPose().getY()).distance(new Coordinate(previousPS.getPose().getX(),previousPS.getPose().getY()));
			currentRatio = scannedDistance/totDistance;
		}
		if (previousPS == null) return this.getTrajectory().getPoseSteering()[0];
		double ratio = (percent-previousRatio)/(currentRatio-previousRatio);
		PoseSteering ret = previousPS.interpolate(currentPS, ratio);
		return ret;
	}

//	public PoseSteering getPoseSteering(long time) {
//		long startTime = this.getTemporalVariable().getEST();
//		long endTime = this.getTemporalVariable().getEET();
//		if (time  < startTime || time > endTime) return null;
//		int index = 0;
//		int previousIndex = 0;
//		long scannedTime = startTime;
//		long prevTime = startTime;
//		while (time > scannedTime) {
//			try {
//				prevTime = scannedTime;
//				previousIndex = index;
//				scannedTime += (long)(this.getTrajectory().getDTs()[++index]*RESOLUTION);
//			}
//			catch(ArrayIndexOutOfBoundsException e) {
//				return this.getTrajectory().getPoseSteering()[--index];
//			}
//		}
//		PoseSteering previousPS = this.getTrajectory().getPoseSteering()[previousIndex];
//		PoseSteering nextPS = this.getTrajectory().getPoseSteering()[index];
//		double ratio = ((double)time-(double)prevTime)/((double)scannedTime-(double)prevTime);
//		return previousPS.interpolate(nextPS, ratio);
//	}
	
	/**
	 * Get the start and end times of the path points representing beginning and ending of
	 * ground {@link TrajectoryEnvelope}s of this {@link TrajectoryEnvelope}.
	 * @return The start and end times of the path points representing beginning and ending of
	 * ground {@link TrajectoryEnvelope}s of this {@link TrajectoryEnvelope}.
	 */
	public double[] getCTs() {
		double[] ret = this.createCTVector();
		TreeSet<TrajectoryEnvelope> rets = this.getGroundEnvelopes();
		int counter = 0;
		for (TrajectoryEnvelope te : rets) {
			ret[counter] = (double)(te.getTemporalVariable().getEST())/RESOLUTION;
			ret[counter+te.getPathLength()-1] = (double)(te.getTemporalVariable().getEET())/RESOLUTION;
			counter += te.getPathLength();
		}
		return ret;
	}
	
	/**
	 * Set the robot ID of this {@link TrajectoryEnvelope}.
	 * @param robotID The robot ID of this {@link TrajectoryEnvelope}.
	 */
	public void setRobotID(int robotID) {
		this.robotID = robotID;
	}
	
	/**
	 * Get the robot ID of this {@link TrajectoryEnvelope}.
	 * @return The robot ID of this {@link TrajectoryEnvelope}.
	 */
	public int getRobotID() {
		return this.robotID;
	}
	
	/**
	 * Set the super-envelope of this {@link TrajectoryEnvelope}.
	 * @param superEnvelope The super-envelope of this {@link TrajectoryEnvelope}.
	 */
	public void setSuperEnvelope(TrajectoryEnvelope superEnvelope) {
		this.superEnvelope = superEnvelope;
		//If this envelope has a super envelope, then compute sequenceNumberStart and sequenceNumberEnd
		this.updateSequenceNumbers();
	}
	
	private void updateSequenceNumbers() {
		if (!this.hasSuperEnvelope()) {
			this.sequenceNumberStart = 0;
			this.sequenceNumberEnd = this.getTrajectory().getPose().length-1;
		}
		else {
			TrajectoryEnvelope superEnv = this;
			while (superEnv.hasSuperEnvelope()) superEnv = superEnv.getSuperEnvelope();
			Coordinate[] psSuperEnv = superEnv.getTrajectory().getPositions();
			Coordinate[] psThis = this.getTrajectory().getPositions();
			this.sequenceNumberStart = 0;
			while (!psSuperEnv[this.sequenceNumberStart].equals(psThis[0])) this.sequenceNumberStart++;
			this.sequenceNumberEnd = this.sequenceNumberStart+this.getTrajectory().getPositions().length-1;
		}
	}
	
	/**
	 * Assess whether this {@link TrajectoryEnvelope} has a super-envelope.
	 * @return <code>true</code> iff this {@link TrajectoryEnvelope} has a super-envelope.
	 */
	public boolean hasSuperEnvelope() {
		return this.superEnvelope != null;
	}

	/**
	 * Set whether this {@link TrajectoryEnvelope} can be refined into sub-envelopes.
	 * @param refinable Whether this {@link TrajectoryEnvelope} can be refined into sub-envelopes.
	 */
	public void setRefinable(boolean refinable) {
		this.refinable = refinable;
	}
	
	/**
	 * Assess whether this {@link TrajectoryEnvelope} can be refined into sub-envelopes.
	 * @return <code>true</code> iff this {@link TrajectoryEnvelope} can be refined into sub-envelopes.
	 */
	public boolean getRefinable() {
		return this.refinable;
	}
	
	@Override
	public int compareTo(Variable o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected Constraint[] createInternalConstraints(Variable[] variables) {
		// TODO Auto-generated method stub
		return null;
	}
		

//	private Coordinate[] createEnvelope() {
//		ArrayList<Coordinate> ret = new ArrayList<Coordinate>();
//		for (PoseSteering ps : this.path.getPoseSteering()) {
//			GeometricShapeFactory gsf = new GeometricShapeFactory();
//			gsf.setHeight(width);
//			gsf.setWidth(length);
//			gsf.setCentre(new Coordinate(ps.getX(),ps.getY()));
//			gsf.setRotation(ps.getTheta());
//			Polygon rect = gsf.createRectangle();
//			for (Coordinate coor : rect.getCoordinates()) ret.add(coor);
//		}
//		return ret.toArray(new Coordinate[ret.size()]);
//	}

//	private Coordinate[] createEnvelope() {
//		ArrayList<Polygon> ret = new ArrayList<Polygon>();
//		for (PoseSteering ps : this.path.getPoseSteering()) {
//			GeometricShapeFactory gsf = new GeometricShapeFactory();
//			gsf.setHeight(width);
//			gsf.setWidth(length);
//			gsf.setCentre(new Coordinate(ps.getX(),ps.getY()));
//			gsf.setRotation(ps.getTheta());
//			Polygon rect = gsf.createRectangle();
//			ret.add(rect);
//		}
//		GeometryFactory gf = new GeometryFactory();
//		MultiPolygon combined = new MultiPolygon(ret.toArray(new Polygon[ret.size()]), gf);
//		return combined.getCoordinates();
//	}


//	private Coordinate[] createEnvelope() {
//		Geometry onePoly = null;
//		for (PoseSteering ps : this.path.getPoseSteering()) {
//			GeometricShapeFactory gsf = new GeometricShapeFactory();
//			gsf.setHeight(width);
//			gsf.setWidth(length);
//			gsf.setCentre(new Coordinate(ps.getX(),ps.getY()));
//			gsf.setRotation(ps.getTheta());
//			Polygon rect = gsf.createRectangle();
//			if (onePoly == null) onePoly = rect;
//			else onePoly = onePoly.union(rect);
//		}
//		return onePoly.getCoordinates();
//	}

	/**
	 * Returns a {@link Geometry} representing the footprint of the robot in a given {@link PoseSteering}.
	 * @param ps The pose and steering used to create the footprint.
	 * @return A {@link Geometry} representing the footprint of the robot in a given {@link PoseSteering}.
	 */
	public Geometry makeFootprint(PoseSteering ps) {
		return makeFootprint(ps.getX(), ps.getY(), ps.getTheta());
	}
	
	/**
	 * Returns a {@link Geometry} representing the footprint of the robot in a given pose.
	 * @param x The x coordinate of the pose used to create the footprint.
	 * @param y The y coordinate of the pose used to create the footprint.
	 * @param theta The orientation of the pose used to create the footprint.
	 * @return A {@link Geometry} representing the footprint of the robot in a given pose.
	 */
	public Geometry makeFootprint(double x, double y, double theta) {
		AffineTransformation at = new AffineTransformation();
		at.rotate(theta);
		at.translate(x,y);
		Geometry rect = at.transform(footprint);
		return rect;
	}
	
	private void initFootprint(double width, double length, double deltaW, double deltaL) {
		GeometricShapeFactory gsf = new GeometricShapeFactory();
		gsf.setHeight(width);
		gsf.setWidth(length);
		gsf.setCentre(new Coordinate(deltaL,deltaW));
		footprint = gsf.createRectangle();
	}

	private void initFootprint(Coordinate ... coords) {
		GeometryFactory gf = new GeometryFactory();
		Coordinate[] newCoords = new Coordinate[coords.length+1];
		for (int i = 0; i < coords.length; i++) {
			newCoords[i] = coords[i];
		}
		newCoords[newCoords.length-1] = coords[0];
		footprint = gf.createPolygon(newCoords);
	}

	private Coordinate[] createEnvelope() {
		Geometry onePoly = null;
		Geometry prevPoly = null;
		for (PoseSteering ps : this.trajectory.getPoseSteering()) {
			Geometry rect = makeFootprint(ps);
			if (onePoly == null) {
				onePoly = rect;
				prevPoly = rect;
			}
			else {
				Geometry auxPoly = prevPoly.union(rect);
				onePoly = onePoly.union(auxPoly.convexHull());
				prevPoly = rect;
			}
		}
//		Geometry ret = GeometryPrecisionReducer.reduce(onePoly, new PrecisionModel(PrecisionModel.FLOATING_SINGLE));
//		return ret.getCoordinates();
		return onePoly.getCoordinates();
	}

	/**
	 * Set the {@link Trajectory} of this {@link TrajectoryEnvelope}.
	 * @param traj The {@link Trajectory} of this {@link TrajectoryEnvelope}.
	 */
	public void setTrajectory(Trajectory traj) {
		if (this.footprint == null) {
			throw new NoFootprintException("No footprint set for " + this + ", please specify one before setting the trajecotry.");
		}
		this.trajectory = traj;
		if (traj.getPoseSteering().length == 1) {
			PointDomain pd = new PointDomain(this, traj.getPositions()[0]);
			this.setDomain(pd);
		}
		else {
			LineStringDomain lsd = new LineStringDomain(this,traj.getPositions());
			this.setDomain(lsd);
		}
		PolygonalDomain env = new PolygonalDomain(this,createEnvelope());
//		PolygonalDomain newEnv = new PolygonalDomain(this, env.getGeometry().convexHull().getCoordinates());
		this.setDomain(env);
		long minDuration = 0;
		for (int i = 0; i < traj.getDTs().length; i++) {
			minDuration += traj.getDTs()[i]*RESOLUTION;
		}
		AllenIntervalConstraint duration = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(minDuration,APSPSolver.INF));
		duration.setFrom(this);
		duration.setTo(this);
		boolean conAdd = this.getConstraintSolver().addConstraint(duration);
		if (conAdd) logger.fine("Added duration constriant " + duration);
		else logger.severe("Failed to add duration constriant " + duration);
		
		//If this envelope has a super envelope, then compute sequenceNumberStart and sequenceNumberEnd
		this.updateSequenceNumbers();
	}
	
	/**
	 * Get the start sequence number of this {@link TrajectoryEnvelope} (0 if this envelope has
	 * no super envelopes).
	 * @return The starting sequence number of this {@link TrajectoryEnvelope} (0 if this envelope has
	 * no super envelopes).
	 */
	public int getSequenceNumberStart() {
		return sequenceNumberStart;
	}

	/**
	 * Get the end sequence number of this {@link TrajectoryEnvelope} (length of trajectory -1 if this envelope has
	 * no super envelopes).
	 * @return The end sequence number of this {@link TrajectoryEnvelope} (length of trajectory -1 if this envelope has
	 * no super envelopes).
	 */
	public int getSequenceNumberEnd() {
		return sequenceNumberEnd;
	}
	
	/**
	 * Get the {@link Trajectory} of this {@link TrajectoryEnvelope}.
	 * @return The {@link Trajectory} of this {@link TrajectoryEnvelope}.
	 */
	public Trajectory getTrajectory() {
		return trajectory;
	}
	
//	public AllenIntervalConstraint getDurationConstriant() {
//		AllenIntervalConstraint ret = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds((long)(this.getTrajectory().getMinTraversalTime()*1000.0), APSPSolver.INF));
//		ret.setFrom(this);
//		ret.setTo(this);
//		return ret;
//	}
	
	/**
	 * Get the length of the {@link Trajectory} underlying this {@link TrajectoryEnvelope}.
	 * @return The length of the {@link Trajectory} underlying this {@link TrajectoryEnvelope}.
	 */
	public int getPathLength() {
		return this.trajectory.getPoseSteering().length;
	}
	
	@Override
	public void setDomain(Domain d) {
		if (d instanceof LineStringDomain || d instanceof PointDomain) {
			this.getInternalVariables()[1].setDomain(d);
		}
		else if (d instanceof PolygonalDomain) {
			this.getInternalVariables()[2].setDomain(d);
		}
	}

	@Override
	public String toString() {
		String ret = "TrajectoryEnvelope " + this.id + " (Robot " + this.robotID + ", SE " + this.getEnvelopeVariable().getID() + ") [" + this.getSequenceNumberStart() + ";" + this.getSequenceNumberEnd() + "]";
		if (this instanceof Activity && ((Activity)this).getSymbols() != null && ((Activity)this).getSymbols().length > 0) ret += (" " + ((Activity)this).getSymbols()[0]);
		return ret;
	}
		
//	@Override
//	public String toString() {
//		TreeSet<TrajectoryEnvelope> subEnv = new TreeSet<TrajectoryEnvelope>();
//		if (this.getSubEnvelopes() != null) subEnv = this.getGroundEnvelopes();
//		String ret = "TrajectoryEnvelope " + this.id + " (Robot " + this.robotID + ")";
//		for (TrajectoryEnvelope te : subEnv) {
//			ret += "\n   " + te;
//		}
//		return ret;
//	}
	
	/**
	 * Returns the temporal part of this {@link TrajectoryEnvelope}.
	 * @return An {@link AllenInterval} representing the temporal part of this {@link TrajectoryEnvelope}.
	 */
	public AllenInterval getTemporalVariable() {
		//return (AllenInterval)this.getInternalVariables()[0];
		return ((SymbolicVariableActivity)this.getInternalVariables()[0]).getTemporalVariable();
	}

	/**
	 * Returns the spatial part of this {@link TrajectoryEnvelope} (reference path or point). Returns a
	 * {@link GeometricShapeVariable} whose domain is either a {@link LineStringDomain} or a {@link PointDomain}.
	 * @return A {@link GeometricShapeVariable} representing the spatial part of this {@link TrajectoryEnvelope}.
	 */
	public GeometricShapeVariable getReferencePathVariable() {
		return (GeometricShapeVariable)this.getInternalVariables()[1];
	}

	/**
	 * Returns the spatial part of this {@link TrajectoryEnvelope} (spatial envelope).
	 * @return A {@link GeometricShapeVariable} representing the spatial part of this {@link TrajectoryEnvelope}.
	 */
	public GeometricShapeVariable getEnvelopeVariable() {
		return (GeometricShapeVariable)this.getInternalVariables()[2];
	}
	
	/**
	 * Returns the symbolic part of this {@link TrajectoryEnvelope}.
	 * @return The symbolic part of this {@link TrajectoryEnvelope}.
	 */
	public SymbolicVariableActivity getSymbolicVariableActivity() {
		return (SymbolicVariableActivity)this.getInternalVariables()[0];
	}

	@Override
	public String[] getSymbols() {
		//return new String[] {((GeometricShapeDomain)getEnvelopeVariable().getDomain()).getGeometry().getCoordinates().length+""};
		return ((SymbolicVariableActivity)this.getInternalVariables()[0]).getSymbols();
	}

	@Override
	public Variable getVariable() {
		return this;
	}

}
