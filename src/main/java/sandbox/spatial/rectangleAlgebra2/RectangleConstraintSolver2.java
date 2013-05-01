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
package sandbox.spatial.rectangleAlgebra2;

import java.util.HashMap;
import java.util.Vector;

import cern.colt.Arrays;
import spatial.rectangleAlgebra.AugmentedRectangleConstraint;
import spatial.rectangleAlgebra.BoundingBox;
import spatial.rectangleAlgebra.QualitativeAllenIntervalConstraint;
import spatial.rectangleAlgebra.TwoDimensionsAllenConstraint;
import spatial.rectangleAlgebra.QualitativeAllenIntervalConstraint.Type;
import time.Bounds;
import multi.allenInterval.AllenInterval;
import multi.allenInterval.AllenIntervalNetworkSolver;
import framework.Constraint;
import framework.ConstraintNetwork;
import framework.ConstraintSolver;
import framework.Variable;
import framework.multi.MultiConstraintSolver;

/**
 * This class represents Rectangle constraints. Each constraint represents two dimension Allen relations between spatial entities.    
 * In rectangle Algebra, each spatial entity is restricted to be an axis parallel rectangle.
 * 
 * @author Iran Mansouri
 *
 */

public class RectangleConstraintSolver2 extends MultiConstraintSolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6694598836324746725L;
	private int IDs = 0;
	
	private HashMap<Integer, Variable> getVaribaleById = new HashMap<Integer, Variable>();
	private boolean debug = false;
	private Vector<Vector<RectangleConstraint2>> recRels; //= new Vector<Vector<RectangleConstraint2>>(); 
	public enum Dimension  {X, Y};

	

	public RectangleConstraintSolver2(long origin, long horizon) {
		super(new Class[] {RectangleConstraint2.class, UnaryRectangleConstraint2.class}, new Class[] {RectangularRegion2.class}, createConstraintSolvers(origin, horizon, -1));
	}

	public RectangleConstraintSolver2(long origin, long horizon, int maxRectangles) {
		super(new Class[] {RectangleConstraint2.class}, new Class[] {RectangularRegion2.class}, createConstraintSolvers(origin, horizon, maxRectangles));
	}

	private static ConstraintSolver[] createConstraintSolvers(long origin, long horizon, int maxRectangles) {
		ConstraintSolver[] ret = new ConstraintSolver[2];
		//X
		ret[0] = (maxRectangles != -1 ? new AllenIntervalNetworkSolver(origin, horizon, maxRectangles) : new AllenIntervalNetworkSolver(origin, horizon));
		//Y
		ret[1] = (maxRectangles != -1 ? new AllenIntervalNetworkSolver(origin, horizon, maxRectangles) : new AllenIntervalNetworkSolver(origin, horizon));
		return ret;
	}
	
	@Override
	protected ConstraintNetwork createConstraintNetwork() {
		return new RectangleConstraintNetwork2(this);
	}
	
	@Override
	protected Variable[] createVariablesSub(int num) {
		Variable[] ret = new Variable[num];
		for (int i = 0; i < num; i++) ret[i] = new RectangularRegion2(this, this.IDs++, this.constraintSolvers);
		return ret;
	}

	@Override
	public boolean propagate() {
		// Do nothing, AllenIntervalNetworkSolver takes care of propagation...
		
		
//		if(this.getConstraints().length == 0) return true;
//		for (int i = 0; i < this.getVariables().length; i++) {
//			getVaribaleById.put(this.getVariables()[i].getID(), this.getVariables()[i]);
//		}
//		
//		recRels = new Vector<Vector<RectangleConstraint2>>();
//		recRels = createAllenCompleteNetwork(this.getConstraints());
//		
//		
//		if(debug){
//			boolean fail = true;
//			System.out.println("Debug Mode:");
//			System.out.println("Before: ");
//			System.out.println(PrintSpatialRelation(recRels));
//			
//			
//			if(weakPathConsistency(recRels))
//				fail = false;
//			
//			System.out.println("After:");
//			System.out.println(PrintSpatialRelation(recRels));
//			if(fail) return false;
//			else return true;
//		}
//		
//		if(!debug){
//			if(weakPathConsistency(recRels))
//				return true;
//			return false;
//		}
//		
//		return false;

		return true;
	}

	/**
	 * make script readable for gnuplot
	 * @param st
	 */
	public String drawAlmostCentreRectangle(long horizon, RectangularRegion2 ... rect){
		String ret = "";
		int j = 1;
		ret = "set xrange [0:" + horizon +"]"+ "\n";
		ret += "set yrange [0:" + horizon +"]" + "\n";
		for (int i = 0; i < rect.length; i++) {
			//rec 
			ret += "set obj " + j + " rect from " + extractBoundingBoxesFromSTPs(rect[i]).getAlmostCentreRectangle().getMinX() + "," + extractBoundingBoxesFromSTPs(rect[i]).getAlmostCentreRectangle().getMinY() 
					+" to " + extractBoundingBoxesFromSTPs(rect[i]).getAlmostCentreRectangle().getMaxX() + "," +extractBoundingBoxesFromSTPs(rect[i]).getAlmostCentreRectangle().getMaxY() + 
					" front fs transparent solid 0.0 border " + (i+1) +" lw 0.5" + "\n";
			j++;
			//label of centre Rec
			ret += "set label " + "\""+ rect[i]+"-c" +"\""+" at "+ extractBoundingBoxesFromSTPs(rect[i]).getAlmostCentreRectangle().getCenterX() +"," 
					+ extractBoundingBoxesFromSTPs(rect[i]).getAlmostCentreRectangle().getCenterY() + " textcolor lt " + (i+1) + " font \"9\"" + "\n";
			j++;
		}
		ret += "plot NaN" + "\n";
		ret += "pause -1";
		return ret;
	}

	/**
	 *  it computes a bounding box which with two bounds on both sides resulted from 2 extreme bounded 2D STP solution 
	 * @param name of the rectangle region
	 * @return a bounding box 
	 */
	public BoundingBox extractBoundingBoxesFromSTPs(RectangularRegion2 rect){
		Bounds xLB, xUB, yLB, yUB;
		xLB = new Bounds(((AllenInterval)rect.getInternalVariables()[0]).getEST(),((AllenInterval)rect.getInternalVariables()[0]).getLST()); 
		xUB = new Bounds(((AllenInterval)rect.getInternalVariables()[0]).getEET(),((AllenInterval)rect.getInternalVariables()[0]).getLET()); 
		yLB = new Bounds(((AllenInterval)rect.getInternalVariables()[1]).getEST(),((AllenInterval)rect.getInternalVariables()[1]).getLST()); 
		yUB = new Bounds(((AllenInterval)rect.getInternalVariables()[1]).getEET(),((AllenInterval)rect.getInternalVariables()[1]).getLET()); 
		return new BoundingBox(xLB, xUB, yLB, yUB);
	}
	
	
	//Qualitative part
