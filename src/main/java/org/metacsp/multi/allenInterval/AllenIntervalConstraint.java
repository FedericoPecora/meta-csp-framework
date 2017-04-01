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
package org.metacsp.multi.allenInterval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiBinaryConstraint;
import org.metacsp.throwables.time.MalformedBoundsException;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.time.SimpleDistanceConstraint;
import org.metacsp.time.TimePoint;

/**
 * This class provides an implementation of Allen's Interval Algebra (see Allen, 1984).  It includes the 13 basic qualitative
 * relations, as well as several tractable disjunctions.  Relations can also be provided with metric bounds - see
 * {@link Type} enum for details.
 * 
 * @author Federico Pecora
 *
 */
public class AllenIntervalConstraint extends MultiBinaryConstraint {

	private static final long serialVersionUID = -4010342193923812891L;

	/**
	 * The 13 basic qualitative relations in Allen's Interval Algebra, plus several tractable disjunctions.  Metric bounds
	 * can be specified on basic relations.
	 * 
	 * @author Federico Pecora
	 */
	public static enum Type {
		//Before,Meets,Overlaps,FinishedBy,Contains,StartedBy,Equals,Starts,During,Finishes,OverlappedBy,
		//MetBy,After,
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A BEFORE [l,u] B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../../../img/aaia-p.png alt=""> 
		 */
		Before(1L, APSPSolver.INF),
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A MEETS B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../../../img/aaia-m.png alt=""> 
		 */
		Meets(0),
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A OVERLAPS [l,u][l,u][l,u] B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../../../img/aaia-o.png alt=""> 
		 */
		Overlaps(1L, APSPSolver.INF, 1L, APSPSolver.INF, 1L, APSPSolver.INF),
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A FINISHED-BY [l,u] B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../../../img/aaia-fi.png alt=""> 
		 */
		FinishedBy(1L, APSPSolver.INF),
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A CONTAINS [l,u][l,u] B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../../../img/aaia-di.png alt=""> 
		 */
		Contains(1L, APSPSolver.INF,   1L, APSPSolver.INF),
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A STARTED-BY [l,u] B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../../../img/aaia-si.png alt=""> 
		 */
		StartedBy(1L, APSPSolver.INF),
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A EQUALS B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../../../img/aaia-e.png alt=""> 
		 */
		Equals(0),
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A STARTS [l,u] B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../../../img/aaia-s.png alt=""> 
		 */
		Starts(1L, APSPSolver.INF),
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A DURING [l,u][l,u] B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../../../img/aaia-d.png alt=""> 
		 */
		During(1L, APSPSolver.INF,   1L, APSPSolver.INF),
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A FINISHES [l,u] B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../../../img/aaia-f.png alt=""> 
		 */
		Finishes(1L, APSPSolver.INF),
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A OVERLAPPED-BY [l,u][l,u][l,u] B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../../../img/aaia-oi.png alt=""> 
		 */
		OverlappedBy(1L, APSPSolver.INF, 1L, APSPSolver.INF, 1L, APSPSolver.INF),
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A AFTER [l,u] B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../../../img/aaia-pi.png alt=""> 
		 */
		After(1L, APSPSolver.INF),
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A MET-BY B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../../../img/aaia-mi.png alt=""> 
		 */
		MetBy(0),
		
		Release(0L, APSPSolver.INF),
		Deadline(0L, APSPSolver.INF),
		
		BeforeOrMeets(0L, APSPSolver.INF), 
		MetByOrAfter(0L, APSPSolver.INF),
		MetByOrOverlappedBy(0L, APSPSolver.INF),
		MetByOrOverlappedByOrAfter(0),
		MetByOrOverlappedByOrIsFinishedByOrDuring(0L, APSPSolver.INF),
		MeetsOrOverlapsOrBefore(0),
		DuringOrEquals(0L, APSPSolver.INF,   0L, APSPSolver.INF),
		DuringOrEqualsOrStartsOrFinishes(0L, APSPSolver.INF,   0L, APSPSolver.INF),
		MeetsOrOverlapsOrFinishedByOrContains(0L, APSPSolver.INF),		// PDDL at start
		ContainsOrStartedByOrOverlappedByOrMetBy(0L, APSPSolver.INF),		// PDDL at end
		EndsDuring(0),
		@Deprecated
		EndEnd(0L, APSPSolver.INF),
		EndsOrEndedBy(0L, APSPSolver.INF),
		At(0L, APSPSolver.INF, 0L, APSPSolver.INF),
		@Deprecated
		StartStart(0L, APSPSolver.INF),
		StartsOrStartedBy(0L, APSPSolver.INF),
		Duration(0L, APSPSolver.INF),		
		Forever(0),
		NotBeforeAndNotAfter(0),
		DisjunctionRelation;
		
		private static Bounds[] createDefaultBounds(Long... bounds) {
			assert bounds.length % 2 == 0;
			
			final Bounds[] intervals = new Bounds[bounds.length/2];
			
			for(int i = 0; i < bounds.length; i+=2) {
				intervals[i/2] = new Bounds(bounds[i], bounds[i+1]);
			}
			return intervals;
		}
		
		protected final Bounds[] defaultIntervalBounds;
		public final int numParams;
		
		
		private Type(Long... bounds) {
			defaultIntervalBounds = createDefaultBounds(bounds);
			this.numParams = defaultIntervalBounds.length;
		}
		
		private Type(int numParams) {
			defaultIntervalBounds = (numParams == 0) ? new Bounds[]{} : null;
			this.numParams = numParams;
		}
		
