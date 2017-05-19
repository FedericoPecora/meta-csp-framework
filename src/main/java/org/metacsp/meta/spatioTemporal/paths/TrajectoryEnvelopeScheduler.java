/*******************************************************************************
 * Copyright (c) 2010-2013 Federico Pecora <federico.pecora@oru.se>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package org.metacsp.meta.spatioTemporal.paths;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeSet;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.framework.meta.MetaConstraintSolver;
import org.metacsp.framework.meta.MetaVariable;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.spatial.DE9IM.DE9IMRelation;
import org.metacsp.multi.spatial.DE9IM.GeometricShapeDomain;
import org.metacsp.multi.spatial.DE9IM.GeometricShapeVariable;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.metacsp.multi.spatioTemporal.paths.PoseSteering;
import org.metacsp.multi.spatioTemporal.paths.Trajectory;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelope;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelopeSolver;
import org.metacsp.multi.symbols.SymbolicValueConstraint;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.UI.JTSDrawingPanel;
import org.metacsp.utility.UI.TrajectoryEnvelopeAnimator;

import cern.colt.Arrays;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * This class is used to backtrack over {@link TrajectoryEnvelope} conflicts (see the {@link Map} meta-constraint).
 * @author Federico Pecora
 *
 */
public class TrajectoryEnvelopeScheduler extends MetaConstraintSolver {

	//private ArrayList<TrajectoryEnvelope> envelopesForScheduling = new ArrayList<TrajectoryEnvelope>();
	private static final long serialVersionUID = 8551829132754804513L;
	private HashMap<TrajectoryEnvelope,ArrayList<TrajectoryEnvelope>> refinedWith = new HashMap<TrajectoryEnvelope, ArrayList<TrajectoryEnvelope>>();
	private static final int MINIMUM_SIZE = 5;

	/**
	 * Create a {@link TrajectoryEnvelopeScheduler} with a given origin and temporal horizon.
	 * @param origin The origin of time.
	 * @param horizon The temporal horizon.
	 * @param animationTime Time between backtracking (0 if none).
	 */
	public TrajectoryEnvelopeScheduler(long origin, long horizon, long animationTime) {
		super(new Class[] {AllenIntervalConstraint.class, DE9IMRelation.class}, animationTime, new TrajectoryEnvelopeSolver(origin, horizon));
	}

	/**
	 * Create a {@link TrajectoryEnvelopeScheduler} with a given origin and temporal horizon.
	 * @param origin The origin of time.
	 * @param horizon The temporal horizon.
	 */
	public TrajectoryEnvelopeScheduler(long origin, long horizon) {
		super(new Class[] {AllenIntervalConstraint.class, DE9IMRelation.class}, 0, new TrajectoryEnvelopeSolver(origin, horizon));
	}
	