//	private String PrintSpatialRelation(Vector<Vector<RectangleConstraint2>> recRels) {
//		
//		String ret = "";
//		for(int i = 0; i < recRels.size(); i++){
//			for(int j = 0; j < recRels.size(); j++){
//					if(i == j) continue;
//					ret += ( "\n"+ i + " --> " +  j + " :");
//					String rectangleRel = "";
//					if(recRels.get(i).get(j).getTypes().length != 0 && recRels.get(i).get(j).getTypes().length != 0){
//						for (int j2 = 0; j2 < recRels.get(i).get(j).getTypes().length; j2++) {
//							if(j2 > 0)
//								rectangleRel += " v ";
//							rectangleRel += " (" + recRels.get(i).get(j).getTypes()[j2].getAllenType()[0] + ", " +  recRels.get(i).get(j).getTypes()[j2].getAllenType()[1]+") " + " + " 
//								+ RectangleConstraint2.getRCCConstraint(recRels.get(i).get(j).getTypes()[j2].getAllenType()[0], recRels.get(i).get(j).getTypes()[j2].getAllenType()[1]) + "  ";
//						}
//					}
//					if(rectangleRel == "")
//						rectangleRel = " no Constraint ";
//					ret +=  recRels.get(i).get(j).getFrom() + rectangleRel + recRels.get(i).get(j).getTo() +"\n";
//				}
//		}
//		return ret;
//	}
//
//
//	private boolean weakPathConsistency(Vector<Vector<RectangleConstraint2>> recRels) {
//		int numVars = this.getVariables().length;
//		//need to cycle at least (numVars^2 - numVars) times
//		int counter = numVars*numVars - numVars;
//		boolean[][] mark = new boolean[numVars][numVars];
//		
//		for(int i = 0; i < numVars; i++) {
//			for(int j = 0; j < numVars; j++) {
//				if(i == j) mark[i][j] = false;
//				else mark[i][j] = true;
//			}
//		}	
//		
//		while(counter != 0) { // while the set is not empty
//			for(int i = 0; i < numVars; i++) {
//				for(int j = 0; j < numVars; j++) {
//					if(i == j) continue;
//					if(mark[i][j]){
//						mark[i][j] = false;	
//						counter--; //remove from set
//						for(int k = 0; k < numVars; k++) {
//							if(k == i || k == j) continue;
//							//process relation (k,j)
//							//back up relation (k,j)
//							RectangleConstraint2 rectmp = (RectangleConstraint2)recRels.get(k).get(j).clone();
//							//(k,j) <-- (k,j) n (k,i) + (i,j) 
//							if(!revise(recRels.get(k).get(j), recRels.get(k).get(i), recRels.get(i).get(j)))
//								return false;
//
//							//if changed, must re-process (k,j)
//							
//							if(!compareRelation(rectmp, recRels.get(k).get(j))) {
//								
//								if(!mark[k][j]) {
//									mark[k][j] = true;
//									counter++;
//								}
//							}
//							//process relation (i,k)
//							//back up relation (i,k)
//							rectmp = (RectangleConstraint2)recRels.get(i).get(k).clone();
//							//(i,k) <-- (i,k) n (i,j) + (j,k)
//							
//							if(!revise(recRels.get(i).get(k), recRels.get(i).get(j), recRels.get(j).get(k)))
//								return false;
//					
//								
//							
//							//if changed, must re-process (i,k)
//							if(!compareRelation(rectmp, recRels.get(i).get(k))) {
//								if(!mark[i][k]) {
//									mark[i][k] = true;
//									counter++;
//								}
//							}
//						}//end of k loop	
//					}
//				}
//			}
//		}
//		return true;
//	}
//	
//	//this is not true
//	private boolean compareRelation(RectangleConstraint2 first, RectangleConstraint2 second) {
//				
//		boolean existed = false;
//		for (int i = 0; i < first.types.length; i++) {
//			existed = false;
//			for (int j = 0; j < second.types.length; j++) {
//				if(first.types[i].getAllenType()[0] == second.types[j].getAllenType()[0] && first.types[i].getAllenType()[1] == second.types[j].getAllenType()[1])
//					existed = true;
//			}
//			if(!existed) return false;
//		}
//		return true;		
//	}
//
//
//	private boolean revise(RectangleConstraint2 originalRealtions, RectangleConstraint2 recCons, RectangleConstraint2 recCons2) {
//	
//		Vector<QualitativeAllenIntervalConstraint.Type> xcomRelation = new Vector<QualitativeAllenIntervalConstraint.Type>();
//		Vector<QualitativeAllenIntervalConstraint.Type> ycomRelation = new Vector<QualitativeAllenIntervalConstraint.Type>();
//		for (int t = 0; t < recCons.types.length; t++) {
//			for (int t2 = 0; t2 < recCons2.types.length; t2++) {
//				QualitativeAllenIntervalConstraint.Type[] xType = QualitativeAllenIntervalConstraint.transitionTable[recCons.types[t].getAllenType()[0].ordinal()][recCons2.types[t2].getAllenType()[0].ordinal()];
//				QualitativeAllenIntervalConstraint.Type[] yType = QualitativeAllenIntervalConstraint.transitionTable[recCons.types[t].getAllenType()[1].ordinal()][recCons2.types[t2].getAllenType()[1].ordinal()];
//				for(QualitativeAllenIntervalConstraint.Type t3: xType){
//					if(!xcomRelation.contains(t3))				
//						xcomRelation.add(t3);
//				}
//				for(QualitativeAllenIntervalConstraint.Type t3: yType){
//						if(!ycomRelation.contains(t3))				
//							ycomRelation.add(t3);
//				}
//			}
//		}
//		//zero for x and 1 for y
//		Vector<Integer> xnonexists = getNonExistedIndices(originalRealtions, xcomRelation, 0);
//		Vector<Integer> ynonexists = getNonExistedIndices(originalRealtions, ycomRelation, 1);
//		
//		//TwoDimensionsAllenConstraint[] t = new TwoDimensionsAllenConstraint[originalRealtions.getTypes().length - Math.min(xnonexists.size(), ynonexists.size())]; 
//		Vector<TwoDimensionsAllenConstraint> t = new Vector<TwoDimensionsAllenConstraint>();
//		
//		for (int i = 0; i < originalRealtions.getTypes().length; i++) 
//			if((!xnonexists.contains(i)) && (!ynonexists.contains(i)))
//				t.add(originalRealtions.getTypes()[i]);
//			
//		originalRealtions.setTypes(t.toArray(new TwoDimensionsAllenConstraint[t.size()]));
//		//if the result of intersection is empty 
//		if(t.size() == 0) return false;
//		
//		// TODO Auto-generated method stub
//		return true;
//	}
//
//
//
//
//	private Vector<Integer> getNonExistedIndices(
//			RectangleConstraint2 originalRealtions, Vector<Type> comRelation, int dimension) {
//		
//		Vector<Integer> nonexists = new Vector<Integer>();
//		boolean exist = false;
//		for (int i = 0; i < originalRealtions.getTypes().length; i++) {
//			exist = false;
//			for (int j = 0; j < comRelation.size(); j++) {
//				if(originalRealtions.getTypes()[i].getAllenType()[dimension].equals(comRelation.get(j))){
//					exist = true;
//					break;
//				}
//			}
//			if(!exist) nonexists.add(i);
//		}
//		
//		return nonexists;
//
//	}
//
//
//	private Vector<Vector<RectangleConstraint2>> createAllenCompleteNetwork(Constraint[] c) {
//
//		RectangleConstraint2[][] tmp = new RectangleConstraint2[this.getVariables().length][this.getVariables().length];
//		int row = 0 , col = 0;
//		for(int i = 0; i < c.length; i++){
//			row = this.getID(c[i].getScope()[0]);
//			col = this.getID(c[i].getScope()[1]);
//			tmp[row][col] = (RectangleConstraint2)c[i];
//		}
//		
//		for(int i = 0; i < tmp.length; i++){
//			Vector<RectangleConstraint2> con = new Vector<RectangleConstraint2>(); 
//			for(int j = 0; j < tmp.length; j++){
//				if(tmp[i][j] != null)  						
//					con.add(tmp[i][j]);
//				else if (tmp[j][i] != null){
//					Vector<TwoDimensionsAllenConstraint> invers2DAllenRelains = new Vector<TwoDimensionsAllenConstraint>();
//					for (int k = 0; k < tmp[j][i].getTypes().length; k++) {
//						invers2DAllenRelains.add(new TwoDimensionsAllenConstraint(QualitativeAllenIntervalConstraint.getInverseRelation(tmp[j][i].types[k].getAllenType()[0]), QualitativeAllenIntervalConstraint.getInverseRelation(tmp[j][i].types[k].getAllenType()[1])));
//					}
//					RectangleConstraint2 rc = new RectangleConstraint2(invers2DAllenRelains.toArray(new TwoDimensionsAllenConstraint[invers2DAllenRelains.size()]));
//					rc.setFrom(getVaribaleById.get(i));
//					rc.setTo(getVaribaleById.get(j));
//					con.add(rc);	
//				}
//				//if no relation exists
//				else{
//					RectangleConstraint2 universe = createUnivreseRectangleRelation();
//					universe.setFrom(getVaribaleById.get(i));
//					universe.setTo(getVaribaleById.get(j));
//					con.add(universe);
//				}
//			}
//			recRels.add(con);
//		}
//		return recRels;
//	}
//
//
//	private RectangleConstraint2 createUnivreseRectangleRelation() {
//		
//		Vector<TwoDimensionsAllenConstraint> tdr = new Vector<TwoDimensionsAllenConstraint>();  
//		for (int i = 0; i < QualitativeAllenIntervalConstraint.Type.values().length; i++) 
//			for (int j = 0; j < QualitativeAllenIntervalConstraint.Type.values().length; j++)
//				tdr.add(new TwoDimensionsAllenConstraint(QualitativeAllenIntervalConstraint.Type.values()[i], QualitativeAllenIntervalConstraint.Type.values()[j]));
//
//		return new RectangleConstraint2(tdr.toArray(new TwoDimensionsAllenConstraint[tdr.size()]));
//	}	



}
	