		public Bounds[] getDefaultBounds() {
			if(defaultIntervalBounds == null) {
				throw new Error("No default bounds for " + this);
			} else {
				return defaultIntervalBounds;
			}
		}
		
		/**
		 * Returns an {@link AllenIntervalConstraint.Type} given its name.
		 * @param name the name of the constraint (non case-sensitive).
		 * @return The corresponding constraint or null if the given constraint name does not exist. 
		 */
		public static AllenIntervalConstraint.Type fromString(String name) {
			
			for (Type enumConstant : AllenIntervalConstraint.Type.values()) {
				if(enumConstant.toString().equalsIgnoreCase(name)) {
					return enumConstant;
				}
			}
			return null;
			/*
			try {
				return Enum.valueOf(Type.class, name);
			} catch (IllegalArgumentException ex) {
				return null;
			}
			*/
		}
	};					

	
	//protected Type type;
	protected Bounds[] bounds;
	protected Type[] types;
	
//	@Deprecated
//	public AllenIntervalConstraint(Type type, Interval first, Interval... remainder) {		
//		this.type = type;
//		Interval[] intervals = Arrays.asList(first, remainder).toArray(new Interval[remainder.length + 1]);
//		bounds = new Bounds[intervals.length];
//		for(int i = 0; i < bounds.length; ++i) {
//			bounds[i] = new Bounds(intervals[i].getLowerBound(), intervals[i].getUpperBound());
//		}
//		
//		if(type.numParams >= 0 && type.numParams != bounds.length) {
//			throw new IllegalArgumentException("Invalid numer of parameters for constraint " + type + ", expected: " + type.numParams);
//		}
//	}
	

	/**
	 * Convenience constructor for not casting bounds into an array (see {@link AllenIntervalConstraint#AllenIntervalConstraint(Type, Bounds[])} constructor).
	 * @param type The type of the constraint.
	 * @param bounds The one and only upper-lower bound pair for the constraint.
	 */
	public AllenIntervalConstraint(Type type, Bounds bounds) {
		this(type, new Bounds[] {bounds});
	}

	/**
	 * Convenience constructor for not casting bounds into an array (see {@link AllenIntervalConstraint#AllenIntervalConstraint(Type, Bounds[])} constructor).
	 * @param type The type of the constraint.
	 * @param bounds1 The first upper-lower bound pair for the constraint.
	 * @param bounds2 The second upper-lower bound pair for the constraint.
	 */
	public AllenIntervalConstraint(Type type, Bounds bounds1, Bounds bounds2) {
		this(type, new Bounds[] {bounds1, bounds2});
	}

	/**
	 * Convenience constructor for not casting bounds into an array (see {@link AllenIntervalConstraint#AllenIntervalConstraint(Type, Bounds[])} constructor).
	 * @param type The type of the constraint.
	 * @param bounds1 The first upper-lower bound pair for the constraint.
	 * @param bounds2 The second upper-lower bound pair for the constraint.
	 * @param bounds3 The third upper-lower bound pair for the constraint.
	 */
	public AllenIntervalConstraint(Type type, Bounds bounds1, Bounds bounds2, Bounds bounds3) {
		this(type, new Bounds[] {bounds1, bounds2, bounds3});
	}
	
	/**
	 * Convenience constructor for not casting bounds into an array (see {@link AllenIntervalConstraint#AllenIntervalConstraint(Type, Bounds[])} constructor).
	 * @param type The type of the constraint.
	 * @param bounds1 The first upper-lower bound pair for the constraint.
	 * @param bounds2 The second upper-lower bound pair for the constraint.
	 * @param bounds3 The third upper-lower bound pair for the constraint.
	 * @param bounds4 The fourth upper-lower bound pair for the constraint.
	 */
	public AllenIntervalConstraint(Type type, Bounds bounds1, Bounds bounds2, Bounds bounds3, Bounds bounds4) {
		this(type, new Bounds[] {bounds1, bounds2, bounds3, bounds4});
	}
	
	/**
	 * Creates an elementary {@link AllenIntervalConstraint} of a given type with given bounds.
	 * @param type The type of the constraint.
	 * @param bounds The bounds of the constraint.
	 */
	public AllenIntervalConstraint(Type type, Bounds[] bounds) {
		//this.type = type;
		this.types = new Type[] {type};
		this.bounds = bounds;
		if(type.numParams >= 0 && type.numParams != bounds.length) {
			throw new IllegalArgumentException("Invalid numer of parameters for constraint " + type + ", expected: " + type.numParams + " got "+ bounds.length );
		}
	}
	
	/**
	 * Creates an {@link AllenIntervalConstraint} of a given (disjunctive) type with default bounds.
	 * @param types The elementary Allen relations that constitute this constraint.
	 */
	public AllenIntervalConstraint(Type ... types) {
		
		if(types.length == 1){
			//this.type = types[0];
			this.types = types;
			this.bounds = types[0].getDefaultBounds();
		}
		else{
			//it assumed that creating convexity is done one step before, in other words, where it calls this constructor
			this.types = types;
			//this.type = Type.DisjunctionRelation;
		
			this.bounds = new Bounds[4];
			
			Bounds[] fs_ts = new Bounds[types.length];
			Bounds[] fs_te = new Bounds[types.length];
			Bounds[] fe_ts = new Bounds[types.length];
			Bounds[] fe_te = new Bounds[types.length];
			for (int i = 0; i < types.length; i++) {
				fs_ts[i] = getQuantitativeTranslationOfAllen(types[i])[0];
				fs_te[i] = getQuantitativeTranslationOfAllen(types[i])[1];
				fe_ts[i] = getQuantitativeTranslationOfAllen(types[i])[2];
				fe_te[i] = getQuantitativeTranslationOfAllen(types[i])[3];
			}
			this.bounds[0] = Bounds.union(fs_ts); 
			this.bounds[1] = Bounds.union(fs_te);
			this.bounds[2] = Bounds.union(fe_ts);
			this.bounds[3] = Bounds.union(fe_te);
		}

	}

//	private Bounds[] getUnionBound(){
//		
//		return null;
//	}
	

