package org.metacsp.multi.spatial.blockAlgebra;

import java.util.Arrays;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiBinaryConstraint;
import org.metacsp.framework.multi.MultiVariable;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.spatial.RCC.RCCConstraint;
import org.metacsp.spatial.cardinal.CardinalConstraint;
import org.metacsp.spatial.cardinal.CardinalConstraint.Type;

public class BlockAlgebraConstraint extends MultiBinaryConstraint {

	private static final long serialVersionUID = 304977081496019725L;
	private AllenIntervalConstraint xConstraint, yConstraint, zConstraint;
	private AllenIntervalConstraint.Type[][] types = new AllenIntervalConstraint.Type[3][];
	
	public BlockAlgebraConstraint(AllenIntervalConstraint xConstraint, AllenIntervalConstraint yConstraint, AllenIntervalConstraint zConstraint) {
		this.xConstraint = xConstraint;
		this.yConstraint = yConstraint;
		this.zConstraint = zConstraint;
		this.types[0] = xConstraint.getTypes();
		this.types[1] = yConstraint.getTypes();
		this.types[2] = yConstraint.getTypes();
	}
	
	public AllenIntervalConstraint.Type[][] getType() { return types; }
		
	public static RCCConstraint.Type getRCCConstraint(AllenIntervalConstraint.Type x,  AllenIntervalConstraint.Type y){
				
		return ReactangleToRCC[x.ordinal()][y.ordinal()];
	}
	
	public AllenIntervalConstraint[] getInternalAllenIntervalConstraints(){
		return new AllenIntervalConstraint[] {this.xConstraint, this.yConstraint, this.zConstraint};	
	}
	
	public static CardinalConstraint.Type getCardinalConstraint(BlockAlgebraConstraint c){
		
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
		this.xConstraint.setFrom(((RectangularCuboidRegion)from).getInternalVariables()[0]);
		this.xConstraint.setTo(((RectangularCuboidRegion)to).getInternalVariables()[0]);
		this.yConstraint.setFrom(((RectangularCuboidRegion)from).getInternalVariables()[1]);
		this.yConstraint.setTo(((RectangularCuboidRegion)to).getInternalVariables()[1]);
		this.zConstraint.setFrom(((RectangularCuboidRegion)from).getInternalVariables()[2]);
		this.zConstraint.setTo(((RectangularCuboidRegion)to).getInternalVariables()[2]);
		//xConstraint should not be processed by Y and Z solver		
		xConstraint.skipSolver(((MultiVariable)from).getInternalConstraintSolvers()[1], ((MultiVariable)from).getInternalConstraintSolvers()[2]);
		//yConstraint should not be processed by X and Z solver 
		yConstraint.skipSolver(((MultiVariable)from).getInternalConstraintSolvers()[0], ((MultiVariable)from).getInternalConstraintSolvers()[2]);
		//zConstraint should not be processed by X and Y solver 
		zConstraint.skipSolver(((MultiVariable)from).getInternalConstraintSolvers()[0], ((MultiVariable)from).getInternalConstraintSolvers()[1]);
		return new Constraint[] {this.xConstraint, this.yConstraint, this.zConstraint};
	}

	@Override
	public boolean isEquivalent(Constraint c) {
		return xConstraint.isEquivalent(((BlockAlgebraConstraint)c).getInternalConstraints()[0]) && yConstraint.isEquivalent(((BlockAlgebraConstraint)c).getInternalConstraints()[1]) 
				&& zConstraint.isEquivalent(((BlockAlgebraConstraint)c).getInternalConstraints()[2]);
	}
	
	@Override
	public Object clone() {
		BlockAlgebraConstraint ret = new BlockAlgebraConstraint(xConstraint, yConstraint, zConstraint);
		return ret;
	}
	
	public String toString() {
		String ret = "[" + this.getFrom() + " ---" ;
		ret +="(" + Arrays.toString(this.types[0]) + ", " + Arrays.toString(this.types[1]) + ", " + Arrays.toString(this.types[2]) + ")" ; 
		ret += "--> (" + this.getTo() + "]";
		return ret;
	}

	@Override
	public String getEdgeLabel() {
		return "(" + Arrays.toString(this.types[0]) + ", " + Arrays.toString(this.types[1]) + ", " + Arrays.toString(this.types[2]) +")" ; 
	}

	
}
