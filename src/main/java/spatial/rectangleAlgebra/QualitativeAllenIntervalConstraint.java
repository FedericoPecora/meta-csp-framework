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

import java.awt.Point;
import java.util.Collections;
import java.util.Vector;

import framework.BinaryConstraint;
import framework.Constraint;
import fuzzyAllenInterval.FuzzyAllenIntervalConstraint;

/**
 * There are 13 types of Allen interval constraints (see [Allen 1984]).
 *  
 * @author Iran Mansouri
 *
 */


public class QualitativeAllenIntervalConstraint extends BinaryConstraint {


	/**
	 * 
	 */
	private static final long serialVersionUID = -4615004022622018572L;

	public static enum Type {
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A BEFORE B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../images/beforeQ.png> 
		 */
		Before,

		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A MEETS B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../images/meetsQ.png> 
		 */
		Meets,
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A OVERLAPS B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../images/overlapsQ.png> 
		 */
		Overlaps,

		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A FINISHED-BY B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../images/finishedbyQ.png> 
		 */
		FinishedBy,
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A CONTAINS B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../images/containsQ.png> 
		 */
		Contains,


		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A STARTED-BY B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../images/startedbyQ.png> 
		 */
		StartedBy,
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A EQUALS B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../images/equalsQ.png> 
		 */
		Equals,

		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A STARTS B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../images/startsQ.png> 
		 */
		Starts,
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A DURING B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../images/duringQ.png> 
		 */
		During,

		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A FINISHES B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../images/finishesQ.png> 
		 */
		Finishes,
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A OVERLAPPED-BY B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../images/overlappedbyQ.png> 
		 */
		OverlappedBy,
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A MET-BY B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../images/metbyQ.png> 
		 */
		MetBy,
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A AFTER B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../images/afterQ.png> 
		 */
		After
	};
	
	protected Type[] types;


	
	/**
	 * Create a {@link FuzzyAllenIntervalConstraint} given an array of constraint types
	 * whose desired possibility is 1. 
	 * @param types
	 */
	public QualitativeAllenIntervalConstraint(Type... types) {
		this.types = types;
		//this.isSensoryRelation = false;
	}
	
	/**
	 * Query whether this constraint's desired possibility for a given type is 1.
	 * @param type The type to query.
	 * @return <code>true</code> iff the constraint prescribes that the given type
	 * has possibility 1. 
	 */
	public boolean containsType(Type type) {
		for (Type t : types)
			if (t.equals(type)) return true;
		return false;
	}
	
	/**
	 * Get all types with desired possibility 1.
	 * @return All types with desired possibility 1.
	 */
	public Type[] getTypes() {
		return types;
	}

	/**
	 * Set all types with desired possibility 1.
	 * @param types The types whose desired possibility should be 1.
	 */
	public void setTypes(Type[] types) {
		this.types = types;
	}


		
	@Override
	public Object clone() {
		QualitativeAllenIntervalConstraint ret = new QualitativeAllenIntervalConstraint(); 
		ret.setTypes(types);
		return ret;
	}
	
	public String getEdgeLabel() {
		if (types.length == 0) return null;
		if (types.length == 1) return types[0].toString();
		String ret = "{";
		for (Type t : types) ret += (t + " v ");
		return ret.substring(0, ret.length()-3) + "}";
	}
	