	/**
	 * Get the type of this {@link AllenIntervalConstraint}.
	 * @return The type of this {@link AllenIntervalConstraint}.
	 */
	@Deprecated
	public Type getType() { 
		//return type;
		return types[0];
	}

	/**
	 * Get the type of this {@link AllenIntervalConstraint}.
	 * @return The type of this {@link AllenIntervalConstraint}.
	 */
	public Type[] getTypes() { 
		//return type;
		return types;
	}
	
	/**
	 * Get the bounds for this {@link AllenIntervalConstraint}.
	 * @return The bounds for this {@link AllenIntervalConstraint}.
	 */
	public Bounds[] getBounds() { return this.bounds; }
	
	@Override
	protected Constraint[] createInternalConstraints(Variable f, Variable t) {
		if (f instanceof AllenInterval && t instanceof AllenInterval) {
			AllenInterval from = (AllenInterval)f;
			AllenInterval to = (AllenInterval)t;
			
			/*
			 * The quantitative constraint between two bounds in the translation of R is the union of quantitative constraints between 
			 * these two bounds which are in the translations of the atomic relations forming R
			 */
			//if(type.equals(Type.DisjunctionRelation)){
			if(this.types.length > 1){
				
				TimePoint fs = from.getStart();
				TimePoint ts = to.getStart();
				TimePoint fe = from.getEnd();
				TimePoint te = to.getEnd();
				
				
				//SimpleDistanceConstraint[] simDisCons = new SimpleDistanceConstraint[4]; //fs - ts
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				SimpleDistanceConstraint second = new SimpleDistanceConstraint();//fs te
				SimpleDistanceConstraint third = new SimpleDistanceConstraint();//fe ts
				SimpleDistanceConstraint fourth = new SimpleDistanceConstraint();//fe te
				if(bounds[0].min == -APSPSolver.INF && bounds[0].max == APSPSolver.INF)
					first = null;
				else if(bounds[0].min == -APSPSolver.INF){
					first.setMinimum(0);
					first.setMaximum(APSPSolver.INF);
					first.setFrom(ts);
					first.setTo(fs);

				}
				else{
					first.setMinimum(bounds[0].min);
					first.setMaximum(bounds[0].max);
					first.setFrom(fs);
					first.setTo(ts);
				}

				if(bounds[1].min == -APSPSolver.INF && bounds[1].max == APSPSolver.INF)
					second = null;
				else if(bounds[1].min == -APSPSolver.INF){
					second.setMinimum(0);
					second.setMaximum(APSPSolver.INF);
					second.setFrom(te);
					second.setTo(fs);

				}
				else{
					second.setMinimum(bounds[1].min);
					second.setMaximum(bounds[1].max);
					second.setFrom(fs);
					second.setTo(te);
				}

				if(bounds[2].min == -APSPSolver.INF && bounds[2].max == APSPSolver.INF)
					third = null;
				else if(bounds[2].min == -APSPSolver.INF){
					third.setMinimum(0);
					third.setMaximum(APSPSolver.INF);
					third.setFrom(ts);
					third.setTo(fe);
				}
				else{
					third.setMinimum(bounds[2].min);
					third.setMaximum(bounds[2].max);
					third.setFrom(fe);
					third.setTo(ts);
				}
				
				if(bounds[3].min == -APSPSolver.INF && bounds[3].max == APSPSolver.INF)
					fourth = null;
				else if(bounds[3].min == -APSPSolver.INF){
					fourth.setMinimum(0);
					fourth.setMaximum(APSPSolver.INF);
					fourth.setFrom(te);
					fourth.setTo(fe);
				}
				else{
					fourth.setMinimum(bounds[3].min);
					fourth.setMaximum(bounds[3].max);
					fourth.setFrom(fe);
					fourth.setTo(te);
				}
				
				Vector<SimpleDistanceConstraint> sdc = new Vector<SimpleDistanceConstraint>();
//				for (int i = 0; i < simDisCons.length; i++) {
//					if(simDisCons[i] != null)
//						sdc.add(simDisCons[i]);
//				}
				if(first != null)
					sdc.add(first);
				if(second != null)
					sdc.add(second);
				if(third != null)
					sdc.add(third);
				if(fourth != null)
					sdc.add(fourth);
				
				Constraint[] ret = sdc.toArray(new SimpleDistanceConstraint[sdc.size()]);
				return ret;
			}
			
			if (types[0].equals(Type.Equals)) {
				TimePoint fs = from.getStart();
				TimePoint ts = to.getStart();
				TimePoint fe = from.getEnd();
				TimePoint te = to.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				SimpleDistanceConstraint second = new SimpleDistanceConstraint();
				first.setMinimum(0);
				first.setMaximum(0);
				first.setFrom(fs);
				first.setTo(ts);
				second.setMinimum(0);
				second.setMaximum(0);
				second.setFrom(te);
				second.setTo(fe);
				Constraint[] ret = {first,second};
				return ret;
			}
			
			if (types[0].equals(Type.Before)) {
				if (bounds[0].min == 0) throw new MalformedBoundsException(Type.Before, bounds[0]);
				TimePoint ts = to.getStart();
				TimePoint fe = from.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				first.setMinimum(bounds[0].min);
				first.setMaximum(bounds[0].max);
				first.setFrom(fe);
				first.setTo(ts);
				Constraint[] ret = {first};
				return ret;
			}
			
			if (types[0].equals(Type.After)) {
				if (bounds[0].min == 0) throw new MalformedBoundsException(Type.After, bounds[0]);
				TimePoint fs = from.getStart();
				TimePoint te = to.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				first.setMinimum(bounds[0].min);
				first.setMaximum(bounds[0].max);
				first.setFrom(te);
				first.setTo(fs);
				Constraint[] ret = {first};
				return ret;
			}
			
			if (types[0].equals(Type.Meets)) {
				TimePoint ts = to.getStart();
				TimePoint fe = from.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				first.setMinimum(0);
				first.setMaximum(0);
				first.setFrom(fe);
				first.setTo(ts);
				Constraint[] ret = {first};
				return ret;
			}
			
			if (types[0].equals(Type.MetBy)) {
				TimePoint fs = from.getStart();
				TimePoint te = to.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				first.setMinimum(0);
				first.setMaximum(0);
				first.setFrom(te);
				first.setTo(fs);
				Constraint[] ret = {first};
				return ret;
			}
			
			if (types[0].equals(Type.Starts)) {
				if (bounds[0].min == 0) throw new MalformedBoundsException(Type.Starts, bounds[0]);
				TimePoint fs = from.getStart();
				TimePoint ts = to.getStart();
				TimePoint fe = from.getEnd();
				TimePoint te = to.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				SimpleDistanceConstraint second = new SimpleDistanceConstraint();
				first.setMinimum(0);
				first.setMaximum(0);
				first.setFrom(fs);
				first.setTo(ts);
				second.setMinimum(bounds[0].min);
				second.setMaximum(bounds[0].max);
				second.setFrom(fe);
				second.setTo(te);
				Constraint[] ret = {first,second};
				return ret;
			}
			
			if (types[0].equals(Type.StartedBy)) {
				if (bounds[0].min == 0) throw new MalformedBoundsException(Type.StartedBy, bounds[0]);
				TimePoint fs = from.getStart();
				TimePoint ts = to.getStart();
				TimePoint fe = from.getEnd();
				TimePoint te = to.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				SimpleDistanceConstraint second = new SimpleDistanceConstraint();
				first.setMinimum(0);
				first.setMaximum(0);
				first.setFrom(fs);
				first.setTo(ts);
				second.setMinimum(bounds[0].min);
				second.setMaximum(bounds[0].max);
				second.setFrom(te);
				second.setTo(fe);
				Constraint[] ret = {first,second};
				return ret;
			}
			
			if (types[0].equals(Type.During)) {
				if (bounds[0].min == 0) throw new MalformedBoundsException(Type.During, bounds[0]);
				if (bounds[1].min == 0) throw new MalformedBoundsException(Type.During, bounds[1]);
				TimePoint fs = from.getStart();
				TimePoint ts = to.getStart();
				TimePoint fe = from.getEnd();
				TimePoint te = to.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				SimpleDistanceConstraint second = new SimpleDistanceConstraint();
				first.setMinimum(bounds[0].min);
				first.setMaximum(bounds[0].max);
				first.setFrom(ts);
				first.setTo(fs);
				second.setMinimum(bounds[1].min);
				second.setMaximum(bounds[1].max);
				second.setFrom(fe);
				second.setTo(te);
				Constraint[] ret = {first,second};
				return ret;
			}
			
			if (types[0].equals(Type.Contains)) {
				if (bounds[0].min == 0) throw new MalformedBoundsException(Type.Contains, bounds[0]);
				if (bounds[1].min == 0) throw new MalformedBoundsException(Type.Contains, bounds[1]);
				TimePoint fs = from.getStart();
				TimePoint ts = to.getStart();
				TimePoint fe = from.getEnd();
				TimePoint te = to.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				SimpleDistanceConstraint second = new SimpleDistanceConstraint();
				first.setMinimum(bounds[0].min);
				first.setMaximum(bounds[0].max);
				first.setFrom(fs);
				first.setTo(ts);
				second.setMinimum(bounds[1].min);
				second.setMaximum(bounds[1].max);
				second.setFrom(te);
				second.setTo(fe);
				Constraint[] ret = {first,second};
				return ret;
			}
			
			if (types[0].equals(Type.Finishes)) {
				if (bounds[0].min == 0) throw new MalformedBoundsException(Type.Finishes, bounds[0]);
				TimePoint fs = from.getStart();
				TimePoint ts = to.getStart();
				TimePoint fe = from.getEnd();
				TimePoint te = to.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				SimpleDistanceConstraint second = new SimpleDistanceConstraint();
				first.setMinimum(bounds[0].min); //1
				first.setMaximum(bounds[0].max); //INF
				first.setFrom(ts);    
				first.setTo(fs);      
				second.setMinimum(0); //0
				second.setMaximum(0); //0
				second.setFrom(fe);
				second.setTo(te);
				Constraint[] ret = {first,second};
				return ret;
			}
			
			if (types[0].equals(Type.FinishedBy)) {
				if (bounds[0].min == 0) throw new MalformedBoundsException(Type.FinishedBy, bounds[0]);
				TimePoint fs = from.getStart();
				TimePoint ts = to.getStart();
				TimePoint fe = from.getEnd();
				TimePoint te = to.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				SimpleDistanceConstraint second = new SimpleDistanceConstraint();
				first.setMinimum(bounds[0].min);
				first.setMaximum(bounds[0].max);
				first.setFrom(fs);
				first.setTo(ts);
				second.setMinimum(0);
				second.setMaximum(0);
				second.setFrom(fe);
				second.setTo(te);
				Constraint[] ret = {first,second};
				return ret;
			}
			
			if (types[0].equals(Type.Overlaps)) {
				if (bounds[0].min == 0) throw new MalformedBoundsException(Type.Overlaps, bounds[0]);
				if (bounds[1].min == 0) throw new MalformedBoundsException(Type.Overlaps, bounds[1]);
				if (bounds[2].min == 0) throw new MalformedBoundsException(Type.Overlaps, bounds[2]);
				TimePoint fs = from.getStart();
				TimePoint ts = to.getStart();
				TimePoint fe = from.getEnd();
				TimePoint te = to.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				SimpleDistanceConstraint second = new SimpleDistanceConstraint();
				SimpleDistanceConstraint third = new SimpleDistanceConstraint();
				first.setMinimum(bounds[0].min);
				first.setMaximum(bounds[0].max);
				first.setFrom(fs);
				first.setTo(ts);
				second.setMinimum(bounds[1].min);
				second.setMaximum(bounds[1].max);
				second.setFrom(ts);
				second.setTo(fe);
				third.setMinimum(bounds[2].min);
				third.setMaximum(bounds[2].max);
				third.setFrom(fe);
				third.setTo(te);
				Constraint[] ret = {first,second,third};
				return ret;
			}
			
		
			if (types[0].equals(Type.OverlappedBy)) {
				if (bounds[0].min == 0) throw new MalformedBoundsException(Type.Overlaps, bounds[0]);
				if (bounds[1].min == 0) throw new MalformedBoundsException(Type.Overlaps, bounds[1]);
				if (bounds[2].min == 0) throw new MalformedBoundsException(Type.Overlaps, bounds[2]);
				TimePoint fs = from.getStart();
				TimePoint ts = to.getStart();
				TimePoint fe = from.getEnd();
				TimePoint te = to.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				SimpleDistanceConstraint second = new SimpleDistanceConstraint();
				SimpleDistanceConstraint third = new SimpleDistanceConstraint();
				first.setMinimum(bounds[0].min);
				first.setMaximum(bounds[0].max);
				first.setFrom(ts);
				first.setTo(fs);
				second.setMinimum(bounds[1].min);
				second.setMaximum(bounds[1].max);
				second.setFrom(fs);
				second.setTo(te);
				third.setMinimum(bounds[2].min);
				third.setMaximum(bounds[2].max);
				third.setFrom(te);
				third.setTo(fe);
				Constraint[] ret = {first,second,third};
				return ret;
			}
			
			if (types[0].equals(Type.At)) {
				TimePoint fs = from.getStart();
				TimePoint fe = from.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				SimpleDistanceConstraint second = new SimpleDistanceConstraint();
				//SimpleDistanceConstraint third = new SimpleDistanceConstraint();
				first.setMinimum(bounds[0].min-((APSPSolver)from.getInternalConstraintSolvers()[0]).getO());
				first.setMaximum(bounds[0].max-((APSPSolver)from.getInternalConstraintSolvers()[0]).getO());
				second.setMinimum(bounds[1].min-((APSPSolver)from.getInternalConstraintSolvers()[0]).getO());
				second.setMaximum(bounds[1].max-((APSPSolver)from.getInternalConstraintSolvers()[0]).getO());
				//third.setMinimum(bounds[2].start);
				//third.setMaximum(bounds[2].stop);
				first.setFrom(((APSPSolver)from.getInternalConstraintSolvers()[0]).getSource());
				first.setTo(fs);
				second.setFrom(((APSPSolver)from.getInternalConstraintSolvers()[0]).getSource());
				second.setTo(fe);
				//third.setFrom(fs);
				//third.setTo(fe);
				Constraint[] ret = {first,second};
				return ret;
			}
						
			if (types[0].equals(Type.Duration)) {
				TimePoint fs = from.getStart();
				TimePoint fe = from.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				first.setMinimum(bounds[0].min);
				first.setMaximum(bounds[0].max);
				first.setFrom(fs);
				first.setTo(fe);
				Constraint[] ret = {first};
				return ret;
			}
			
			if (types[0].equals(Type.Release)) {
				
				TimePoint fs = from.getStart();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				if (bounds[0].max == APSPSolver.INF) bounds[0].max = ((APSPSolver)from.getInternalConstraintSolvers()[0]).getH();
				first.setMinimum(bounds[0].min-((APSPSolver)from.getInternalConstraintSolvers()[0]).getO());
				first.setMaximum(bounds[0].max-((APSPSolver)from.getInternalConstraintSolvers()[0]).getO());
				first.setFrom(((APSPSolver)from.getInternalConstraintSolvers()[0]).getSource());
				first.setTo(fs);
				Constraint[] ret = {first};
				return ret;	
			}
			
			if (types[0].equals(Type.Deadline)) {
				TimePoint fe = from.getEnd();
				if (bounds[0].max == APSPSolver.INF) bounds[0].max = ((APSPSolver)from.getInternalConstraintSolvers()[0]).getH();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				first.setMinimum(bounds[0].min-((APSPSolver)from.getInternalConstraintSolvers()[0]).getO());
				first.setMaximum(bounds[0].max-((APSPSolver)from.getInternalConstraintSolvers()[0]).getO());
				first.setFrom(((APSPSolver)from.getInternalConstraintSolvers()[0]).getSource());
				first.setTo(fe);
				Constraint[] ret = {first};
				return ret;	
			}

			if (types[0].equals(Type.Forever)) {
				TimePoint fe = from.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				first.setMinimum(0);
				first.setMaximum(0);
				first.setFrom(fe);
				first.setTo(((APSPSolver)from.getInternalConstraintSolvers()[0]).getSink());
				Constraint[] ret = {first};
				return ret;	
			}

			if (types[0].equals(Type.BeforeOrMeets)) {
				TimePoint ts = to.getStart();
				TimePoint fe = from.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				first.setMinimum(bounds[0].min);
				first.setMaximum(bounds[0].max);
				first.setFrom(fe);
				first.setTo(ts);
				Constraint[] ret = {first};
				return ret;
			}
			
			
			if (types[0].equals(Type.MetByOrAfter)) {
				TimePoint fs = from.getStart();
				TimePoint te = to.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				first.setMinimum(bounds[0].min);
				first.setMaximum(bounds[0].max);
				first.setFrom(te);
				first.setTo(fs);
				Constraint[] ret = {first};
				return ret;
			}
			
			if (types[0].equals(Type.MetByOrOverlappedBy)) {
				TimePoint fs = from.getStart();
				TimePoint ts = to.getStart();
				TimePoint fe = from.getEnd();
				TimePoint te = to.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				SimpleDistanceConstraint second = new SimpleDistanceConstraint();
				SimpleDistanceConstraint third = new SimpleDistanceConstraint();
				first.setMinimum(1);
				first.setMaximum(APSPSolver.INF);
				first.setFrom(ts);
				first.setTo(fs);
				second.setMinimum(bounds[0].min);
				second.setMaximum(bounds[0].max);
				second.setFrom(fs);
				second.setTo(te);
				third.setMinimum(1);
				third.setMaximum(APSPSolver.INF);
				third.setFrom(te);
				third.setTo(fe);
				Constraint[] ret = {first,second,third};
				return ret;
			}

			if (types[0].equals(Type.MetByOrOverlappedByOrAfter)) {
				TimePoint fs = from.getStart();
				TimePoint ts = to.getStart();
				TimePoint fe = from.getEnd();
				TimePoint te = to.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				SimpleDistanceConstraint second = new SimpleDistanceConstraint();
				first.setMinimum(1);
				first.setMaximum(APSPSolver.INF);
				first.setFrom(ts);
				first.setTo(fs);
				second.setMinimum(1);
				second.setMaximum(APSPSolver.INF);
				second.setFrom(te);
				second.setTo(fe);
				Constraint[] ret = {first,second};
				return ret;
			}

			if (types[0].equals(Type.MetByOrOverlappedByOrIsFinishedByOrDuring)) {
				TimePoint fs = from.getStart();
				TimePoint ts = to.getStart();
				TimePoint fe = from.getEnd();
				TimePoint te = to.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				SimpleDistanceConstraint second = new SimpleDistanceConstraint();
				first.setMinimum(1);
				first.setMaximum(APSPSolver.INF);
				first.setFrom(ts);
				first.setTo(fs);
				second.setMinimum(0);
				second.setMaximum(APSPSolver.INF);
				second.setFrom(fs);
				second.setTo(te);
				Constraint[] ret = {first,second};
				return ret;
			}

			if (types[0].equals(Type.MeetsOrOverlapsOrBefore)) {
				TimePoint ts = from.getStart();
				TimePoint fs = to.getStart();
				TimePoint te = from.getEnd();
				TimePoint fe = to.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				SimpleDistanceConstraint second = new SimpleDistanceConstraint();
				first.setMinimum(1);
				first.setMaximum(APSPSolver.INF);
				first.setFrom(fs);
				first.setTo(ts);
				second.setMinimum(1);
				second.setMaximum(APSPSolver.INF);
				second.setFrom(fe);
				second.setTo(te);
				Constraint[] ret = {first,second};
				return ret;
			}

			if (types[0].equals(Type.DuringOrEquals)) {
				TimePoint fs = from.getStart();
				TimePoint ts = to.getStart();
				TimePoint fe = from.getEnd();
				TimePoint te = to.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				SimpleDistanceConstraint second = new SimpleDistanceConstraint();
				first.setMinimum(bounds[0].min);
				first.setMaximum(bounds[0].max);
				first.setFrom(ts);
				first.setTo(fs);
				second.setMinimum(bounds[1].min);
				second.setMaximum(bounds[1].max);
				second.setFrom(fe);
				second.setTo(te);
				Constraint[] ret = {first,second};
				return ret;
			}
			
			if (types[0].equals(Type.DuringOrEqualsOrStartsOrFinishes)) {
				TimePoint fs = from.getStart();
				TimePoint ts = to.getStart();
				TimePoint fe = from.getEnd();
				TimePoint te = to.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				SimpleDistanceConstraint second = new SimpleDistanceConstraint();
				first.setMinimum(bounds[0].min);
				first.setMaximum(bounds[0].max);
				first.setFrom(ts);
				first.setTo(fs);
				second.setMinimum(bounds[1].min);
				second.setMaximum(bounds[1].max);
				second.setFrom(fe);
				second.setTo(te);
				Constraint[] ret = {first,second};
				return ret;
			}
			
			
			if (types[0].equals(Type.MeetsOrOverlapsOrFinishedByOrContains)) {
				TimePoint fs = from.getStart();
				TimePoint ts = to.getStart();
				TimePoint fe = from.getEnd();
//				TimePoint te = to.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				SimpleDistanceConstraint second = new SimpleDistanceConstraint();
				first.setMinimum(1);
				first.setMaximum(APSPSolver.INF);
				first.setFrom(fs);
				first.setTo(ts);
				second.setMinimum(bounds[0].min);
				second.setMaximum(bounds[0].max);
				second.setFrom(ts);
				second.setTo(fe);
				Constraint[] ret = {first,second};
				return ret;
			}
			
			if (types[0].equals(Type.ContainsOrStartedByOrOverlappedByOrMetBy)) {
				TimePoint fs = from.getStart();
//				TimePoint ts = to.getStart();
				TimePoint fe = from.getEnd();
				TimePoint te = to.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				SimpleDistanceConstraint second = new SimpleDistanceConstraint();
				first.setMinimum(1);
				first.setMaximum(APSPSolver.INF);
				first.setFrom(te);
				first.setTo(fe);
				second.setMinimum(bounds[0].min);
				second.setMaximum(bounds[0].max);
				second.setFrom(fs);
				second.setTo(te);
				Constraint[] ret = {first,second};
				return ret;
			}			
			
			if (types[0].equals(Type.StartStart)) {
				TimePoint fs = from.getStart();
				TimePoint ts = to.getStart();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				first.setMinimum(bounds[0].min);
				first.setMaximum(bounds[0].max);
				first.setFrom(fs);
				first.setTo(ts);
				Constraint[] ret = {first};
				return ret;	
			}

			if (types[0].equals(Type.StartsOrStartedBy)) {
				TimePoint fs = from.getStart();
				TimePoint ts = to.getStart();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				first.setMinimum(bounds[0].min);
				first.setMaximum(bounds[0].max);
				first.setFrom(fs);
				first.setTo(ts);
				Constraint[] ret = {first};
				return ret;	
			}

			if (types[0].equals(Type.EndEnd)) {
				TimePoint fe = from.getEnd();
				TimePoint te = to.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				first.setMinimum(bounds[0].min);
				first.setMaximum(bounds[0].max);
				first.setFrom(fe);
				first.setTo(te);
				Constraint[] ret = {first};
				return ret;	
			}
			
			if (types[0].equals(Type.EndsOrEndedBy)) {
				TimePoint fe = from.getEnd();
				TimePoint te = to.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				first.setMinimum(bounds[0].min);
				first.setMaximum(bounds[0].max);
				first.setFrom(fe);
				first.setTo(te);
				Constraint[] ret = {first};
				return ret;	
			}
			
			if (types[0].equals(Type.NotBeforeAndNotAfter)) {
				TimePoint fs = from.getStart();
				TimePoint ts = to.getStart();
				TimePoint fe = from.getEnd();
				TimePoint te = to.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				SimpleDistanceConstraint second = new SimpleDistanceConstraint();
				first.setMinimum(0);
				first.setMaximum(APSPSolver.INF);
				second.setMinimum(0);
				second.setMaximum(APSPSolver.INF);
				first.setFrom(fs);
				first.setTo(te);
				second.setFrom(ts);
				second.setTo(fe);
				Constraint[] ret = {first,second};
				return ret;	
			}
		}
		return null;
	}

