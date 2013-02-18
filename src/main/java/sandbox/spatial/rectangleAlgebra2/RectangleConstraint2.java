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


import cern.colt.Arrays;
import multi.allenInterval.AllenIntervalConstraint;

import spatial.RCC.RCCConstraint;
import spatial.cardinal.CardinalConstraint;
import spatial.cardinal.CardinalConstraint.Type;
import spatial.rectangleAlgebra.TwoDimensionsAllenConstraint;
import framework.Constraint;
import framework.Variable;
import framework.multi.MultiBinaryConstraint;
import framework.multi.MultiVariable;

public class RectangleConstraint2 extends MultiBinaryConstraint {

	private static final long serialVersionUID = 304977081496019725L;
	private AllenIntervalConstraint xConstraint, yConstraint;
	private AllenIntervalConstraint.Type[][] types = new AllenIntervalConstraint.Type[2][];
	
	public RectangleConstraint2(AllenIntervalConstraint xConstraint, AllenIntervalConstraint yConstraint) {
		this.xConstraint = xConstraint;
		this.yConstraint = yConstraint;
		this.types[0] = xConstraint.getTypes();
		this.types[1] = yConstraint.getTypes();
	}
	
	public AllenIntervalConstraint.Type[][] getType() { return types; }
		
	public static RCCConstraint.Type getRCCConstraint(AllenIntervalConstraint.Type x,  AllenIntervalConstraint.Type y){
				
		return ReactangleToRCC[x.ordinal()][y.ordinal()];
	}
	
	public static CardinalConstraint.Type getCardinalConstraint(RectangleConstraint2 c){
		
		if(c.getType()[0][0].compareTo(AllenIntervalConstraint.Type.Equals) == 0 && c.getType()[1][0].compareTo(AllenIntervalConstraint.Type.Equals) == 0)
			return CardinalConstraint.Type.EQUAL;
		return makeCardinalBy2Dim(ReactangleToCardinalX[c.getType()[0][0].ordinal()], ReactangleToCardinalY[c.getType()[1][0].ordinal()]);

	}
	
	private static Type makeCardinalBy2Dim(Type t1, Type t2) {
		
		if(t1.compareTo(CardinalConstraint.Type.NO) == 0 && t2.compareTo(CardinalConstraint.Type.NO) == 0)
			return CardinalConstraint.Type.NO;
		if(t1.compareTo(CardinalConstraint.Type.NO) == 0 && t2.compareTo(CardinalConstraint.Type.NO) != 0)
			return t2;
		if(t1.compareTo(CardinalConstraint.Type.NO) != 0 && t2.compareTo(CardinalConstraint.Type.NO) == 0)
			return t1;
		if(t1.compareTo(CardinalConstraint.Type.East) == 0 && t2.compareTo(CardinalConstraint.Type.North) == 0)
			return CardinalConstraint.Type.NorthEast;
		if(t1.compareTo(CardinalConstraint.Type.West) == 0 && t2.compareTo(CardinalConstraint.Type.North) == 0)
			return CardinalConstraint.Type.NorthWest;
		if(t1.compareTo(CardinalConstraint.Type.East) == 0 && t2.compareTo(CardinalConstraint.Type.South) == 0)
			return CardinalConstraint.Type.SouthEast;
		if(t1.compareTo(CardinalConstraint.Type.West) == 0 && t2.compareTo(CardinalConstraint.Type.South) == 0)
			return CardinalConstraint.Type.SouthWest;
		
		return null;
	}

	public static CardinalConstraint.Type[] ReactangleToCardinalX = {
	
			CardinalConstraint.Type.West, //before			
			CardinalConstraint.Type.West, //meets
			CardinalConstraint.Type.West, //overlaps
			CardinalConstraint.Type.NO, //fi
			CardinalConstraint.Type.NO, //di
			CardinalConstraint.Type.NO, //si
			CardinalConstraint.Type.NO, //=
			CardinalConstraint.Type.NO, //s
			CardinalConstraint.Type.NO, //d
			CardinalConstraint.Type.NO, //f
			CardinalConstraint.Type.East, //oi
			CardinalConstraint.Type.East, //mi
			CardinalConstraint.Type.East, //bi

	};
	
