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
package spatial.rectangleAlgebra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.datatype.Duration;

import org.w3c.dom.css.Rect;

import framework.ConstraintNetwork;
import framework.Variable;

import multi.allenInterval.AllenInterval;
import multi.allenInterval.AllenIntervalConstraint;
import multi.allenInterval.AllenIntervalNetworkSolver;
import time.APSPSolver;
import time.Bounds;

/**
 * 
 * @author iran Mansouri
 *
 */

public class AugmentedRectangleConstraintSolver extends RectangleConstraintSolver {

	/**
	 * 
	 */
	private long horizon = 2000;
	private AllenIntervalNetworkSolver solverX;
	private AllenIntervalNetworkSolver solverY;
	private boolean isInconsistent = false;
	private Vector<Variable> unaryCulpritVar;
	private HashMap<AllenInterval, Bounds> durationConsX = new HashMap<AllenInterval, Bounds>();
	private HashMap<AllenInterval, Bounds> durationConsY = new HashMap<AllenInterval, Bounds>();

	public AugmentedRectangleConstraintSolver() {
		super();
		this.setOptions(OPTIONS.AUTO_PROPAGATE);
	}

	@Override
	public boolean propagate() {
		if(super.getConstraints().length == 0) 
			return true;
		if(super.propagate())
			convertRectangleTo2DimensionSTP(super.getCompleteRARelations());

		return true;
	}

	public AllenIntervalNetworkSolver[] getInternalSolver(){
		return new AllenIntervalNetworkSolver[]{solverX, solverY};
	}


	private boolean convertRectangleTo2DimensionSTP(Vector<Vector<RectangleConstraint>> consrels){

		solverX = new AllenIntervalNetworkSolver(0, horizon, 50);
		solverY = new AllenIntervalNetworkSolver(0, horizon, 50);


		Vector<AllenIntervalConstraint> xAllenConstraint = new Vector<AllenIntervalConstraint>();
		Vector<AllenIntervalConstraint> yAllenConstraint = new Vector<AllenIntervalConstraint>();

		AllenInterval[] intervalsx = (AllenInterval[])solverX.createVariables(consrels.size());
		AllenInterval[] intervalsy = (AllenInterval[])solverY.createVariables(consrels.size());

		unaryCulpritVar = new Vector<Variable>();

		
		for (int i = 0; i < this.getVariables().length; i++) {
			intervalsx[i].setName(((RectangularRegion)this.getVariables()[i]).getName());
			intervalsy[i].setName(((RectangularRegion)this.getVariables()[i]).getName());
			if(((RectangularRegion)this.getVariables()[i]).getBoundingbox() != null){
				
				Bounds xLB = new Bounds(((RectangularRegion)this.getVariables()[i]).getBoundingbox().getxLB().min, ((RectangularRegion)this.getVariables()[i]).getBoundingbox().getxLB().max);
				Bounds xUB = new Bounds(((RectangularRegion)this.getVariables()[i]).getBoundingbox().getxUB().min, ((RectangularRegion)this.getVariables()[i]).getBoundingbox().getxUB().max);

				Bounds yLB = new Bounds(((RectangularRegion)this.getVariables()[i]).getBoundingbox().getyLB().min, ((RectangularRegion)this.getVariables()[i]).getBoundingbox().getyLB().max);
				Bounds yUB = new Bounds(((RectangularRegion)this.getVariables()[i]).getBoundingbox().getyUB().min, ((RectangularRegion)this.getVariables()[i]).getBoundingbox().getyUB().max);
				
				
				if(!isUnboundedBoundingBox(xLB, xUB, yLB, yUB) && ((RectangularRegion)this.getVariables()[i]).getOntologicalProp().isMovable()){
					unaryCulpritVar.add(this.getVariables()[i]);
				}


				AllenIntervalConstraint releaseX = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, 
						xLB);
				releaseX.setFrom(intervalsx[i]);
				releaseX.setTo(intervalsx[i]);
				xAllenConstraint.add(releaseX);

				AllenIntervalConstraint deadlineX = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, 
						xUB);
				deadlineX.setFrom(intervalsx[i]);
				deadlineX.setTo(intervalsx[i]);
				xAllenConstraint.add(deadlineX);


				AllenIntervalConstraint releaseY = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, 
						yLB);
				releaseY.setFrom(intervalsy[i]);
				releaseY.setTo(intervalsy[i]);
				yAllenConstraint.add(releaseY);