	@Override
	public String getEdgeLabel() {
		//String ret = this.type.toString();
		String ret = Arrays.toString(this.types);
		//if(!type.equals(Type.DisjunctionRelation))
		if(this.types.length == 1)
			for (Bounds in : bounds) ret += " " + in.toString();
		ret+=" AR "+this.isAutoRemovable()+" ";
		ret+=" ID "+this.getID()+" ";
		return ret;
	}

	@Override
	public Object clone() {
//		if (this.type.equals(Type.DisjunctionRelation))
//			return new AllenIntervalConstraint(this.types);
//		return new AllenIntervalConstraint(this.type, this.bounds);
		AllenIntervalConstraint r=null;
		if (this.types.length > 1)
			r= new AllenIntervalConstraint(this.types);
		else
			r=new AllenIntervalConstraint(this.types[0], this.bounds);
		
		r.setAutoRemovable(this.isAutoRemovable());
		return r;
	}


	@Override
	public boolean isEquivalent(Constraint c) {
		AllenIntervalConstraint ac = (AllenIntervalConstraint)c;
		//if (this.type.equals(Type.DisjunctionRelation)) {
		if (this.types.length > 1) {
			for (Type t : this.types) {
				if (Arrays.binarySearch(ac.types, t) < 0) return false;
			}
			return true;
		}
		//return (ac.getType().equals(this.getType()) && ac.getFrom().equals(this.getFrom()) && ac.getTo().equals(this.getTo()));
		return (ac.getTypes()[0].equals(this.getTypes()[0]) && ac.getFrom().equals(this.getFrom()) && ac.getTo().equals(this.getTo()));
	}

