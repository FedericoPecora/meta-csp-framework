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
import java.util.HashMap;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.framework.meta.MetaConstraintSolver;
import org.metacsp.framework.meta.MetaVariable;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.spatial.DE9IM.DE9IMRelation;
import org.metacsp.multi.spatial.DE9IM.GeometricShapeDomain;
import org.metacsp.multi.spatial.DE9IM.GeometricShapeVariable;
import org.metacsp.multi.spatioTemporal.paths.PoseSteering;
import org.metacsp.multi.spatioTemporal.paths.Trajectory;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelope;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelopeSolver;
import org.metacsp.multi.symbols.SymbolicValueConstraint;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class TrajectoryEnvelopeScheduler extends MetaConstraintSolver {

	private static final long serialVersionUID = 8551829132754804513L;
	private HashMap<TrajectoryEnvelope,ArrayList<TrajectoryEnvelope>> refinedWith = new HashMap<TrajectoryEnvelope, ArrayList<TrajectoryEnvelope>>();

	public TrajectoryEnvelopeScheduler(long origin, long horizon, long animationTime) {
		super(new Class[] {AllenIntervalConstraint.class, DE9IMRelation.class}, animationTime, new TrajectoryEnvelopeSolver(origin, horizon));
	}

	public TrajectoryEnvelopeScheduler(long origin, long horizon) {
		super(new Class[] {AllenIntervalConstraint.class, DE9IMRelation.class}, 0, new TrajectoryEnvelopeSolver(origin, horizon));
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

	@Override
	public void postBacktrack(MetaVariable mv) {
		// TODO Auto-generated method stub
		
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
	
	public ConstraintNetwork refineTrajectoryEnvelopes() {
		ConstraintNetwork ret = new ConstraintNetwork(null);
		Variable[] varsBefore = this.getConstraintSolvers()[0].getVariables();
		for (int i = 0; i < varsBefore.length-1; i++) {
			for (int j = i+1; j < varsBefore.length; j++) {
				
				//Get TEs
				TrajectoryEnvelope te1 = (TrajectoryEnvelope)varsBefore[i];
				TrajectoryEnvelope te2 = (TrajectoryEnvelope)varsBefore[j];

				//Init data structures
				if (!refinedWith.containsKey(te1)) refinedWith.put(te1,new ArrayList<TrajectoryEnvelope>());
				if (!refinedWith.containsKey(te2)) refinedWith.put(te2,new ArrayList<TrajectoryEnvelope>());
				
				// If != robots
				if (te1.getRobotID() != te2.getRobotID()) {
					
					//if they intersect
					GeometricShapeVariable poly1 = te1.getEnvelopeVariable();
					GeometricShapeVariable poly2 = te2.getEnvelopeVariable();
					Geometry shape1 = ((GeometricShapeDomain)poly1.getDomain()).getGeometry();
					Geometry shape2 = ((GeometricShapeDomain)poly2.getDomain()).getGeometry();
					if (shape1.intersects(shape2)) {
	
						if (te1.getRefinable() && !refinedWith.get(te1).contains(te2)) {
							ConstraintNetwork ref1 = refineTrajectoryEnvelopes(te1, te2);
							refinedWith.get(te1).add(te2);
							ret.join(ref1);
						}
						if (te2.getRefinable() && !refinedWith.get(te2).contains(te1)) {
							ConstraintNetwork ref2 = refineTrajectoryEnvelopes(te2, te1);
							refinedWith.get(te2).add(te1);
							ret.join(ref2);
						}
					}
				}
			}
		}
		return ret;
	}
	
	private ConstraintNetwork refineTrajectoryEnvelopes(TrajectoryEnvelope var1, TrajectoryEnvelope var2) {
		logger.fine("Refining " + var1 + " with " + var2);
		TrajectoryEnvelopeSolver solver = (TrajectoryEnvelopeSolver)this.getConstraintSolvers()[0];
		ConstraintNetwork toReturn = new ConstraintNetwork(null);
		GeometryFactory gf = new GeometryFactory();
		Geometry se1 = ((GeometricShapeDomain)var1.getEnvelopeVariable().getDomain()).getGeometry();
		Geometry se2 = ((GeometricShapeDomain)var2.getEnvelopeVariable().getDomain()).getGeometry();
		Geometry intersectionse1se2 = se1.intersection(se2);

		ArrayList<PoseSteering> var1sec1 = new ArrayList<PoseSteering>();
		ArrayList<PoseSteering> var1sec2 = new ArrayList<PoseSteering>();
		ArrayList<PoseSteering> var1sec3 = new ArrayList<PoseSteering>();
		for (int i = 0; i < var1.getPathLength(); i++) {
			Coordinate coord = var1.getTrajectory().getPositions()[i];
			PoseSteering ps = var1.getTrajectory().getPoseSteering()[i];
			Point point = gf.createPoint(coord);
			if (!intersectionse1se2.contains(point) && var1sec2.isEmpty()) {
				var1sec1.add(ps);
			}
			else if (intersectionse1se2.contains(point)) {
				var1sec2.add(ps);
			}
			else if (!intersectionse1se2.contains(point) && !var1sec2.isEmpty()) {
				var1sec3.add(ps);
			}
		}
		
		boolean done = false;
		while (!done) {
			Geometry lastPolySec1 = var1.makeFootprint(var1sec1.get(var1sec1.size()-1));
			if (lastPolySec1.disjoint(se2)) done = true;
			else {
				var1sec2.add(0,var1sec1.get(var1sec1.size()-1));
				var1sec1.remove(var1sec1.size()-1);
				logger.finest("Added to start...");
			}
		}

		done = false;
		while (!done) {
			Geometry firstPolySec3 = var1.makeFootprint(var1sec3.get(0));
			if (firstPolySec3.disjoint(se2)) done = true;
			else {
				var1sec2.add(var1sec3.get(0));
				var1sec3.remove(0);
				logger.finest("Added to end...");
			}
		}

		Trajectory newPath1sec1 = new Trajectory(var1sec1.toArray(new PoseSteering[var1sec1.size()]));
		Trajectory newPath1sec2 = new Trajectory(var1sec2.toArray(new PoseSteering[var1sec2.size()]));
		Trajectory newPath1sec3 = new Trajectory(var1sec3.toArray(new PoseSteering[var1sec3.size()]));

		Variable[] newVars = solver.createVariables(3);
		TrajectoryEnvelope newVar1sec1 = (TrajectoryEnvelope)newVars[0];
		TrajectoryEnvelope newVar1sec2 = (TrajectoryEnvelope)newVars[1];
		TrajectoryEnvelope newVar1sec3 = (TrajectoryEnvelope)newVars[2];
		
		newVar1sec2.setRefinable(false);

		newVar1sec1.setTrajectory(newPath1sec1);
		newVar1sec2.setTrajectory(newPath1sec2);
		newVar1sec3.setTrajectory(newPath1sec3);

		TrajectoryEnvelope[] newEnvelopes = new TrajectoryEnvelope[3];
		newEnvelopes[0] = newVar1sec1;
		newEnvelopes[1] = newVar1sec2;
		newEnvelopes[2] = newVar1sec3;
		newEnvelopes[0].setRefinable(false);
		newEnvelopes[1].setRefinable(false);
		newEnvelopes[2].setRefinable(false);
		newEnvelopes[0].setSuperEnvelope(var1);
		newEnvelopes[1].setSuperEnvelope(var1);
		newEnvelopes[2].setSuperEnvelope(var1);
		newEnvelopes[0].setRobotID(var1.getRobotID());
		newEnvelopes[1].setRobotID(var1.getRobotID());
		newEnvelopes[2].setRobotID(var1.getRobotID());
		refinedWith.get(var2).add(newVar1sec2);
		((Map)this.getMetaConstraints()[0]).setUsage(newEnvelopes);

		AllenIntervalConstraint starts1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Starts);
		starts1.setFrom(newVar1sec1);
		starts1.setTo(var1);		
		AllenIntervalConstraint finishes1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Finishes);
		finishes1.setFrom(newVar1sec3);
		finishes1.setTo(var1);
		AllenIntervalConstraint meets1sec1sec2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets);
		meets1sec1sec2.setFrom(newVar1sec1);
		meets1sec1sec2.setTo(newVar1sec2);
		AllenIntervalConstraint meets1sec2sec3 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets);
		meets1sec2sec3.setFrom(newVar1sec2);
		meets1sec2sec3.setTo(newVar1sec3);

		solver.addConstraints(starts1,finishes1,meets1sec1sec2,meets1sec2sec3);
		toReturn.addConstraints(starts1,finishes1,meets1sec1sec2,meets1sec2sec3);
		return toReturn;
	}
	
}