	/**
	 * Get the dependency graph of trajectory envelopes from the resolving constraints added by this
	 * {@link TrajectoryEnvelopeScheduler}.
	 * @return A directed graph where each edge (x,y) has a label {p1,p2} indicating that
	 * robot x.robotID has to wait at path point p1 for robot y.robotID to reach path point p2.
	 */
	public DirectedSparseMultigraph<TrajectoryEnvelope,Integer[]> getCurrentDependencies() {
		DirectedSparseMultigraph<TrajectoryEnvelope,Integer[]> depGraph = new DirectedSparseMultigraph<TrajectoryEnvelope,Integer[]>();
		ConstraintNetwork cn = this.getConstraintSolvers()[0].getConstraintNetwork();
		Constraint[] cons = cn.getConstraints();
		for (Constraint con : cons) {
			if (con instanceof AllenIntervalConstraint) {
				AllenIntervalConstraint aic = (AllenIntervalConstraint)con;
				if (aic.getTypes()[0].equals(AllenIntervalConstraint.Type.BeforeOrMeets)) {

					//The two TEs involved in the constraint
					TrajectoryEnvelope mustWaitToStart = (TrajectoryEnvelope)aic.getTo();
					TrajectoryEnvelope mustFinishBeforeOtherCanStart = (TrajectoryEnvelope)aic.getFrom();
					TrajectoryEnvelope waitingEnvelope = null;

					//Find waitingEnvelope = previous of mustWaitToStart
					TrajectoryEnvelope root = mustWaitToStart;
					while (root.hasSuperEnvelope()) root = root.getSuperEnvelope();
					for (Variable depVar : root.getRecursivelyDependentVariables()) {
						TrajectoryEnvelope depTE = (TrajectoryEnvelope)depVar;
						if (!depTE.hasSubEnvelopes() && depTE.getTrajectory().getSequenceNumberEnd() == mustWaitToStart.getSequenceNumberStart()-1) {
							waitingEnvelope = depTE;
							break;
						}
					}

					//Calculate waiting points
					Integer thresholdPoint = mustFinishBeforeOtherCanStart.getTrajectory().getSequenceNumberEnd();
					Integer waitingPoint = null;
					
					//If there was no previous envelope, then make the robot stay in the start point of this one
					if (waitingEnvelope == null) {
						//System.out.println("IGNORE THIS DEPENDENCY, the following robot is parked anyway " + mustWaitToStart);
						waitingPoint = mustWaitToStart.getTrajectory().getSequenceNumberStart();
						waitingEnvelope = mustWaitToStart;
					}
					else {
						waitingPoint = waitingEnvelope.getTrajectory().getSequenceNumberEnd();
					}
					
					//Add edge in dep graph
					ArrayList<TrajectoryEnvelope> verts = new ArrayList<TrajectoryEnvelope>();
					verts.add(waitingEnvelope);
					verts.add(mustFinishBeforeOtherCanStart);
					depGraph.addVertex(waitingEnvelope);
					depGraph.addVertex(mustFinishBeforeOtherCanStart);
					depGraph.addEdge(new Integer[] {waitingPoint,thresholdPoint}, waitingEnvelope, mustFinishBeforeOtherCanStart);
				}
			}
		}
		return depGraph;
	}
	
	/**
	 * Create a {@link TrajectoryEnvelopeScheduler} with a given origin and temporal horizon.
	 * @param origin The origin of time.
	 * @param horizon The temporal horizon.
	 * @param maxTrajectories The maximum number of {@link TrajectoryEnvelope}s that can be created with this solver.
	 */
	public TrajectoryEnvelopeScheduler(long origin, long horizon, int maxTrajectories) {
		super(new Class[] {AllenIntervalConstraint.class, DE9IMRelation.class}, 0, new TrajectoryEnvelopeSolver(origin, horizon, maxTrajectories));
	}

	@Override
	public void preBacktrack() {
		// TODO Auto-generated method stub

	}
	
