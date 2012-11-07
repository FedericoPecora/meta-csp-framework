package spatial.rectangleAlgebra;

import java.util.Collections;
import java.util.Vector;

import spatial.RCC.RCCConstraint;
import framework.BinaryConstraint;
import framework.Constraint;

public class RectangleConstraint extends BinaryConstraint {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 304977081496019725L;

	public RectangleConstraint(TwoDimensionsAllenConstraint... types) {
		this.types = types;
	}

	protected TwoDimensionsAllenConstraint[] types;	
	
	public TwoDimensionsAllenConstraint[] getTypes() {
		return types;
	}

	public void setTypes(TwoDimensionsAllenConstraint... types) {
		this.types = types;
	}
	

	
	

	
	public String getEdgeLabel() {
		if (types.length == 0) return null;
		if (types.length == 1) return "(" + types[0].getAllenType()[0].toString()+ ", " + types[0].getAllenType()[1] + ")";
		String ret = "{";
		for (TwoDimensionsAllenConstraint t : this.types) ret += "(" + (t.getAllenType()[0] + ", " + t.getAllenType()[1]+ ")" + " v ");
		return ret.substring(0, ret.length()-2) + "}";
		
	}
	

	@Override
	public Object clone() {
		return new RectangleConstraint(this.types);
	}

	@Override
	public boolean isEquivalent(Constraint c) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public String toString() {
		String ret = "[" + this.getFrom() + " ---" ;
		for (int i = 0; i < types.length; i++) {
			ret +=" (" + this.types[i].getAllenType()[0] + ", " + this.types[i].getAllenType()[1] + "),  " ; 
		}
		ret += "--> (" + this.getTo() + "]";
		return ret;

	}
	
	public static RCCConstraint.Type getRCCConstraint(QualitativeAllenIntervalConstraint.Type x,  QualitativeAllenIntervalConstraint.Type y){
				
		return ReactangleToRCC[x.ordinal()][y.ordinal()];
	}
	
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
	
	
	/**
	 * Get the dimension of a RA relation (A, B) which is dim(A) + dim(B) such that A, B are basic Allen relation
	 * @param types an array of 2D Allen relations.
	 * @return The dimension of RA relation.
	 */
	public static int getDimension(TwoDimensionsAllenConstraint... types){
		if(types.length == 0)
			return -1;
		Vector<Integer> xDims = new Vector<Integer>(); 
		Vector<Integer> yDims = new Vector<Integer>();
		for (int i = 0; i < types.length; i++) {
			xDims.add(QualitativeAllenIntervalConstraint.getDimension(types[i].getAllenType()[0]));
			yDims.add(QualitativeAllenIntervalConstraint.getDimension(types[i].getAllenType()[1]));
		}
		return Collections.max(xDims) + Collections.max(yDims);
	}
	
	/**
	 * Get convex closure of RA relation which is I(R) = I(R1) * I(R2)  
	 * @param types an array of basic RA relations.
	 * @return Convex closure of the RA relation.
	 */
	public static TwoDimensionsAllenConstraint[] getRAConvexClosure(TwoDimensionsAllenConstraint... types){
		
		Vector<QualitativeAllenIntervalConstraint.Type> xDims = new Vector<QualitativeAllenIntervalConstraint.Type>(); 
		Vector<QualitativeAllenIntervalConstraint.Type> yDims = new Vector<QualitativeAllenIntervalConstraint.Type>();
		Vector<TwoDimensionsAllenConstraint> twoDAllenVec = new Vector<TwoDimensionsAllenConstraint>(); 
		for (int i = 0; i < types.length; i++) {
			xDims.add(types[i].getAllenType()[0]);
			yDims.add(types[i].getAllenType()[1]);
		}
		
		QualitativeAllenIntervalConstraint.Type[] ir1 =	QualitativeAllenIntervalConstraint.getAllenConvexClosure(xDims.toArray(new QualitativeAllenIntervalConstraint.Type[xDims.size()]));
		QualitativeAllenIntervalConstraint.Type[] ir2 = QualitativeAllenIntervalConstraint.getAllenConvexClosure(yDims.toArray(new QualitativeAllenIntervalConstraint.Type[yDims.size()]));
		for (int i = 0; i < ir1.length; i++) {
			for (int j = 0; j < ir2.length; j++) 
				twoDAllenVec.add(new TwoDimensionsAllenConstraint(ir1[i], ir2[j]));
		}
		return twoDAllenVec.toArray(new TwoDimensionsAllenConstraint[twoDAllenVec.size()]);
	}
	