	/**
	 * Get the qualitative relation that exists between two {@link AllenInterval}s, under the earliest time assumption.
	 * @param i1 The first {@link AllenInterval} in the sought relation.
	 * @param i2 The second {@link AllenInterval} in the sought relation.
	 * @return The qualitative relation in {@link Type} that exists between the two given {@link AllenInterval}s (under the earliest time assumption).
	 */
	public static Type getRelation(AllenInterval i1, AllenInterval i2) {
		if (i1.getEET() == i2.getEST()) return Type.Meets;
		if (i2.getEET() == i1.getEST()) return Type.MetBy;
		if (i1.getEST() ==  i2.getEST() && i1.getEET() == i2.getEET()) return Type.Equals;
		if (i1.getEET() < i2.getEST()) return Type.Before;
		if (i2.getEET() < i1.getEST()) return Type.After;
		if (i1.getEST() < i2.getEST() && i1.getEET() > i2.getEST() && i1.getEET() < i2.getEET()) return Type.Overlaps;
		if (i2.getEST() < i1.getEST() && i2.getEET() > i1.getEST() && i2.getEET() < i1.getEET()) return Type.OverlappedBy;
		if (i1.getEST() == i2.getEST() && i1.getEET() < i2.getEET()) return Type.Starts;
		if (i1.getEST() == i2.getEST() && i1.getEET() > i2.getEET()) return Type.StartedBy;
		if (i2.getEST() < i1.getEST() && i2.getEET() > i1.getEET()) return Type.During;
		if (i1.getEST() < i2.getEST() && i1.getEET() > i2.getEET()) return Type.Contains;
		if (i1.getEST() > i2.getEST() && i1.getEET() == i2.getEET()) return Type.Finishes;
		return Type.FinishedBy;
	}
	