	/**
	 * The composition used for fuzzy path consistency.
	 */
	public static Type[][][] transitionTable = {
		{
			{Type.Before},
			{Type.Before},
			{Type.Before},
			{Type.Before},
			{Type.Before},
			{Type.Before},
			{Type.Before},
			{Type.Before},
			{Type.Before, Type.Meets, Type.Overlaps, Type.Starts, Type.During},
			{Type.Before, Type.Meets, Type.Overlaps, Type.Starts, Type.During},
			{Type.Before, Type.Meets, Type.Overlaps, Type.Starts, Type.During},
			{Type.Before, Type.Meets, Type.Overlaps, Type.Starts, Type.During},
			{Type.Before, Type.Meets, Type.Overlaps, Type.Starts, Type.During, Type.Finishes, Type.Equals, Type.FinishedBy, Type.Contains, Type.StartedBy, Type.OverlappedBy, Type.MetBy, Type.After}
		},
		{
			{Type.Before},
			{Type.Before},
			{Type.Before},
			{Type.Before},
			{Type.Before},
			{Type.Meets},
			{Type.Meets},
			{Type.Meets},
			{Type.Overlaps, Type.Starts, Type.During},
			{Type.Overlaps, Type.Starts, Type.During},
			{Type.Overlaps, Type.Starts, Type.During},
			{Type.Finishes, Type.Equals, Type.FinishedBy},
			{Type.Contains, Type.StartedBy, Type.OverlappedBy, Type.MetBy, Type.After}		
		},
		{
			{Type.Before},
			{Type.Before},
			{Type.Before, Type.Meets, Type.Overlaps},
			{Type.Before, Type.Meets, Type.Overlaps},
			{Type.Before, Type.Meets, Type.Overlaps, Type.FinishedBy, Type.Contains},
			{Type.Overlaps, Type.FinishedBy, Type.Contains},
			{Type.Overlaps},
			{Type.Overlaps},
			{Type.Overlaps, Type.Starts, Type.During},
			{Type.Overlaps, Type.Starts, Type.During},
			{Type.Overlaps, Type.Starts, Type.During, Type.Finishes, Type.Equals, Type.FinishedBy, Type.Contains, Type.StartedBy, Type.OverlappedBy},
			{Type.Contains, Type.StartedBy, Type.OverlappedBy},
			{Type.Contains, Type.StartedBy, Type.OverlappedBy, Type.MetBy, Type.After}
		},
		{
			{Type.Before},
			{Type.Meets},
			{Type.Overlaps},
			{Type.FinishedBy},
			{Type.Contains},
			{Type.Contains},
			{Type.FinishedBy},
			{Type.Overlaps},
			{Type.Overlaps, Type.Starts, Type.During},
			{Type.Finishes, Type.Equals, Type.FinishedBy},
			{Type.Contains, Type.StartedBy, Type.OverlappedBy},
			{Type.Contains, Type.StartedBy, Type.OverlappedBy},
			{Type.Contains, Type.StartedBy, Type.OverlappedBy, Type.MetBy, Type.After}
		},
		{
			{Type.Before, Type.Meets, Type.Overlaps, Type.FinishedBy, Type.Contains},
			{Type.Overlaps, Type.FinishedBy, Type.Contains},
			{Type.Overlaps, Type.FinishedBy, Type.Contains},
			{Type.Contains},
			{Type.Contains},
			{Type.Contains},
			{Type.Contains},
			{Type.Overlaps, Type.FinishedBy, Type.Contains},
			{Type.Overlaps, Type.Starts, Type.During, Type.Finishes, Type.Equals, Type.FinishedBy, Type.Contains, Type.StartedBy, Type.OverlappedBy},
			{Type.Contains, Type.StartedBy, Type.OverlappedBy},
			{Type.Contains, Type.StartedBy, Type.OverlappedBy},
			{Type.Contains, Type.StartedBy, Type.OverlappedBy},
			{Type.Contains, Type.StartedBy, Type.OverlappedBy, Type.MetBy, Type.After}
		},
		{
			{Type.Before, Type.Meets, Type.Overlaps, Type.FinishedBy, Type.Contains},
			{Type.Overlaps, Type.FinishedBy, Type.Contains},
			{Type.Overlaps, Type.FinishedBy, Type.Contains},
			{Type.Contains},
			{Type.Contains},
			{Type.StartedBy},
			{Type.StartedBy},
			{Type.Starts, Type.Equals, Type.StartedBy},
			{Type.During, Type.Finishes, Type.OverlappedBy},
			{Type.OverlappedBy},
			{Type.OverlappedBy},
			{Type.MetBy},
			{Type.After}
		},
		{
			{Type.Before},
			{Type.Meets},
			{Type.Overlaps},
			{Type.FinishedBy},
			{Type.Contains},
			{Type.StartedBy},
			{Type.Equals},
			{Type.Starts},
			{Type.During},
			{Type.Finishes},
			{Type.OverlappedBy},
			{Type.MetBy},
			{Type.After}
		},
		{
			{Type.Before},
			{Type.Before},
			{Type.Before, Type.Meets, Type.Overlaps},
			{Type.Before, Type.Meets, Type.Overlaps},
			{Type.Before, Type.Meets, Type.Overlaps, Type.FinishedBy, Type.Contains},
			{Type.Starts, Type.Equals, Type.StartedBy},
			{Type.Starts},
			{Type.Starts},
			{Type.During},
			{Type.During},
			{Type.During, Type.Finishes, Type.OverlappedBy},
			{Type.MetBy},
			{Type.After}
		},
		{
			{Type.Before},
			{Type.Before},
			{Type.Before, Type.Meets, Type.Overlaps, Type.Starts, Type.During},
			{Type.Before, Type.Meets, Type.Overlaps, Type.Starts, Type.During},
			{Type.Before, Type.Meets, Type.Overlaps, Type.Starts, Type.During, Type.Finishes, Type.Equals, Type.FinishedBy, Type.Contains, Type.StartedBy, Type.OverlappedBy, Type.MetBy, Type.After},
			{Type.During, Type.Finishes, Type.OverlappedBy, Type.MetBy, Type.After},
			{Type.During},
			{Type.During},
			{Type.During},
			{Type.During},
			{Type.During, Type.Finishes, Type.OverlappedBy, Type.MetBy, Type.After},
			{Type.After},
			{Type.After},
		},
		{
			{Type.Before},
			{Type.Meets}, 
			{Type.Overlaps, Type.Starts, Type.During},
			{Type.Finishes, Type.Equals, Type.FinishedBy},
			{Type.Contains, Type.StartedBy, Type.OverlappedBy, Type.MetBy, Type.After},
			{Type.OverlappedBy, Type.MetBy, Type.After},
			{Type.Finishes},
			{Type.During},
			{Type.During},
			{Type.Finishes},
			{Type.OverlappedBy, Type.MetBy, Type.After},
			{Type.After},
			{Type.After}
		},
		{
			{Type.Before, Type.Meets, Type.Overlaps, Type.FinishedBy, Type.Contains},
			{Type.Overlaps, Type.FinishedBy, Type.Contains},
			{Type.Overlaps, Type.Starts, Type.During, Type.Finishes, Type.Equals, Type.FinishedBy, Type.Contains, Type.StartedBy, Type.OverlappedBy},
			{Type.Contains, Type.StartedBy, Type.OverlappedBy},
			{Type.Contains, Type.StartedBy, Type.OverlappedBy, Type.MetBy, Type.After},
			{Type.OverlappedBy, Type.MetBy, Type.After},
			{Type.OverlappedBy},
			{Type.During, Type.Finishes, Type.OverlappedBy},
			{Type.During, Type.Finishes, Type.OverlappedBy},
			{Type.OverlappedBy},
			{Type.OverlappedBy, Type.MetBy, Type.After},
			{Type.After},
			{Type.After}
		},
		{
			{Type.Before, Type.Meets, Type.Overlaps, Type.FinishedBy, Type.Contains},
			{Type.Starts, Type.Equals, Type.StartedBy},
			{Type.During, Type.Finishes, Type.OverlappedBy},
			{Type.MetBy},
			{Type.After},
			{Type.After},
			{Type.MetBy},
			{Type.During, Type.Finishes, Type.OverlappedBy},
			{Type.During, Type.Finishes, Type.OverlappedBy},
			{Type.MetBy},
			{Type.After},
			{Type.After},
			{Type.After}
		},
		{
			{Type.Before, Type.Meets, Type.Overlaps, Type.Starts, Type.During, Type.Finishes, Type.Equals, Type.FinishedBy, Type.Contains, Type.StartedBy, Type.OverlappedBy, Type.MetBy, Type.After},
			{Type.During, Type.Finishes, Type.OverlappedBy, Type.MetBy, Type.After},
			{Type.During, Type.Finishes, Type.OverlappedBy, Type.MetBy, Type.After},
			{Type.After},
			{Type.After},
			{Type.After},
			{Type.After},
			{Type.During, Type.Finishes, Type.OverlappedBy, Type.MetBy, Type.After},
			{Type.During, Type.Finishes, Type.OverlappedBy, Type.MetBy, Type.After},
			{Type.After},
			{Type.After},
			{Type.After},
			{Type.After}
		}
	};//3 by 3 composition Table
	
