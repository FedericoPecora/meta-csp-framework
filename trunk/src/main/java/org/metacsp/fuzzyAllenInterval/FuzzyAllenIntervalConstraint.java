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
package org.metacsp.fuzzyAllenInterval;

import java.util.HashMap;

import org.metacsp.framework.Constraint;
import org.metacsp.time.qualitative.QualitativeAllenIntervalConstraint;

/**
 * This class represents fuzzy Allen Interval constraints.  Each constraint is a 
 * fuzzy set of the 13 Allen temporal relations, where each relation has an associated
 * possibility degree.  A {@link FuzzyAllenIntervalConstraint} is created by specifying
 * an initial possibility degree for a subset of the 13 relations.  The possibility degree
 * of all other relations is calculated according to Freksa's conceptual neighborhood (see
 * the DELTAS enum for alpha-cuts defining how possibility is assigned to neighbors). 
 * 
 * @author Masoumeh Mansouri
 *
 */
public class FuzzyAllenIntervalConstraint extends QualitativeAllenIntervalConstraint {
	
	private static final long serialVersionUID = 6199642316673769575L;

	/**
	 * Create a {@link FuzzyAllenIntervalConstraint} given an array of constraint types
	 * whose desired possibility is 1. 
	 * @param types
	 */
	public FuzzyAllenIntervalConstraint(Type... types) {
		super(types);
		//this.isSensoryRelation = false;
	}
	
	
	
	

	/**
	 * Get current possibilities of all Allen relations (types). 
	 * @return The current possibilities of all Allen relations.
	 */
	public HashMap<FuzzyAllenIntervalConstraint.Type, Double> getPossibilities() { 
		HashMap<FuzzyAllenIntervalConstraint.Type, Double> fr = new HashMap<FuzzyAllenIntervalConstraint.Type, Double>();
		for (Type t : Type.values()) fr.put(t, 0.0);
		for (Type type : types) {
			for(int t = 0; t <  FuzzyAllenIntervalConstraint.freksa_neighbor[type.ordinal()].length; t++)
				if (fr.get(FuzzyAllenIntervalConstraint.lookupTypeByInt(t)) != null) {
					fr.put(FuzzyAllenIntervalConstraint.lookupTypeByInt(t), Math.max(fr.get(FuzzyAllenIntervalConstraint.lookupTypeByInt(t)), FuzzyAllenIntervalConstraint.getPossibilityDegree(FuzzyAllenIntervalConstraint.freksa_neighbor[type.ordinal()][t])));
				}
				else {
					fr.put(FuzzyAllenIntervalConstraint.lookupTypeByInt(t), FuzzyAllenIntervalConstraint.getPossibilityDegree(FuzzyAllenIntervalConstraint.freksa_neighbor[type.ordinal()][t]));
				}
			}
		return fr;
	}
	
	public HashMap<Type, Double> makeCrispRel(){
		
		HashMap<FuzzyAllenIntervalConstraint.Type, Double> fr = new HashMap<FuzzyAllenIntervalConstraint.Type, Double>();
		for (Type t : Type.values()) fr.put(t, 0.0);
		for (Type type : types) {
			fr.put(type, 1.0);
		}
		return fr;
		
	}

	/**
	 * Get the possibilities of all inverse relations. 
	 * @return The possibilities of all inverse relations
	 */
	public HashMap<Type, Double> getInversePossibilities() {
		HashMap<Type, Double> ret = new HashMap<Type, Double>();
		for (Type t : Type.values()) ret.put(t, 0.0);
		for (Type t : types) {
			//??? IS THIS CORRECT ??? --> Seems correct
			//get inverse of each relation that is 1.0 
			Type inverseRelation = FuzzyAllenIntervalConstraint.getInverseRelation(t);
			
			HashMap<FuzzyAllenIntervalConstraint.Type, Double> possibilities = this.getPossibilities();
			
			//set poss of each inverse relation to 1.0
			ret.put(inverseRelation, possibilities.get(t));
			
			//calculate the Freksa N of each inverse relation
			HashMap<FuzzyAllenIntervalConstraint.Type, Double> fr = new HashMap<FuzzyAllenIntervalConstraint.Type, Double>();
			for(int i = 0; i <  FuzzyAllenIntervalConstraint.freksa_neighbor[inverseRelation.ordinal()].length; i++)
				fr.put(FuzzyAllenIntervalConstraint.lookupTypeByInt(i), FuzzyAllenIntervalConstraint.getPossibilityDegree(FuzzyAllenIntervalConstraint.freksa_neighbor[inverseRelation.ordinal()][i]));
			
			//take the maximum between calculated Freksa N and previously added possibilities
			//(because this is an OR)
			for(FuzzyAllenIntervalConstraint.Type t1: fr.keySet())
				ret.put(t1, Math.max(ret.get(t1), fr.get(t1)));
		}
		
		/*
		System.out.println("=====================================");
		System.out.println("DIRECT " + this + ":\n" + this.possibilities);
		System.out.println("INVERSE:\n" + ret);
		System.out.println("=====================================");
		*/
		
		return ret;
	}
	
