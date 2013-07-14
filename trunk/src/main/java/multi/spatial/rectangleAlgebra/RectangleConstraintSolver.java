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
package multi.spatial.rectangleAlgebra;

import java.util.HashMap;

import multi.allenInterval.AllenInterval;
import multi.allenInterval.AllenIntervalNetworkSolver;
import time.Bounds;
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

public class RectangleConstraintSolver extends MultiConstraintSolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6694598836324746725L;
	private int IDs = 0;
	
	private HashMap<Integer, Variable> getVaribaleById = new HashMap<Integer, Variable>();
	public enum Dimension  {X, Y};

	public RectangleConstraintSolver(long origin, long horizon) {
		super(new Class[] {RectangleConstraint.class, UnaryRectangleConstraint.class}, RectangularRegion.class, createConstraintSolvers(origin, horizon, -1), new int[] {1,1});
	}

	public RectangleConstraintSolver(long origin, long horizon, int maxRectangles) {
		super(new Class[] {RectangleConstraint.class}, RectangularRegion.class, createConstraintSolvers(origin, horizon, maxRectangles), new int[] {1,1});
	}

	private static ConstraintSolver[] createConstraintSolvers(long origin, long horizon, int maxRectangles) {
		ConstraintSolver[] ret = new ConstraintSolver[2];
		//X
		ret[0] = (maxRectangles != -1 ? new AllenIntervalNetworkSolver(origin, horizon, maxRectangles) : new AllenIntervalNetworkSolver(origin, horizon));
		//Y
		ret[1] = (maxRectangles != -1 ? new AllenIntervalNetworkSolver(origin, horizon, maxRectangles) : new AllenIntervalNetworkSolver(origin, horizon));
		return ret;
	}
	
//	@Override
//	protected Variable[] createVariablesSub(int num) {
//		Variable[] ret = new Variable[num];
//		for (int i = 0; i < num; i++) ret[i] = new RectangularRegion2(this, this.IDs++, this.constraintSolvers);
//		return ret;
//	}

	@Override
	public boolean propagate() {
		// Do nothing, AllenIntervalNetworkSolver takes care of propagation...
		return true;
	}

	/**
	 * make script readable for gnuplot
	 * @param st
	 */
	public String drawAlmostCentreRectangle(long horizon, RectangularRegion ... rect){
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
	public BoundingBox extractBoundingBoxesFromSTPs(RectangularRegion rect){
		Bounds xLB, xUB, yLB, yUB;
		xLB = new Bounds(((AllenInterval)rect.getInternalVariables()[0]).getEST(),((AllenInterval)rect.getInternalVariables()[0]).getLST()); 
		xUB = new Bounds(((AllenInterval)rect.getInternalVariables()[0]).getEET(),((AllenInterval)rect.getInternalVariables()[0]).getLET()); 
		yLB = new Bounds(((AllenInterval)rect.getInternalVariables()[1]).getEST(),((AllenInterval)rect.getInternalVariables()[1]).getLST()); 
		yUB = new Bounds(((AllenInterval)rect.getInternalVariables()[1]).getEET(),((AllenInterval)rect.getInternalVariables()[1]).getLET()); 
		return new BoundingBox(xLB, xUB, yLB, yUB);
	}
	
	public BoundingBox extractBoundingBoxesFromSTPs(String name){
		
		Bounds xLB, xUB, yLB, yUB;
		for (int i = 0; i < this.getConstraintSolvers()[0].getVariables().length; i++) {
			if(((RectangularRegion)this.getConstraintNetwork().getVariables()[i]).getName().compareTo(name) == 0){
				xLB = new Bounds(((AllenInterval)this.getConstraintSolvers()[0].getVariables()[i]).getEST(), ((AllenInterval)this.getConstraintSolvers()[0].getVariables()[i]).getLST());
				xUB = new Bounds(((AllenInterval)this.getConstraintSolvers()[0].getVariables()[i]).getEET(), ((AllenInterval)this.getConstraintSolvers()[0].getVariables()[i]).getLET());
				yLB = new Bounds(((AllenInterval)this.getConstraintSolvers()[1].getVariables()[i]).getEST(), ((AllenInterval)this.getConstraintSolvers()[1].getVariables()[i]).getLST());
				yUB = new Bounds(((AllenInterval)this.getConstraintSolvers()[1].getVariables()[i]).getEET(), ((AllenInterval)this.getConstraintSolvers()[1].getVariables()[i]).getLET());
				return new BoundingBox(xLB, xUB, yLB, yUB);
			}
		}
		return null;
	}
	

}
	