				AllenIntervalConstraint deadlineY = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, 
						yUB);
				deadlineY.setFrom(intervalsy[i]);
				deadlineY.setTo(intervalsy[i]);
				yAllenConstraint.add(deadlineY);
			}
			else{//for those which are unbounded
				AllenIntervalConstraint releaseX = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, 
						new Bounds(0, APSPSolver.INF));
				releaseX.setFrom(intervalsx[i]);
				releaseX.setTo(intervalsx[i]);
				xAllenConstraint.add(releaseX);

				AllenIntervalConstraint deadlineX = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, 
						new Bounds(0, APSPSolver.INF));
				deadlineX.setFrom(intervalsx[i]);
				deadlineX.setTo(intervalsx[i]);
				xAllenConstraint.add(deadlineX);


				AllenIntervalConstraint releaseY = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, 
						new Bounds(0, APSPSolver.INF));
				releaseY.setFrom(intervalsy[i]);
				releaseY.setTo(intervalsy[i]);
				yAllenConstraint.add(releaseY);

				AllenIntervalConstraint deadlineY = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, 
						new Bounds(0, APSPSolver.INF));
				deadlineY.setFrom(intervalsy[i]);
				deadlineY.setTo(intervalsy[i]);
				yAllenConstraint.add(deadlineY);
			}
		}
		
		
		convertUnboundedRAConstraints(xAllenConstraint, yAllenConstraint, intervalsx, intervalsy, consrels);
		//for bounded RA constraint
		convertBoundedRAConstraints(xAllenConstraint, yAllenConstraint, intervalsx, intervalsy, super.getBoundedConstraint());
		AllenIntervalConstraint[] consX = xAllenConstraint.toArray(new AllenIntervalConstraint[xAllenConstraint.size()]);
		
		if (!solverX.addConstraints(consX)) { 
			System.out.println(solverX + "Failed to add constraints in X dimension! ");
			isInconsistent = true;

		}
		//ConstraintNetwork.draw(solverX.getConstraintNetwork(), "X");
		AllenIntervalConstraint[] consY = yAllenConstraint.toArray(new AllenIntervalConstraint[yAllenConstraint.size()]);
		if (!solverY.addConstraints(consY)) { 
			System.out.println("Failed to add constraints in Y dimension! ");
			isInconsistent = true;
		}
		