	public HashMap<Type, Double> getCrispInverse() {
		HashMap<Type, Double> ret = new HashMap<Type, Double>();
		for (Type t : Type.values()) ret.put(t, 0.0);
		for (Type t : types) {
			//??? IS THIS CORRECT ??? --> Seems correct
			//get inverse of each relation that is 1.0 
			Type inverseRelation = FuzzyAllenIntervalConstraint.getInverseRelation(t);

			HashMap<FuzzyAllenIntervalConstraint.Type, Double> possibilities = this.getPossibilities();
			
			//set poss of each inverse relation to 1.0
			ret.put(inverseRelation, possibilities.get(t));
			
		}
		/*
		System.out.println("=====================================");
		System.out.println("DIRECT " + this + ":\n" + this.possibilities);
		System.out.println("INVERSE:\n" + ret);
		System.out.println("=====================================");
		*/
		
		return ret;
	}
	
	
	
	@Override
	public Object clone() {
		FuzzyAllenIntervalConstraint ret = new FuzzyAllenIntervalConstraint(); 
		ret.setTypes(types);
		return ret;
	}
	
	
/*	public static int[][] freksa_neighbor = { //B_neighbor

//Before,Meets,Overlaps,FinishedBy,Contains,StartedBy,Equals,Starts,During,Finishes,OverlappedBy,
//MetBy,After,
		
		{0, 1, 2, 3, 4, 5, 3, 3, 4, 5, 2, 5, 6},//Before
		{1, 0, 1, 2, 3, 3, 2, 2, 3, 3, 3, 4, 5},//Meets
		{2, 1, 0, 1, 2, 3, 1, 1, 2, 3, 2, 3, 4},//Overlaps
		{3, 2, 1, 0, 1, 2, 2, 2, 3, 4, 3, 4, 5},//FinishedBy
		{4, 3, 2, 1, 0, 1, 1, 3, 2, 3, 2, 3, 4},//Contains
		{5, 4, 3, 2, 1, 0, 2, 4, 3, 2, 1, 2, 3},//StartedBy
		{3, 2, 1, 2, 3, 2, 0, 2, 3, 2, 1, 2, 3},//Equals
		{3, 2, 1, 2, 3, 4, 2, 0, 1, 2, 3, 4, 5},//Starts
		{4, 3, 2, 3, 2, 3, 1, 1, 0, 1, 2, 3, 4},//During
		{5, 4, 3, 4, 3, 2, 2, 2, 1, 0, 1, 2, 3},//Finishes
		{4, 3, 2, 3, 2, 1, 1, 3, 2, 1, 0, 1, 2},//OverlappedBy
		{5, 4, 3, 3, 3, 2, 2, 3, 3, 2, 1, 0, 1},//MetBy
		{6, 5, 4, 5, 4, 3, 3, 5, 4, 3, 2, 1, 0},//After
		
	};//B-neighbor
*/
	/**
	 * The transition table used for computing Freksa neighborhoods.
	 */
	public static int[][] freksa_neighbor = { //B_neighbor

		//Before,Meets,Overlaps,FinishedBy,Contains,StartedBy,Equals,Starts,During,Finishes,OverlappedBy,
		//MetBy,After,
				
				{0, 1, 2, 3, 4, 5, 6, 3, 4, 5, 6, 7, 8},//Before
				{1, 0, 1, 2, 3, 4, 4, 2, 3, 4, 5, 6, 7},//Meets
				{2, 1, 0, 1, 2, 3, 4, 1, 2, 3, 4, 5, 6},//Overlaps
				{3, 2, 1, 0, 1, 2, 2, 2, 3, 4, 3, 4, 5},//FinishedBy
				{4, 3, 2, 1, 0, 1, 1, 3, 2, 3, 2, 3, 4},//Contains
				{5, 4, 3, 2, 1, 0, 2, 4, 3, 2, 1, 2, 3},//StartedBy
				{5, 4, 3, 2, 1, 2, 0, 2, 1, 2, 3, 4, 5},//Equals
				{3, 2, 1, 2, 3, 4, 2, 0, 1, 2, 3, 4, 5},//Starts
				{4, 3, 2, 3, 2, 3, 1, 1, 0, 1, 2, 3, 4},//During
				{5, 4, 3, 4, 3, 2, 2, 2, 1, 0, 1, 2, 3},//Finishes
				{6, 5, 4, 3, 2, 1, 3, 3, 2, 1, 0, 1, 2},//OverlappedBy
				{7, 6, 5, 4, 3, 2, 4, 4, 3, 2, 1, 0, 1},//MetBy
				{8, 7, 6, 5, 4, 3, 5, 5, 4, 3, 2, 1, 0},//After
				
			};//iran

	
	
	/**
	 * Get the possibility degree of relations at a given distance (in Freksa's neighborhood).
	 * @param distance The distance at which to get the possibility degree.
	 * @return The possibility degree of relations at the given distance (in Freksa's neighborhood).
	 */
	public static double getPossibilityDegree(int distance) {
		return DELTAS[distance];
	}
	
	/**
	 * Array defining how possibility decreases with distance in Freksa's neighborhood.  
	 */
	public static double[] DELTAS = {1.0, 0.8, 0.6, 0.4, 0.2, 0.1, 0.0, 0.0, 0.0};

	@Override
	public boolean isEquivalent(Constraint c) {
		FuzzyAllenIntervalConstraint fc = (FuzzyAllenIntervalConstraint)c;
		if (!(fc.getFrom().equals(this.getFrom()) && fc.getTo().equals(this.getTo()))) return false;
		for (Type t : this.getTypes()) {
			boolean found = false;
			for (Type t1 : fc.getTypes()) {
				if (t.equals(t1)) {
					found = true;
					break;
				}
				if (!found) return false;
			}
		}
		return true;
	}


}