	@Override
	protected void retractResolverSub(ConstraintNetwork metaVariable, ConstraintNetwork metaValue) {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean addResolverSub(ConstraintNetwork metaVariable,
			ConstraintNetwork metaValue) {
		return true;

	}
	
	private int BTcounter = 0;
	public int getBTcounter() {
		return BTcounter;
	}
	
	@Override
	public void postBacktrack(MetaVariable mv) {
		BTcounter++;
	}

	@Override
	protected double getUpperBound() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void setUpperBound() {
		// TODO Auto-generated method stub

	}

	@Override
	protected double getLowerBound() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void setLowerBound() {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean hasConflictClause(ConstraintNetwork metaValue) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void resetFalseClause() {
		// TODO Auto-generated method stub

	}	

	public ConstraintNetwork refineTrajectoryEnvelopesFixed(int maxSize) {
		ConstraintNetwork ret = new ConstraintNetwork(null);
		
		TrajectoryEnvelopeSolver solver = (TrajectoryEnvelopeSolver)this.getConstraintSolvers()[0];
		Variable[] vars = solver.getVariables();
		for (Variable var : vars) {
			ArrayList<Trajectory> newTrajectories = new ArrayList<Trajectory>();
			TrajectoryEnvelope te = (TrajectoryEnvelope)var;
			if (te.getRefinable()) {
				te.setRefinable(false);
				if (!te.hasSubEnvelopes() && te.getTrajectory().getPose().length > 3) {
					logger.info("Refining " + te);
					Trajectory traj = te.getTrajectory();
					int soFar = 0;
					while (soFar < traj.getPose().length) {
						int pieceLength = Math.min(maxSize, traj.getPose().length-soFar);
						if (pieceLength == 0) break;
						Pose[] piecePoses = new Pose[pieceLength];
						double[] dts = new double[pieceLength];
						//System.out.println("Piece length = " + pieceLength);
						for (int j = 0; j < piecePoses.length; j++) {
							piecePoses[j] = traj.getPose()[j+soFar];
							dts[j] = traj.getDTs()[j+soFar];
						}
						Trajectory piece = new Trajectory(piecePoses);
						piece.setDTs(dts);
						newTrajectories.add(piece);
						soFar += pieceLength;
					}
				}
				
				if (!newTrajectories.isEmpty()) {
					//Create new TEs
					ArrayList<TrajectoryEnvelope> newTrajectoryEnvelopes = new ArrayList<TrajectoryEnvelope>();
					Variable[] newVars = solver.createVariables(newTrajectories.size());
					for (int i = 0; i < newVars.length; i++) {
						TrajectoryEnvelope onete = (TrajectoryEnvelope)newVars[i];
						onete.setComponent(te.getComponent());
						onete.getSymbolicVariableActivity().setSymbolicDomain(te.getSymbols());
						onete.setFootprint(te.getFootprint());
						onete.setRefinable(false);
						onete.setTrajectory(newTrajectories.get(i));
						onete.setSuperEnvelope(te);
						onete.setRobotID(te.getRobotID());
						te.addSubEnvelope(onete);
						te.addDependentVariables(onete);
						newTrajectoryEnvelopes.add(onete);			
					}
					
					AllenIntervalConstraint starts = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Starts);
					starts.setFrom(newTrajectoryEnvelopes.get(0));
					starts.setTo(te);
					ret.addConstraint(starts);
		
					AllenIntervalConstraint finishes = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Finishes, new Bounds(1, APSPSolver.INF));
					finishes.setFrom(newTrajectoryEnvelopes.get(newTrajectoryEnvelopes.size()-1));
					finishes.setTo(te);
					ret.addConstraint(finishes);
					
					for (int i = 0; i < newTrajectoryEnvelopes.size()-1; i++) {
						TrajectoryEnvelope from = newTrajectoryEnvelopes.get(i);
						TrajectoryEnvelope to = newTrajectoryEnvelopes.get(i+1);
						double ttt = from.getTrajectory().getDTs()[from.getTrajectory().getDTs().length-1];
						long tttLong = (long)(TrajectoryEnvelope.RESOLUTION*ttt);
						if (tttLong == 0) throw new Error("Before bounds are 0! " + ttt);
						AllenIntervalConstraint before = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, new Bounds(tttLong,tttLong));
						before.setFrom(from);
						before.setTo(to);
						ret.addConstraint(before);
					}
				}
			}
		}