	public static CardinalConstraint.Type[] ReactangleToCardinalY = {
		
		CardinalConstraint.Type.South, //before			
		CardinalConstraint.Type.South, //meets
		CardinalConstraint.Type.South, //overlaps
		CardinalConstraint.Type.NO, //fi
		CardinalConstraint.Type.NO, //di
		CardinalConstraint.Type.NO, //si
		CardinalConstraint.Type.NO, //=
		CardinalConstraint.Type.NO, //s
		CardinalConstraint.Type.NO, //d
		CardinalConstraint.Type.NO, //f
		CardinalConstraint.Type.North, //oi
		CardinalConstraint.Type.North, //mi
		CardinalConstraint.Type.North, //bi

};

	
	public static RCCConstraint.Type[][] ReactangleToRCC = {
		{
			//BEFORE
			RCCConstraint.Type.DC,
			RCCConstraint.Type.DC,
			RCCConstraint.Type.DC,
			RCCConstraint.Type.DC,
			RCCConstraint.Type.DC,
			RCCConstraint.Type.DC,
			RCCConstraint.Type.DC,
			RCCConstraint.Type.DC,
			RCCConstraint.Type.DC,
			RCCConstraint.Type.DC,
			RCCConstraint.Type.DC,
			RCCConstraint.Type.DC,
			RCCConstraint.Type.DC,
		},
		{
			//MEETS
			RCCConstraint.Type.DC,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.DC,
		},
		{
			//Overlaps

			RCCConstraint.Type.DC,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.DC,
		},
		{
			//FinishedBY
			RCCConstraint.Type.DC,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.TPPI,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.TPPI,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.DC,
		},
		{
			//Contains
			RCCConstraint.Type.DC,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.NTPPI,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.TPPI,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.DC,
			RCCConstraint.Type.EC,
		},
		{
			//startedBY
			RCCConstraint.Type.DC,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.TPPI,
			RCCConstraint.Type.TPPI,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.TPP,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.DC,
		},
		{
			//Equals
			RCCConstraint.Type.EC,
			RCCConstraint.Type.DC,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.TPPI,
			RCCConstraint.Type.TPPI,
			RCCConstraint.Type.TPPI,
			RCCConstraint.Type.EQ,
			RCCConstraint.Type.TPP,
			RCCConstraint.Type.TPP,
			RCCConstraint.Type.TPP,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.DC,
		},
		{
			//STARTS
			RCCConstraint.Type.DC,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.TPP,
			RCCConstraint.Type.TPP,
			RCCConstraint.Type.TPP,
			RCCConstraint.Type.TPP,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.DC,
		},
		{
			//DURING
			RCCConstraint.Type.DC,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.TPP,
			RCCConstraint.Type.TPP,
			RCCConstraint.Type.TPP,
			RCCConstraint.Type.NTPP,
			RCCConstraint.Type.TPP,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.DC,
		},
		
		{
			//FINISHES
			RCCConstraint.Type.DC,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.TPP,
			RCCConstraint.Type.TPP,
			RCCConstraint.Type.TPP,
			RCCConstraint.Type.TPP,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.DC,
		},
		{
			//OVERLAPPEDBY
			RCCConstraint.Type.DC,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.PO,
			RCCConstraint.Type.DC,
			RCCConstraint.Type.EC,
		},
		{
			//METBY
			RCCConstraint.Type.DC,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.EC,
			RCCConstraint.Type.DC,
		},
		{
			//AFTER
			RCCConstraint.Type.DC,
			RCCConstraint.Type.DC,
			RCCConstraint.Type.DC,
			RCCConstraint.Type.DC,
			RCCConstraint.Type.DC,
			RCCConstraint.Type.DC,
			RCCConstraint.Type.DC,
			RCCConstraint.Type.DC,
			RCCConstraint.Type.DC,
			RCCConstraint.Type.DC,
			RCCConstraint.Type.DC,
			RCCConstraint.Type.DC,
			RCCConstraint.Type.DC,
		}
		


	};//3 by 3 composition Table


	@Override
	protected Constraint[] createInternalConstraints(Variable from, Variable to) {
		this.xConstraint.setFrom(((RectangularRegion2)from).getInternalVariables()[0]);
		this.xConstraint.setTo(((RectangularRegion2)to).getInternalVariables()[0]);
		this.yConstraint.setFrom(((RectangularRegion2)from).getInternalVariables()[1]);
		this.yConstraint.setTo(((RectangularRegion2)to).getInternalVariables()[1]);
		//xConstraint should not be processed by Y solver		
		xConstraint.skipSolver(((MultiVariable)from).getInternalConstraintSolvers()[1]);
		//yConstraint should not be processed by X solver
		yConstraint.skipSolver(((MultiVariable)from).getInternalConstraintSolvers()[0]);
		return new Constraint[] {this.xConstraint, this.yConstraint};
	}

	@Override
	public boolean isEquivalent(Constraint c) {
		return xConstraint.isEquivalent(((RectangleConstraint2)c).getInternalConstraints()[0]) && yConstraint.isEquivalent(((RectangleConstraint2)c).getInternalConstraints()[1]);
	}
	
	@Override
	public Object clone() {
		RectangleConstraint2 ret = new RectangleConstraint2(xConstraint, yConstraint);
		return ret;
	}
	
	public String toString() {
		String ret = "[" + this.getFrom() + " ---" ;
		ret +="(" + Arrays.toString(this.types[0]) + ", " + Arrays.toString(this.types[1]) + ")" ; 
		ret += "--> (" + this.getTo() + "]";
		return ret;
	}

	@Override
	public String getEdgeLabel() {
		return "(" + Arrays.toString(this.types[0]) + ", " + Arrays.toString(this.types[1]) + ")" ; 
	}

	
}