	public static Type[][] topologicalClosure = {

			{Type.Before, Type.Meets},
			{Type.Meets},
			{Type.Meets, Type.Overlaps, Type.FinishedBy, Type.Starts, Type.Equals},
			{Type.FinishedBy, Type.Equals},
			{Type.Contains, Type.StartedBy, Type.FinishedBy, Type.Equals},
			{Type.StartedBy,  Type.Equals},
			{Type.Equals},
			{Type.Starts, Type.Equals},
			{Type.During, Type.Starts, Type.Finishes, Type.Equals},
			{Type.Finishes, Type.Equals},
			{Type.MetBy, Type.OverlappedBy, Type.Finishes, Type.StartedBy, Type.Equals},
			{Type.MetBy},
			{Type.MetBy, Type.After}

	};
	

	/**
	 * Get the inverse relation of a given Allen relation.
	 * @param t The relation to invert.
	 * @return The inverse of relation <code>t</code>.
	 */
	public static Type getInverseRelation(Type t)
	{
		if (t.equals(Type.Before))
			return Type.After;
		if (t.equals(Type.Meets))
			return Type.MetBy;
		if (t.equals(Type.Overlaps))
			return Type.OverlappedBy;
		if (t.equals(Type.Finishes))
			return Type.FinishedBy;
		if (t.equals(Type.Starts))
			return Type.StartedBy;
		if (t.equals(Type.During))
			return Type.Contains;
		if (t.equals(Type.After))
			return Type.Before;
		if (t.equals(Type.MetBy))
			return Type.Meets;
		if (t.equals(Type.OverlappedBy))
			return Type.Overlaps;
		if (t.equals(Type.FinishedBy))
			return Type.Finishes;
		if (t.equals(Type.StartedBy))
			return Type.Starts;
		if (t.equals(Type.Contains))
			return Type.During;
		return Type.Equals;
	}
	