	/*
	 * these gives the translation of quantitative Allen to Qualitative Allen based on the relation over
	 * first bound = [fs ts]
	 * second bound = [fs te]
	 * third bound = [fe ts]
	 * fourth bound = [fe te]
	 */
	private static Bounds[] getQuantitativeTranslationOfAllen(AllenIntervalConstraint.Type aType){
		
		Bounds[] bound = new Bounds[4];
		
		if(aType.equals(Type.OverlappedBy)){
			bound[0] = new Bounds(-APSPSolver.INF, 0);
			bound[1] = new Bounds(0, APSPSolver.INF);
			bound[2] = new Bounds(-APSPSolver.INF, 0);
			bound[3] = new Bounds(-APSPSolver.INF, 0);
		}
		
		else if(aType.equals(Type.Meets)){
			bound[0] = new Bounds(0, APSPSolver.INF);
			bound[1] = new Bounds(0, APSPSolver.INF);
			bound[2] = new Bounds(0, 0);
			bound[3] = new Bounds(0, APSPSolver.INF);
		}
		
		else if(aType.equals(Type.MetBy)){
			bound[0] = new Bounds(-APSPSolver.INF, 0);
			bound[1] = new Bounds(0, 0);
			bound[2] = new Bounds(-APSPSolver.INF, 0);
			bound[3] = new Bounds(-APSPSolver.INF, 0);
		}
		
		else if(aType.equals(Type.Starts)){
			bound[0] = new Bounds(0, 0);
			bound[1] = new Bounds(0, APSPSolver.INF);
			bound[2] = new Bounds(-APSPSolver.INF, 0);
			bound[3] = new Bounds(0, APSPSolver.INF);
		}
		
		else if(aType.equals(Type.StartedBy)){
			bound[0] = new Bounds(0, 0);
			bound[1] = new Bounds(0, APSPSolver.INF);
			bound[2] = new Bounds(-APSPSolver.INF, 0);
			bound[3] = new Bounds(-APSPSolver.INF, 0);
		}
		
		else if(aType.equals(Type.Finishes)){
			bound[0] = new Bounds(-APSPSolver.INF, 0);
			bound[1] = new Bounds(0, APSPSolver.INF);
			bound[2] = new Bounds(-APSPSolver.INF, 0);
			bound[3] = new Bounds(0, 0);
		}
		
		else if(aType.equals(Type.Equals)){
			bound[0] = new Bounds(0, 0);
			bound[1] = new Bounds(0, APSPSolver.INF);
			bound[2] = new Bounds(-APSPSolver.INF, 0);
			bound[3] = new Bounds(0, 0);
		}
		
		else if(aType.equals(Type.Before)){
			bound[0] = new Bounds(0, APSPSolver.INF);
			bound[1] = new Bounds(0, APSPSolver.INF);
			bound[2] = new Bounds(0, APSPSolver.INF);
			bound[3] = new Bounds(0, APSPSolver.INF);
		}
		
		else if(aType.equals(Type.After)){
			bound[0] = new Bounds(-APSPSolver.INF, 0);
			bound[1] = new Bounds(-APSPSolver.INF, 0);
			bound[2] = new Bounds(-APSPSolver.INF, 0);
			bound[3] = new Bounds(-APSPSolver.INF, 0);
		}
		
		else if(aType.equals(Type.During)){
			bound[0] = new Bounds(-APSPSolver.INF, 0);
			bound[1] = new Bounds(0, APSPSolver.INF);
			bound[2] = new Bounds(-APSPSolver.INF, 0);
			bound[3] = new Bounds(0, APSPSolver.INF);
		}
		
		else if(aType.equals(Type.Contains)){
			bound[0] = new Bounds(0, APSPSolver.INF);
			bound[1] = new Bounds(0, APSPSolver.INF);
			bound[2] = new Bounds(-APSPSolver.INF, 0);
			bound[3] = new Bounds(-APSPSolver.INF, 0);
		}
		
		else if(aType.equals(Type.Overlaps)){
			bound[0] = new Bounds(0, APSPSolver.INF);
			bound[1] = new Bounds(0, APSPSolver.INF);
			bound[2] = new Bounds(-APSPSolver.INF, 0);
			bound[3] = new Bounds(0, APSPSolver.INF);
		}

		else if(aType.equals(Type.FinishedBy)){
			bound[0] = new Bounds(0, APSPSolver.INF);
			bound[1] = new Bounds(0, APSPSolver.INF);
			bound[2] = new Bounds(-APSPSolver.INF, 0);
			bound[3] = new Bounds(0, 0);
		}
		
		return bound;
	}
	
	
}