//		double avg = ((double)(solverX.getRigidityNumber()) + (double)(solverY.getRigidityNumber())) / 2;
//		System.out.println("TMP AVG:" + avg);

		if(isInconsistent)
			return false;

		//ConstraintNetwork.draw(solverY.getConstraintNetwork(), "Y"); 		
		return true;

	}

	private boolean isUnboundedBoundingBox(Bounds xLB, Bounds xUB,
			Bounds yLB, Bounds yUB) {
		if(xLB.min != 0 && xLB.max != APSPSolver.INF)
			return false;
		if(xUB.min != 0 && xUB.max != APSPSolver.INF)
			return false;
		if(yLB.min != 0 && yLB.max != APSPSolver.INF)
			return false;
		if(yLB.min != 0 && yUB.max != APSPSolver.INF)
			return false;
		return true;
	}

	private void convertBoundedRAConstraints(
			Vector<AllenIntervalConstraint> xAllenConstraint, Vector<AllenIntervalConstraint> yAllenConstraint,
			AllenInterval[] intervalsx, AllenInterval[] intervalsy, Vector<AugmentedRectangleConstraint> boundedConstraint) {

		if(super.getBoundedConstraint() != null){
			for (int i = 0; i < super.getBoundedConstraint().size(); i++) {
				
				AllenIntervalConstraint boundedIntervalX = new AllenIntervalConstraint(super.getBoundedConstraint().get(i).getBoundedConstraintX().getType(), 
						super.getBoundedConstraint().get(i).getBoundedConstraintX().getBounds());
				boundedIntervalX.setFrom(intervalsx[super.getBoundedConstraint().get(i).getFrom().getID()]);
				boundedIntervalX.setTo(intervalsx[super.getBoundedConstraint().get(i).getTo().getID()]);
				xAllenConstraint.add(boundedIntervalX);


				AllenIntervalConstraint boundedIntervalY = new AllenIntervalConstraint(super.getBoundedConstraint().get(i).getBoundedConstraintY().getType(), 
						super.getBoundedConstraint().get(i).getBoundedConstraintY().getBounds());
				boundedIntervalY.setFrom(intervalsy[super.getBoundedConstraint().get(i).getFrom().getID()]);
				boundedIntervalY.setTo(intervalsy[super.getBoundedConstraint().get(i).getTo().getID()]);
				yAllenConstraint.add(boundedIntervalY);
				
				if(super.getBoundedConstraint().get(i).getBoundedConstraintX().getType().equals(AllenIntervalConstraint.Type.Duration)){
					durationConsX.put(intervalsx[super.getBoundedConstraint().get(i).getFrom().getID()], boundedIntervalX.getBounds()[0]);
					durationConsY.put(intervalsy[super.getBoundedConstraint().get(i).getFrom().getID()], boundedIntervalY.getBounds()[0]);
				}

					

			}
		}
	}

	private void convertUnboundedRAConstraints(Vector<AllenIntervalConstraint> xAllenConstraint, Vector<AllenIntervalConstraint> yAllenConstraint,
			AllenInterval[] intervalsx, AllenInterval[] intervalsy, Vector<Vector<RectangleConstraint>> consrels) {


		for (int i = 0; i < consrels.size(); i++) {
			for (int j = i + 1; j < consrels.size(); j++) {

				//create convexity
				TwoDimensionsAllenConstraint[] convex2DAllen = RectangleConstraint.getRAConvexClosure(consrels.get(i).get(j).getTypes());

				Vector<AllenIntervalConstraint.Type> xtp = new Vector<AllenIntervalConstraint.Type>(); 
				Vector<AllenIntervalConstraint.Type> ytp = new Vector<AllenIntervalConstraint.Type>();
				for (int j2 = 0; j2 < convex2DAllen.length; j2++) {
					if(!xtp.contains(AllenIntervalConstraint.Type.fromString(convex2DAllen[j2].getAllenType()[0].name())))
						xtp.add(AllenIntervalConstraint.Type.fromString(convex2DAllen[j2].getAllenType()[0].name()));
					if(!ytp.contains(AllenIntervalConstraint.Type.fromString(convex2DAllen[j2].getAllenType()[1].name())))
						ytp.add(AllenIntervalConstraint.Type.fromString(convex2DAllen[j2].getAllenType()[1].name()));
				}

				AllenIntervalConstraint btwintervalx = new AllenIntervalConstraint(xtp.toArray(new AllenIntervalConstraint.Type[xtp.size()]));
				btwintervalx.setFrom(intervalsx[i]);
				btwintervalx.setTo(intervalsx[j]);
				xAllenConstraint.add(btwintervalx);

				AllenIntervalConstraint btwintervaly = new AllenIntervalConstraint(ytp.toArray(new AllenIntervalConstraint.Type[ytp.size()]));
				btwintervaly.setFrom(intervalsy[i]);
				btwintervaly.setTo(intervalsy[j]);
				yAllenConstraint.add(btwintervaly);

			}
		}

	}
	

	/**
	 *  it computes a bounding box which with two bounds on both sides resulted from 2 extreme bounded 2D STP solution 
	 * @param name of the rectangle region
	 * @return a bounding box 
	 */
	public BoundingBox extractBoundingBoxesFromSTPs(String name){
		
		Bounds xLB, xUB, yLB, yUB;
		
		for (int i = 0; i < solverX.getVariables().length; i++) {
			if(((AllenInterval)solverX.getVariables()[i]).getName().compareTo(name) == 0){
				xLB = new Bounds(((AllenInterval)solverX.getVariables()[i]).getEST(), ((AllenInterval)solverX.getVariables()[i]).getLST());
				xUB = new Bounds(((AllenInterval)solverX.getVariables()[i]).getEET(), ((AllenInterval)solverX.getVariables()[i]).getLET());
				yLB = new Bounds(((AllenInterval)solverY.getVariables()[i]).getEST(), ((AllenInterval)solverY.getVariables()[i]).getLST());
				yUB = new Bounds(((AllenInterval)solverY.getVariables()[i]).getEET(), ((AllenInterval)solverY.getVariables()[i]).getLET());
				return new BoundingBox(xLB, xUB, yLB, yUB);
			}
		}
		return null;
	}
	
	private BoundingBox incrementalPathConsistencyByCMConstraints(){
		
		HashMap<RectangularRegion, BoundingBox> cmBBs = new HashMap<RectangularRegion, BoundingBox>(); //centre of mass bounding boxes

		for (int i = 0; i < solverX.getVariables().length; i++) {
			//calculate the centre of mass
			BoundingBox bb = getCMbasedBB((AllenInterval)solverX.getVariables()[i], (AllenInterval)solverY.getVariables()[i]);
			//extractBoundingBoxesFromSTPs()
			
			//impose the At constraint (exact position)
			Vector<AllenIntervalConstraint> xAllenConstraint = new Vector<AllenIntervalConstraint>();
			Vector<AllenIntervalConstraint> yAllenConstraint = new Vector<AllenIntervalConstraint>();
			
			AllenIntervalConstraint releaseX = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, 
					bb.getxLB());
			releaseX.setFrom(solverX.getVariables()[i]);
			releaseX.setTo(solverX.getVariables()[i]);
			xAllenConstraint.add(releaseX);

			AllenIntervalConstraint deadlineX = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, 
					bb.getxUB());
			deadlineX.setFrom(solverX.getVariables()[i]);
			deadlineX.setTo(solverX.getVariables()[i]);
			xAllenConstraint.add(deadlineX);


			AllenIntervalConstraint releaseY = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, 
					bb.getyLB());
			releaseY.setFrom(solverY.getVariables()[i]);
			releaseY.setTo(solverY.getVariables()[i]);
			yAllenConstraint.add(releaseY);


			AllenIntervalConstraint deadlineY = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, 
					bb.getyUB());
			deadlineY.setFrom(solverY.getVariables()[i]);
			deadlineY.setTo(solverY.getVariables()[i]);
			yAllenConstraint.add(deadlineY);
			
			solverX.addConstraints(xAllenConstraint.toArray(new AllenIntervalConstraint[xAllenConstraint.size()]));
			solverY.addConstraints(yAllenConstraint.toArray(new AllenIntervalConstraint[yAllenConstraint.size()]));
			
			//add them to hashMap			
			//cmBBs.put((RectangularRegion)solverX.getVariables()[i], getCentreOfMass((AllenInterval)solverX.getVariables()[i], (AllenInterval)solverY.getVariables()[i]));
		}
		
		
		//add them to the constraint network
		//store them in a hashMap! by name!?
		return null;
		
	}

	private BoundingBox getCMbasedBB(AllenInterval intervalX,
			AllenInterval intervalY){
			
		Point px = getCentreOfMass(intervalX);
		Point py = getCentreOfMass(intervalY);
		return new BoundingBox(new Bounds(px.x(),px.x()), new Bounds(px.y(),px.y()), new Bounds(py.x(),py.x()), new Bounds(py.y(),py.y()));
	}
	
	private Point getCentreOfMass(AllenInterval interval) {
		
		double cRecX = (double)(interval.getLST() - interval.getEST()) /2;
		double cRecY = 	(double)(interval.getLET() - interval.getEET()) /2;	
		double cx = 0, cy = 0;
		double durMinX = 0, durMaxX = 0;

		if(durationConsX.get(interval) != null){
			durMinX = durationConsX.get(interval).min;
			durMaxX = durationConsX.get(interval).max;
		}
		
		//This is for X dimension
		//area of triangle
		double s0 = ((double)(interval.getLET() - interval.getEET()) * (interval.getLST() - interval.getEST())) / 2; // area of rectangle
		double s1 = ((double)(interval.getEET() - interval.getLST()) * (interval.getLST() - interval.getEET())) / 2; //area of triangle without duration
		double s2 = ((double)((interval.getLST() + durMinX) - interval.getEET()) * (interval.getLST() - (interval.getEET() - durMinX))) / 2; // lower triangle when lower bound duration exists 
		double s3 = ((double)(((interval.getLET() - durMaxX) - interval.getEST()) * (interval.getLET() - (interval.getEST() + durMaxX)))) / 2; // upper triangle when upper bound duration exists
		
		//
		if(durationConsX.get(interval) == null){
			double cTrinagleX = (2/3 * (interval.getLST() - interval.getEET()) + interval.getEET());
			double cTrinagleY = (1/3 * (interval.getLST() - interval.getEET()) + interval.getEET());;
			cx = (double) ((-cTrinagleX * s1) + ( cRecX * s0)) / (s1 + s0);
			cy = (double) ((-cTrinagleY * s1) + ( cRecY * s0)) / (s1 + s0);
		}
		else{			
			double cLTrinagleX = (2/3 * (interval.getLST() - (interval.getEET() - durMinX))) + (interval.getEET() - durMinX);
			double cLTrinagleY = (1/3 * (interval.getLST() + durMinX - interval.getEET())) + interval.getEET();
			
			double cUTrinagleX = (1/3 * (interval.getLET() - durMaxX - interval.getEST())) + interval.getEST();
			double cUTrinagleY = (1/3 * (interval.getEST() + durMaxX - interval.getLET())) + (interval.getEST() + durMaxX);

			cx = (double) ((-cLTrinagleX * s2) + (-cUTrinagleX * s3) + ( cRecX * s0)) / (s0 + s2 + s3);
			cy = (double) ((-cLTrinagleY * s2) + (-cUTrinagleY * s3) + ( cRecY * s0)) / (s0 + s2 + s3);
		}
		
		
		
		
		//return new BoundingBox(xLB, xUB, yLB, yUB);
		return new Point(cx, cy);
	}



	/**
	 * make script readable for gnuplot
	 * @param st
	 */	
	public String drawMinMaxRectangle(long horizon, String... st){
		int j = 1;
		String ret = "";
		ret = "set xrange [0:" + horizon +"]" + "\n";
		ret += "set yrange [0:" + horizon +"]" + "\n";
		for (int i = 0; i < st.length; i++) {
			//rec min
			ret += "set obj " + j + " rect from " + extractBoundingBoxesFromSTPs(st[i]).getMinRectabgle().getMinX() + "," + extractBoundingBoxesFromSTPs(st[i]).getMinRectabgle().getMinY() 
					+" to " + extractBoundingBoxesFromSTPs(st[i]).getMinRectabgle().getMaxX() + "," +extractBoundingBoxesFromSTPs(st[i]).getMinRectabgle().getMaxY() + 
					" front fs transparent solid 0.0 border " + (i+1) +" lw 0.5" + "\n";
			j++;
			//label of rec min
			ret += "set label " + "\""+ st[i]+"-min" +"\""+" at "+ extractBoundingBoxesFromSTPs(st[i]).getMinRectabgle().getMinX() +"," 
					+ extractBoundingBoxesFromSTPs(st[i]).getMinRectabgle().getCenterY() + " textcolor lt " + (i+1) + " font \"9\"" + "\n";
			//rec max
			ret += "set obj " + j + " rect from " + extractBoundingBoxesFromSTPs(st[i]).getMaxRectabgle().getMinX() + "," + extractBoundingBoxesFromSTPs(st[i]).getMaxRectabgle().getMinY() 
					+" to " + extractBoundingBoxesFromSTPs(st[i]).getMaxRectabgle().getMaxX() + "," + extractBoundingBoxesFromSTPs(st[i]).getMaxRectabgle().getMaxY() + 
					" front fs transparent solid 0.0 border " + (i+1) +" lw 0.5" + "\n";
			j++;
			//label of rec max			
			ret += "set label " + "\""+ st[i]+"-max" +"\"" +" at "+ extractBoundingBoxesFromSTPs(st[i]).getMaxRectabgle().getMinX() +"," 
					+ extractBoundingBoxesFromSTPs(st[i]).getMaxRectabgle().getCenterY() + " textcolor lt " + (i+1) + " font \"9\"" + "\n";

			//point of the solution
			ret += "set object " + j +" circle at "+ extractBoundingBoxesFromSTPs(st[i]).getACentrePointSolution().x +","
					+ extractBoundingBoxesFromSTPs(st[i]).getACentrePointSolution().y + " size 0.15 fc lt " + (i+1) + "\n";

			//label of solution
			ret += "set label " + "\""+ st[i]+"-C" +"\"" +" at "+ extractBoundingBoxesFromSTPs(st[i]).getACentrePointSolution().x +"," 
					+ extractBoundingBoxesFromSTPs(st[i]).getACentrePointSolution().y + " textcolor lt " + (i+1) + " font \"9\"" + "\n";
			j++;
		}
		ret += "plot NaN" + "\n";
		ret += "pause -1" + "\n";
		return ret;
	}


	/**
	 * make script readable for gnuplot
	 * @param st
	 */
	public String drawAlmostCentreRectangle(long horizon, String... st){
		String ret = "";
		int j = 1;
		ret = "set xrange [0:" + horizon +"]"+ "\n";
		ret += "set yrange [0:" + horizon +"]" + "\n";
		for (int i = 0; i < st.length; i++) {
			//rec 
			ret += "set obj " + j + " rect from " + extractBoundingBoxesFromSTPs(st[i]).getAlmostCentreRectangle().getMinX() + "," + extractBoundingBoxesFromSTPs(st[i]).getAlmostCentreRectangle().getMinY() 
					+" to " + extractBoundingBoxesFromSTPs(st[i]).getAlmostCentreRectangle().getMaxX() + "," +extractBoundingBoxesFromSTPs(st[i]).getAlmostCentreRectangle().getMaxY() + 
					" front fs transparent solid 0.0 border " + (i+1) +" lw 0.5" + "\n";
			j++;
			//label of centre Rec
			ret += "set label " + "\""+ st[i]+"-c" +"\""+" at "+ extractBoundingBoxesFromSTPs(st[i]).getAlmostCentreRectangle().getCenterX() +"," 
					+ extractBoundingBoxesFromSTPs(st[i]).getAlmostCentreRectangle().getCenterY() + " textcolor lt " + (i+1) + " font \"9\"" + "\n";
			j++;
		}
		ret += "plot NaN" + "\n";
		ret += "pause -1";
		return ret;
	}


	public BoundingBox[] minimalCulpritDetector(){
		
		
		HashMap<RectangularRegion, HashMap<RectangularRegion, BoundingBox>> bestMoveCandidates = new HashMap<RectangularRegion, HashMap<RectangularRegion,BoundingBox>>(); 
		HashMap<RectangularRegion, Double> rigidityHuristic = new HashMap<RectangularRegion, Double>();
		//culprit set with cardinality one (at constraint)
		for (int u = 0; u < unaryCulpritVar.size(); u++) {

			AllenIntervalNetworkSolver tmpSolverX = new AllenIntervalNetworkSolver(0, horizon);
			AllenIntervalNetworkSolver tmpSolverY = new AllenIntervalNetworkSolver(0, horizon);


			Vector<AllenIntervalConstraint> xAllenConstraint = new Vector<AllenIntervalConstraint>();
			Vector<AllenIntervalConstraint> yAllenConstraint = new Vector<AllenIntervalConstraint>();

			AllenInterval[] intervalsx = (AllenInterval[])tmpSolverX.createVariables(super.getCompleteRARelations().size());
			AllenInterval[] intervalsy = (AllenInterval[])tmpSolverY.createVariables(super.getCompleteRARelations().size());

			Bounds xLB, xUB,yLB, yUB;
			for (int i = 0; i < this.getVariables().length; i++) {
				intervalsx[i].setName(((RectangularRegion)this.getVariables()[i]).getName());
				intervalsy[i].setName(((RectangularRegion)this.getVariables()[i]).getName());
				if(((RectangularRegion)this.getVariables()[i]).getBoundingbox() != null && 
						((RectangularRegion)this.getVariables()[i]).getID() !=  unaryCulpritVar.get(u).getID())
				{
					xLB = ((RectangularRegion)this.getVariables()[i]).getBoundingbox().getxLB();
					xUB = ((RectangularRegion)this.getVariables()[i]).getBoundingbox().getxUB();

					yLB = ((RectangularRegion)this.getVariables()[i]).getBoundingbox().getyLB();
					yUB = ((RectangularRegion)this.getVariables()[i]).getBoundingbox().getyUB();



					AllenIntervalConstraint releaseX = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, 
							xLB);
					releaseX.setFrom(intervalsx[i]);
					releaseX.setTo(intervalsx[i]);
					xAllenConstraint.add(releaseX);

					AllenIntervalConstraint deadlineX = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, 
							xUB);
					deadlineX.setFrom(intervalsx[i]);
					deadlineX.setTo(intervalsx[i]);
					xAllenConstraint.add(deadlineX);


					AllenIntervalConstraint releaseY = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, 
							yLB);
					releaseY.setFrom(intervalsy[i]);
					releaseY.setTo(intervalsy[i]);
					yAllenConstraint.add(releaseY);


					AllenIntervalConstraint deadlineY = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, 
							yUB);
					deadlineY.setFrom(intervalsy[i]);
					deadlineY.setTo(intervalsy[i]);
					yAllenConstraint.add(deadlineY);
				}
				else{//for those which are unbounded
					AllenIntervalConstraint releaseX = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, 
							new Bounds(0, APSPSolver.INF));
					releaseX.setFrom(intervalsx[i]);
					releaseX.setTo(intervalsx[i]);
					xAllenConstraint.add(releaseX);

					AllenIntervalConstraint deadlineX = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, 
							new Bounds(0, APSPSolver.INF));
					deadlineX.setFrom(intervalsx[i]);
					deadlineX.setTo(intervalsx[i]);
					xAllenConstraint.add(deadlineX);


					AllenIntervalConstraint releaseY = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, 
							new Bounds(0, APSPSolver.INF));
					releaseY.setFrom(intervalsy[i]);
					releaseY.setTo(intervalsy[i]);
					yAllenConstraint.add(releaseY);

					AllenIntervalConstraint deadlineY = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, 
							new Bounds(0, APSPSolver.INF));
					deadlineY.setFrom(intervalsy[i]);
					deadlineY.setTo(intervalsy[i]);
					yAllenConstraint.add(deadlineY);
				}
			}//end of loop for variables


			convertUnboundedRAConstraints(xAllenConstraint, yAllenConstraint, intervalsx, intervalsy, super.getCompleteRARelations());
			//for bounded RA constraint
			convertBoundedRAConstraints(xAllenConstraint, yAllenConstraint, intervalsx, intervalsy, super.getBoundedConstraint());



			//check whether they are consistent then measureRigidity and save rigidity with respect to rectangle  
			AllenIntervalConstraint[] consX = xAllenConstraint.toArray(new AllenIntervalConstraint[xAllenConstraint.size()]);
			AllenIntervalConstraint[] consY = yAllenConstraint.toArray(new AllenIntervalConstraint[yAllenConstraint.size()]);
			if (tmpSolverX.addConstraints(consX) && tmpSolverY.addConstraints(consY)) {
				double avg = ((double)(tmpSolverX.getRigidityNumber()) + (double)(tmpSolverY.getRigidityNumber())) / 2;
				rigidityHuristic.put((RectangularRegion)unaryCulpritVar.get(u), 
						avg);
				System.out.print("name: " + ((RectangularRegion)unaryCulpritVar.get(u)).getName());
				System.out.println(", avg: " + avg);
				ConstraintNetwork.draw(this.theNetwork, ((RectangularRegion)unaryCulpritVar.get(u)).getName());
				bestMoveCandidates.put((RectangularRegion)unaryCulpritVar.get(u), getbestPlacementBoundingBox(tmpSolverX, tmpSolverY));
			}
			else{
				System.out.println("it is not propagated " + ((RectangularRegion)unaryCulpritVar.get(u)).getName());
			}
			//System.out.println(".....................................................................");
		}//end of loop for unary culprit

		ArrayList as = new ArrayList( rigidityHuristic.entrySet() );          
		Collections.sort( as , new Comparator() {  
			public int compare( Object o1 , Object o2 )  
			{  
				Map.Entry e1 = (Map.Entry)o1 ;  
				Map.Entry e2 = (Map.Entry)o2 ;  
				Double first = (Double)e1.getValue();  
				Double second = (Double)e2.getValue();  
				return first.compareTo( second );  
			}  
		}); 