	/**
	 * Get the i-th relation.
	 * @param i Index of the desired relation.
	 * @return The i-th relation.
	 */
	public static Type lookupTypeByInt(int i) {
		return Type.values()[i];
	}
	


	@Override
	public boolean isEquivalent(Constraint c) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private static int getDimensionOfBasicRel(QualitativeAllenIntervalConstraint.Type t){
		if (t.equals(Type.Before) || t.equals(Type.After))
			return 2;
		if (t.equals(Type.Meets) || t.equals(Type.MetBy))
			return 1;
		if (t.equals(Type.Overlaps) || t.equals(Type.OverlappedBy))
			return 2;
		if (t.equals(Type.Finishes) || t.equals(Type.FinishedBy))
			return 1;
		if (t.equals(Type.Starts) || t.equals(Type.StartedBy))
			return 1;
		if (t.equals(Type.During) || t.equals(Type.Contains))
			return 2;		
		else 
			return 0;
	}
	
	/**
	 * Get the dimension of an Allen relation which is the maximum of basic Allen relations.
	 * @param types an array of basic Allen relations.
	 * @return The dimension of relation.
	 */
	public static int getDimension(QualitativeAllenIntervalConstraint.Type... types){
		if(types.length ==  0)
			return -1;
		Vector<Integer> dims = new Vector<Integer>(); 
		for (int i = 0; i < types.length; i++)
			dims.add(getDimensionOfBasicRel(types[i]));
		return Collections.max(dims);
	}
	
	
	private static Point getCanonicalAllenRepresentation(QualitativeAllenIntervalConstraint.Type t){
		if (t.equals(Type.Before))
			return new Point(0, 0);
		if (t.equals(Type.Meets))
			return new Point(0, 1);
		if (t.equals(Type.Overlaps))
			return new Point(0, 2);
		if (t.equals(Type.Finishes))
			return new Point(2, 3);
		if (t.equals(Type.Starts))
			return new Point(1, 2);
		if (t.equals(Type.During))
			return new Point(2, 2);
		if (t.equals(Type.After))
			return new Point(4, 4);
		if (t.equals(Type.MetBy))
			return new Point(3, 4);
		if (t.equals(Type.OverlappedBy))
			return new Point(2, 4);
		if (t.equals(Type.FinishedBy))
			return new Point(0, 3);
		if (t.equals(Type.StartedBy))
			return new Point(1, 4);
		if (t.equals(Type.Contains))
			return new Point(0, 4);
		return new Point(1, 3);
	}
	