	/**
	 * Define whether the RA relation is convex or not, R is a convex relation if dim(I(R)\R) < dim(R) [Balbiani et al, 99]  
	 * @param types an array of basic 2DAllen relation.
	 * @return true iff the relation is convex.
	 */
	public static boolean isConvexRelation(TwoDimensionsAllenConstraint... types){
		
		TwoDimensionsAllenConstraint[] convexClosure = getRAConvexClosure(types);
		Vector<TwoDimensionsAllenConstraint> convexSubtract = new Vector<TwoDimensionsAllenConstraint	>();

		//compute the relative complement 
		for (int i = 0; i < convexClosure.length; i++) {
			int counter = 0;
			for (int j = 0; j < types.length; j++) {
				if(convexClosure[i].getAllenType()[0].equals(types[j].getAllenType()[0]) && convexClosure[i].getAllenType()[1].equals(types[j].getAllenType()[1]));
				else
					counter++;
				
			}
			if(counter == types.length)
				convexSubtract.add(convexClosure[i]);
		}
		if(getDimension(convexSubtract.toArray(new TwoDimensionsAllenConstraint[convexSubtract.size()])) < getDimension(types))
			return true;
		return false;
	}
	
	/**
	 * Define the topological closure of RA relation which is U{C(A, B) : (A, B) is member of R, R is member of power set of basic RA relation, and C(A, B) = C(A) * C(B)}  
	 * @param types an array of RA relations.
	 * @return topological closure of RA.
	 */
	public static TwoDimensionsAllenConstraint[] getRATopologicalClosure(TwoDimensionsAllenConstraint... types){		
		Vector<QualitativeAllenIntervalConstraint.Type> xDims = new Vector<QualitativeAllenIntervalConstraint.Type>(); 
		Vector<QualitativeAllenIntervalConstraint.Type> yDims = new Vector<QualitativeAllenIntervalConstraint.Type>();
		 
		for (int i = 0; i < types.length; i++) {
			for (int j = 0; j < QualitativeAllenIntervalConstraint.topologicalClosure[types[i].getAllenType()[0].ordinal()].length; j++){
				if(!xDims.contains(QualitativeAllenIntervalConstraint.topologicalClosure[types[i].getAllenType()[0].ordinal()][j]))
					xDims.add(QualitativeAllenIntervalConstraint.topologicalClosure[types[i].getAllenType()[0].ordinal()][j]);
			}
			for (int j = 0; j < QualitativeAllenIntervalConstraint.topologicalClosure[types[i].getAllenType()[1].ordinal()].length; j++)
				if(!yDims.contains(QualitativeAllenIntervalConstraint.topologicalClosure[types[i].getAllenType()[1].ordinal()][j]))
					yDims.add(QualitativeAllenIntervalConstraint.topologicalClosure[types[i].getAllenType()[1].ordinal()][j]);
		}
		
		Vector<TwoDimensionsAllenConstraint> topologicalVec = new Vector<TwoDimensionsAllenConstraint>();
		for (int i = 0; i < xDims.size(); i++) {
			for (int j = 0; j < yDims.size(); j++) {
				topologicalVec.add(new TwoDimensionsAllenConstraint(xDims.get(i), yDims.get(j)));
			}
		}
		return topologicalVec.toArray(new TwoDimensionsAllenConstraint[topologicalVec.size()]);
	}
	
	/**
	 * Define whether the RA relation is weakly preconvex or not, R is a weakly preconvex relation if C(R) is a convex relation [Balbiani et al, 99]  
	 * @param types an array of basic 2D Allen relations.
	 * @return true iff the relation is weakly preconvex.
	 */
	public static boolean isWeakPreconvexRelation(TwoDimensionsAllenConstraint... types){
		if(isConvexRelation(getRATopologicalClosure(types)))
			return true;
		return false;
	}

}