//		Iterator i = as.iterator();  
//		while ( i.hasNext() )  
//		{  
//			System.out.println( (Map.Entry)i.next() );  
//		} 

		Iterator i = as.iterator();  
		//System.out.println( ((Map.Entry)i.next()).getKey() );  
		System.out.println(bestMoveCandidates.get((RectangularRegion)((Map.Entry)i.next()).getKey()));

		return null;
	}

	private HashMap<RectangularRegion, BoundingBox> getbestPlacementBoundingBox(
			AllenIntervalNetworkSolver tmpSolverX,
			AllenIntervalNetworkSolver tmpSolverY) {
		HashMap<RectangularRegion, BoundingBox> ret = new HashMap<RectangularRegion, BoundingBox>();
		Bounds xLB, xUB, yLB, yUB;
		
		for (int i = 0; i < this.getVariables().length; i++) {
			xLB = new Bounds(((AllenInterval)tmpSolverX.getVariables()[i]).getEST(), ((AllenInterval)tmpSolverX.getVariables()[i]).getLST());
			xUB = new Bounds(((AllenInterval)tmpSolverX.getVariables()[i]).getEET(), ((AllenInterval)tmpSolverX.getVariables()[i]).getLET());
			yLB = new Bounds(((AllenInterval)tmpSolverY.getVariables()[i]).getEST(), ((AllenInterval)tmpSolverY.getVariables()[i]).getLST());
			yUB = new Bounds(((AllenInterval)tmpSolverY.getVariables()[i]).getEET(), ((AllenInterval)tmpSolverY.getVariables()[i]).getLET());
			ret.put((RectangularRegion)this.getVariables()[i], new BoundingBox(xLB, xUB, yLB, yUB));
		}
		return ret;
	}
	

}