		if (ret.getConstraints() != null && ret.getConstraints().length > 0) {
			if (!solver.addConstraints(ret.getConstraints())) throw new Error("Failed to add temporal constraints in refinement!");
		}
		recomputeUsages();
		return ret;
	}
	
	public ConstraintNetwork refineTrajectoryEnvelopesFixed(double[] proportions) {
		ConstraintNetwork ret = new ConstraintNetwork(null);
		double sum = 0.0;
		for (double part : proportions) sum+=part;
		if (Math.abs(sum-1.0) > 0.00001) throw new Error("Proportions given for refinement must sum up to 1.0: " + Arrays.toString(proportions));
		
		TrajectoryEnvelopeSolver solver = (TrajectoryEnvelopeSolver)this.getConstraintSolvers()[0];
		Variable[] vars = solver.getVariables();
		for (Variable var : vars) {
			ArrayList<Trajectory> newTrajectories = new ArrayList<Trajectory>();
			TrajectoryEnvelope te = (TrajectoryEnvelope)var;
			if (te.getRefinable()) {
				te.setRefinable(false);
				if (!te.hasSubEnvelopes() && te.getTrajectory().getPose().length > 3) {
					logger.info("Refining " + te);
					Trajectory traj = te.getTrajectory();
					int soFar = 0;
					for (int i = 0; i < proportions.length; i++) {
						int pieceLength = (int)(traj.getPose().length*proportions[i]);
						if (i == proportions.length-1) pieceLength = traj.getPose().length-soFar;
						if (pieceLength == 0) break;
						Pose[] piecePoses = new Pose[pieceLength];
						double[] dts = new double[pieceLength];
						//System.out.println("Piece length = " + pieceLength);
						for (int j = 0; j < piecePoses.length; j++) {
							piecePoses[j] = traj.getPose()[j+soFar];
							dts[j] = traj.getDTs()[j+soFar];
						}
						Trajectory piece = new Trajectory(piecePoses);
						piece.setDTs(dts);
						newTrajectories.add(piece);
						soFar += pieceLength;
					}
	
				}
				
				if (!newTrajectories.isEmpty()) {
					//Create new TEs
					ArrayList<TrajectoryEnvelope> newTrajectoryEnvelopes = new ArrayList<TrajectoryEnvelope>();
					Variable[] newVars = solver.createVariables(newTrajectories.size());
					for (int i = 0; i < newVars.length; i++) {
						TrajectoryEnvelope onete = (TrajectoryEnvelope)newVars[i];
						onete.setComponent(te.getComponent());
						onete.getSymbolicVariableActivity().setSymbolicDomain(te.getSymbols());
						onete.setFootprint(te.getFootprint());
						onete.setRefinable(false);
						onete.setTrajectory(newTrajectories.get(i));
						onete.setSuperEnvelope(te);
						onete.setRobotID(te.getRobotID());
						te.addSubEnvelope(onete);
						te.addDependentVariables(onete);
						newTrajectoryEnvelopes.add(onete);			
					}
					
					AllenIntervalConstraint starts = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Starts);
					starts.setFrom(newTrajectoryEnvelopes.get(0));
					starts.setTo(te);
					ret.addConstraint(starts);
		
					AllenIntervalConstraint finishes = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Finishes, new Bounds(1, APSPSolver.INF));
					finishes.setFrom(newTrajectoryEnvelopes.get(newTrajectoryEnvelopes.size()-1));
					finishes.setTo(te);
					ret.addConstraint(finishes);
					
					for (int i = 0; i < newTrajectoryEnvelopes.size()-1; i++) {
						TrajectoryEnvelope from = newTrajectoryEnvelopes.get(i);
						TrajectoryEnvelope to = newTrajectoryEnvelopes.get(i+1);
						double ttt = from.getTrajectory().getDTs()[from.getTrajectory().getDTs().length-1];
						long tttLong = (long)(TrajectoryEnvelope.RESOLUTION*ttt);
						if (tttLong == 0) throw new Error("Before bounds are 0! " + ttt);
						AllenIntervalConstraint before = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, new Bounds(tttLong,tttLong));
						before.setFrom(from);
						before.setTo(to);
						ret.addConstraint(before);
					}
				}
			}
		}

		if (ret.getConstraints() != null && ret.getConstraints().length > 0) {
			if (!solver.addConstraints(ret.getConstraints())) throw new Error("Failed to add temporal constraints in refinement!");
		}
		recomputeUsages();
		return ret;
	}
	
	/**
	 * Refine the {@link TrajectoryEnvelope}s maintained by the {@link TrajectoryEnvelopeSolver} underlying this
	 * {@link TrajectoryEnvelopeScheduler}. This method splits {@link TrajectoryEnvelope}s that overlap in space.
	 * @return A {@link ConstraintNetwork} containing the set of {@link TrajectoryEnvelope}s into which the existing
	 * {@link TrajectoryEnvelope}s were refined. This {@link ConstraintNetwork} is empty if the existing {@link TrajectoryEnvelope}s
	 * cannot be refined any further.
	 */
	public ConstraintNetwork refineTrajectoryEnvelopes() {
		ConstraintNetwork ret = new ConstraintNetwork(null);
		
		boolean done = false;
		while (!done) {
			done = true;
			Variable[] varsOneIteration = this.getConstraintSolvers()[0].getVariables();
			for (int i = 0; i < varsOneIteration.length-1; i++) {
				for (int j = i+1; j < varsOneIteration.length; j++) {

					//Get TEs
					TrajectoryEnvelope te1 = (TrajectoryEnvelope)varsOneIteration[i];
					TrajectoryEnvelope te2 = (TrajectoryEnvelope)varsOneIteration[j];

					//Init data structures
					if (!refinedWith.containsKey(te1)) refinedWith.put(te1,new ArrayList<TrajectoryEnvelope>());
					if (!refinedWith.containsKey(te2)) refinedWith.put(te2,new ArrayList<TrajectoryEnvelope>());

					// If != robots
					boolean te1HasSub = te1.hasSubEnvelopes();
					boolean te2HasSub = te2.hasSubEnvelopes();
					if (te1.getRobotID() != te2.getRobotID()) {
						//if they intersect
						GeometricShapeVariable poly1 = te1.getEnvelopeVariable();
						GeometricShapeVariable poly2 = te2.getEnvelopeVariable();
						Geometry shape1 = ((GeometricShapeDomain)poly1.getDomain()).getGeometry();
						Geometry shape2 = ((GeometricShapeDomain)poly2.getDomain()).getGeometry();
						if (shape1.intersects(shape2)) {
//							logger.info("===>Refinement 1: " + (!te2HasSub && te1.getRefinable() && !refinedWith.get(te1).contains(te2))+
//									" Refinement 2: "+(!te1HasSub && te2.getRefinable() && !refinedWith.get(te2).contains(te1)));
							if (!te2HasSub && te1.getRefinable() && !refinedWith.get(te1).contains(te2)) {
								ConstraintNetwork ref1 = refineTrajectoryEnvelopes(te1, te2);
								refinedWith.get(te1).add(te2);
								ret.join(ref1);
								done = false;
							}
							if (!te1HasSub && te2.getRefinable() && !refinedWith.get(te2).contains(te1)) {
								ConstraintNetwork ref2 = refineTrajectoryEnvelopes(te2, te1);
								refinedWith.get(te2).add(te1);
								ret.join(ref2);
								done = false;
							}
						}
					}
					//we have refined (i,j) with each other
//					logger.info("Refined " + te1 + " into " + te1.getGroundEnvelopes().size() + " envelopes");
//					logger.info("Refined " + te2 + " into " + te2.getGroundEnvelopes().size() + " envelopes");
				}
			}			
		}
		recomputeUsages();
		return ret;
	}
	
	private void recomputeUsages() {
		//recompute usages
		for (Variable v : this.getConstraintSolvers()[0].getVariables()) {
			TrajectoryEnvelope te = (TrajectoryEnvelope)v;
			((Map)this.getMetaConstraints()[0]).removeUsage(te);
			logger.finest("Removed usage of " + te);
			
//			TrajectoryEnvelope te = (TrajectoryEnvelope)v;
//			if (!te.hasSuperEnvelope()) {
//				TreeSet<TrajectoryEnvelope> gevs = te.getGroundEnvelopes(); 
//				if (!gevs.isEmpty()) {
//					((Map)this.getMetaConstraints()[0]).removeUsage(te);		
//					logger.finest("Removed usage of " + te);
//				}
//				for (TrajectoryEnvelope gte : gevs) {
//					((Map)this.getMetaConstraints()[0]).setUsage(gte);
//					logger.finest("Set usage of " + gte);
//				}
//			}
		}
		for (Variable v : this.getConstraintSolvers()[0].getVariables()) {
			TrajectoryEnvelope te = (TrajectoryEnvelope)v;
			if (!te.hasSuperEnvelope()) {
				for (Variable depVar : te.getRecursivelyDependentVariables()) {
					TrajectoryEnvelope subTE = (TrajectoryEnvelope)depVar;
					if (!subTE.hasSubEnvelopes()) {
						((Map)this.getMetaConstraints()[0]).setUsage(subTE);
						logger.finest("Set usage of " + subTE);						
					}
				}
			}
		}
	}
	
	/**
	 * Refine the {@link TrajectoryEnvelope}s maintained by the {@link TrajectoryEnvelopeSolver} underlying this
	 * {@link TrajectoryEnvelopeScheduler}. This method does not actually split {@link TrajectoryEnvelope}s.
	 * @return A {@link ConstraintNetwork} containing the set of {@link TrajectoryEnvelope}s into which the existing
	 * {@link TrajectoryEnvelope}s were refined. This {@link ConstraintNetwork} is empty if the existing {@link TrajectoryEnvelope}s
	 * cannot be refined any further.
	 */
	public ConstraintNetwork refineTrajectoryEnvelopesLight() {
		ConstraintNetwork ret = new ConstraintNetwork(null);		
		//recompute usages
//		for (TrajectoryEnvelope te : envelopesForScheduling) {
//			((Map)this.getMetaConstraints()[0]).removeUsage(te);		
//		}
		for (Variable v : this.getConstraintSolvers()[0].getVariables()) {
			TrajectoryEnvelope te = (TrajectoryEnvelope)v;
			if (!te.hasSuperEnvelope()) {
				for (TrajectoryEnvelope gte : te.getGroundEnvelopes()) {
					((Map)this.getMetaConstraints()[0]).setUsage(gte);
				}
			}
		}
		return ret;
	}

	private ConstraintNetwork refineTrajectoryEnvelopes(TrajectoryEnvelope var1, TrajectoryEnvelope var2) {

		TrajectoryEnvelopeSolver solver = (TrajectoryEnvelopeSolver)this.getConstraintSolvers()[0];
		ConstraintNetwork toReturn = new ConstraintNetwork(null);
		
		if (var1.getPathLength() < MINIMUM_SIZE) return toReturn;
		
		GeometryFactory gf = new GeometryFactory();
		Geometry se1 = ((GeometricShapeDomain)var1.getEnvelopeVariable().getDomain()).getGeometry();
		Geometry se2 = ((GeometricShapeDomain)var2.getEnvelopeVariable().getDomain()).getGeometry();
		Geometry intersectionse1se2 = se1.intersection(se2);
		boolean useDefaultEnvelopeChunks = false;
		
		if (!intersectionse1se2.isValid()) {
			intersectionse1se2 = intersectionse1se2.symDifference(intersectionse1se2.getBoundary());
			logger.info("Intersection " + var1 + " with " + var2 + " invalid - fixing");
		}

		if (intersectionse1se2 instanceof MultiPolygon) {
			logger.info("Intersection " + var1 + " with " + var2 + " too complex - using default segmentation");
			useDefaultEnvelopeChunks = true;
			//return toReturn;								
		}
		
		boolean in  = false;
		int countIn = 0;
		for (int i = 0; i < var1.getPathLength(); i++) {
			Coordinate coord = var1.getTrajectory().getPositions()[i];
			Point point = gf.createPoint(coord);
			if (intersectionse1se2.contains(point) && !in) {
				in = true;
				if (++countIn > 1) {
					logger.info("Reference path of " + var1 + " enters intersection with " + var2 + " multiple times - using default segmentation");
					useDefaultEnvelopeChunks = true;
					break;
					//return toReturn;					
				}
			}
			if (!intersectionse1se2.contains(point)) {
				in = false;
			}
		}

		double areaDifference = intersectionse1se2.symDifference(intersectionse1se2.getBoundary()).union(se1).getArea()-se1.getArea();
		if (areaDifference > 0.001) {
			logger.info("Intersection " + var1 + " with " + var2 + " seems corrupt (area increased by " + areaDifference + ") - skipping ");
			useDefaultEnvelopeChunks = true;
			//return toReturn;											
		}

		// IRAN: UNOCMMENT THIS IF YOU HAVE PROBLEMS WITH SCHEDULING
//		if (!intersectionse1se2.coveredBy(se1)) {
//			logger.info("Intersection " + var1 + " with " + var2 + " is corrupted - skipping");
//			return toReturn;											
//		}

//		logger.info("Refining " + var1 + " with " + var2);

		ArrayList<PoseSteering> var1sec1 = new ArrayList<PoseSteering>();
		ArrayList<PoseSteering> var1sec2 = new ArrayList<PoseSteering>();
		ArrayList<PoseSteering> var1sec3 = new ArrayList<PoseSteering>();

		boolean skipSec1 = false;
		boolean skipSec3 = false;

		if (useDefaultEnvelopeChunks) {
			float percentageChunckOne = 0.30f;
			float percentageChunckTwo = 0.40f;
			for (int i = 0; i < var1.getPathLength(); i++) {
				PoseSteering ps = var1.getTrajectory().getPoseSteering()[i];
				if (i < var1.getPathLength()*percentageChunckOne) var1sec1.add(ps);
				else if (i < var1.getPathLength()*(percentageChunckOne+percentageChunckTwo)) var1sec2.add(ps);
				else var1sec3.add(ps);
			}
			logger.info("Using default chunk sizes " + var1sec1.size() + " / " + var1sec2.size() + " / " + var1sec3.size());
		}
		else {	
			for (int i = 0; i < var1.getPathLength(); i++) {
				Coordinate coord = var1.getTrajectory().getPositions()[i];
				PoseSteering ps = var1.getTrajectory().getPoseSteering()[i];
				Point point = gf.createPoint(coord);
				Geometry fp = var1.makeFootprint(ps);
				if (!intersectionse1se2.intersects(fp) && var1sec2.isEmpty()) {
					var1sec1.add(ps);
				}
				else if (intersectionse1se2.intersects(fp)) {
					var1sec2.add(ps);
				}
				else if (!intersectionse1se2.intersects(fp) && !var1sec2.isEmpty()) {
					var1sec3.add(ps);
				}
	//			if (!intersectionse1se2.contains(point) && var1sec2.isEmpty()) {
	//				var1sec1.add(ps);
	//			}
	//			else if (intersectionse1se2.contains(point)) {
	//				var1sec2.add(ps);
	//			}
	//			else if (!intersectionse1se2.contains(point) && !var1sec2.isEmpty()) {
	//				var1sec3.add(ps);
	//			}
			}
				
			//Add to start
			boolean done = false;
			while (!done) {
				try {
					Geometry lastPolySec1 = var1.makeFootprint(var1sec1.get(var1sec1.size()-1));
					if (lastPolySec1.disjoint(se2)) done = true;
					else {
						var1sec2.add(0,var1sec1.get(var1sec1.size()-1));
						var1sec1.remove(var1sec1.size()-1);
	//					logger.info("Added to start... (1)");
					}
				} catch (IndexOutOfBoundsException e) { skipSec1 = true; done = true; }
			}
			//If sec1 emptied, remove it
			if (var1sec1.size() < MINIMUM_SIZE) {
				while (var1sec1.size() > 0) {
					var1sec2.add(0,var1sec1.get(var1sec1.size()-1));
					var1sec1.remove(var1sec1.size()-1);
				}
				skipSec1 = true;
			}
	
			//Add to end
			done = false;
			while (!done) {
				try {
					Geometry firstPolySec3 = var1.makeFootprint(var1sec3.get(0));
					if (firstPolySec3.disjoint(se2)) done = true;
					else {
						var1sec2.add(var1sec3.get(0));
						var1sec3.remove(0);
	//					logger.info("Added to end... (1)");
					}
				} catch (IndexOutOfBoundsException e) { skipSec3 = true; done = true; }
			}
			//If sec3 emptied, remove it
			if (var1sec3.size() < MINIMUM_SIZE) {
				while (var1sec3.size() > 0) {
					var1sec2.add(var1sec3.get(0));
					var1sec3.remove(0);
				}
				skipSec3 = true;
			}
			
			if (var1sec2.size() < MINIMUM_SIZE) {
				if (var1sec1.size() > MINIMUM_SIZE) {
					var1sec2.add(0,var1sec1.get(var1sec1.size()-1));
					var1sec1.remove(var1sec1.size()-1);
	//				logger.info("Added to start... (2)");
				}
				else if (var1sec3.size() > MINIMUM_SIZE) {
					var1sec2.add(var1sec3.get(0));
					var1sec3.remove(0);				
	//				logger.info("Added to end... (2)");
				}
			}
	
			if ((skipSec1 && skipSec3) || (!skipSec1 && var1sec1.size() < MINIMUM_SIZE) || (!skipSec3 && var1sec3.size() < MINIMUM_SIZE) || var1sec2.size() < MINIMUM_SIZE) {
				logger.fine("Intersection " + var1 + " with " + var2 + " too small - skipping");
				return toReturn;
			}
		
		}

		var1.setRefinable(false);
		ArrayList<Trajectory> newTrajectories = new ArrayList<Trajectory>();
		ArrayList<TrajectoryEnvelope> newTrajectoryEnvelopes = new ArrayList<TrajectoryEnvelope>();
		
		if (!skipSec1) {
			newTrajectories.add(new Trajectory(var1sec1.toArray(new PoseSteering[var1sec1.size()]),var1.getTrajectory().getDts(0, var1sec1.size())));
			newTrajectories.add(new Trajectory(var1sec2.toArray(new PoseSteering[var1sec2.size()]),var1.getTrajectory().getDts(var1sec1.size(), var1sec1.size()+var1sec2.size())));
			if (!skipSec3) {
				newTrajectories.add(new Trajectory(var1sec3.toArray(new PoseSteering[var1sec3.size()]),var1.getTrajectory().getDts(var1sec1.size()+var1sec2.size(),var1.getTrajectory().getPoseSteering().length)));
			}
		}
		else {
			newTrajectories.add(new Trajectory(var1sec2.toArray(new PoseSteering[var1sec2.size()]),var1.getTrajectory().getDts(0, var1sec2.size())));
			if (!skipSec3) {
				newTrajectories.add(new Trajectory(var1sec3.toArray(new PoseSteering[var1sec3.size()]),var1.getTrajectory().getDts(var1sec2.size(),var1.getTrajectory().getPoseSteering().length)));
			}			
		}
		
		Variable[] newVars = solver.createVariables(newTrajectories.size());
		for (int i = 0; i < newVars.length; i++) {
			TrajectoryEnvelope te = (TrajectoryEnvelope)newVars[i];
			te.setComponent(var1.getComponent());
			te.getSymbolicVariableActivity().setSymbolicDomain(var1.getSymbols());
			//te.setFootprint(var1.getWidth(), var1.getLength(), var1.getDeltaW(), var1.getDeltaL());
			te.setFootprint(var1.getFootprint());
			//Only for second!
			if ((!skipSec1 && i == 1) || (skipSec1 && i == 0)) {
				te.setRefinable(false);
				refinedWith.get(var2).add(te);
			}
//			System.out.println("doing i = " + i + " skipsec1: " + skipSec1 + " skipsec3: " + skipSec3);
			te.setTrajectory(newTrajectories.get(i));
			te.setSuperEnvelope(var1);
			te.setRobotID(var1.getRobotID());
			var1.addSubEnvelope(te);
			var1.addDependentVariables(te);
			newTrajectoryEnvelopes.add(te);			
		}

		AllenIntervalConstraint starts = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Starts);
		starts.setFrom(newTrajectoryEnvelopes.get(0));
		starts.setTo(var1);
		toReturn.addConstraint(starts);

		AllenIntervalConstraint finishes = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Finishes);
		finishes.setFrom(newTrajectoryEnvelopes.get(newTrajectoryEnvelopes.size()-1));
		finishes.setTo(var1);
		toReturn.addConstraint(finishes);

		double minTTT12 = 0.0;
		
		if (!skipSec1) minTTT12 = var1.getTrajectory().getDTs()[var1sec1.size()];
		else minTTT12 = var1.getTrajectory().getDTs()[var1sec2.size()];
		long minTimeToTransition12 = (long)(TrajectoryEnvelope.RESOLUTION*minTTT12);
		AllenIntervalConstraint before1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, new Bounds(minTimeToTransition12,minTimeToTransition12));
		before1.setFrom(newTrajectoryEnvelopes.get(0));
		before1.setTo(newTrajectoryEnvelopes.get(1));
		toReturn.addConstraint(before1);
	
		if (newTrajectoryEnvelopes.size() > 2) {
			double minTTT23 = var1.getTrajectory().getDTs()[var1sec1.size()+var1sec2.size()];
			long minTimeToTransition23 = (long)(TrajectoryEnvelope.RESOLUTION*minTTT23);
			AllenIntervalConstraint before2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, new Bounds(minTimeToTransition23,minTimeToTransition23));
			before2.setFrom(newTrajectoryEnvelopes.get(1));
			before2.setTo(newTrajectoryEnvelopes.get(2));
			toReturn.addConstraint(before2);
		}

//		System.out.println("var1sec1 (" + skipSec1 + "): " + var1sec1);
//		System.out.println("var1sec2: " + var1sec2);
//		System.out.println("var1sec3 (" + skipSec3 + "): " + var1sec3);
//		System.out.println("DTs of var1sec2: " + Arrays.toString(var1.getTrajectory().getDts( var1sec2.size(),var1.getTrajectory().getDTs().length-1 )));
		if (!solver.addConstraints(toReturn.getConstraints())) throw new Error("Failed to add temporal constraints in refinement!");
		
		return toReturn;
	}

}