	private static Type getAllenRelByCoordinate(int x, int y){
		if (x == 0 && y == 0)
			return Type.Before;
		if (x == 0 && y == 1)
			return Type.Meets;
		if (x == 0 && y == 2)
			return Type.Overlaps;
		if (x == 2 && y == 3)
			return Type.Finishes;
		if (x == 1 && y == 2)
			return Type.Starts;
		if (x == 2 && y == 2)
			return Type.During;
		if (x == 4 && y == 4)
			return Type.After;
		if (x == 3 && y == 4)
			return Type.MetBy;
		if (x == 2 && y == 4)
			return Type.OverlappedBy;
		if (x == 0 && y == 3)
			return Type.FinishedBy;
		if (x == 1 && y == 4)
			return Type.StartedBy;
		if (x == 0 && y == 4)
			return Type.Contains;
		if (x == 1 && y == 3)
			return Type.Equals;
		else
			return null;
	}
	
	/**
	 * Get convex closure of Allen Interval based on canonical representation of interval atomic relation[Ligozat, 1996]
	 * and based on the "Geometrical Interpretation of Maximal Tractable Interval Subalgebras" [F. Launay,D. Mitra, 06]  
	 * @param types an array of basic Allen relations.
	 * @return Convex closure of the Allen relations.
	 */
	public static QualitativeAllenIntervalConstraint.Type[] getAllenConvexClosure(QualitativeAllenIntervalConstraint.Type... types){
		
		int lowX, lowY, highX, highY;
		Vector<Integer> xdim = new Vector<Integer>();
		Vector<Integer> ydim = new Vector<Integer>();
		for (int i = 0; i < types.length; i++) {
			xdim.add(getCanonicalAllenRepresentation(types[i]).x);
			ydim.add(getCanonicalAllenRepresentation(types[i]).y);
		}
		
		highX = Collections.max(xdim);
		highY = Collections.max(ydim);
		lowX = Collections.min(xdim);
		lowY = Collections.min(ydim);
		
		Vector<QualitativeAllenIntervalConstraint.Type> convexRel = new Vector<QualitativeAllenIntervalConstraint.Type>();
		for (int i = lowX; i <= highX; i++) {
			for (int j = lowY; j <= highY; j++)
				if(getAllenRelByCoordinate(i, j) != null)
					convexRel.add(getAllenRelByCoordinate(i, j));
		}
		return convexRel.toArray(new QualitativeAllenIntervalConstraint.Type[convexRel.size()]);		
	}
	
	/**
	 * Define whether an Allen relation (disjunction of basic Allen relations) is pre-convex or not, R is a preconvex(weakly preconvex) relation if dim(I(R)\R) < dim(R)  
	 * @param types an array of basic Allen relations representing the disjunction we want to test.
	 * @return true iff the relation is preconvex (weakly preconvex).
	 */
	public boolean isPreconvex(QualitativeAllenIntervalConstraint.Type... types){
		
		QualitativeAllenIntervalConstraint.Type[] convexClosure = getAllenConvexClosure(types);
		Vector<QualitativeAllenIntervalConstraint.Type> convexvector = new Vector<QualitativeAllenIntervalConstraint.Type>();
		
		int counter = 0;
		for (int i = 0; i < convexClosure.length; i++){
			counter = 0;
			for (int j = 0; j < types.length; j++) {
				if(!convexClosure[i].equals(types[j]))
					counter++;
			}
			if(counter == types.length)
				convexvector.add(convexClosure[i]);
		}
		if(getDimension(convexvector.toArray(new QualitativeAllenIntervalConstraint.Type[convexvector.size()])) < getDimension(types))
			return true;
		return false;	
	}

}
