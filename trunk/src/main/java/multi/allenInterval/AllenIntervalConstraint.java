package multi.allenInterval;

import java.util.Arrays;
import java.util.Vector;

import time.APSPSolver;
import time.Bounds;
import time.Interval;
import time.SimpleDistanceConstraint;
import time.TimePoint;
import framework.Constraint;
import framework.Variable;
import framework.multi.MultiBinaryConstraint;

public class AllenIntervalConstraint extends MultiBinaryConstraint {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4010342193923812891L;

	public static enum Type {
		//Before,Meets,Overlaps,FinishedBy,Contains,StartedBy,Equals,Starts,During,Finishes,OverlappedBy,
		//MetBy,After,
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A BEFORE [l,u] B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../../images/before.png> 
		 */
		Before(0L, APSPSolver.INF),
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A MEETS B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../../images/meets.png> 
		 */
		Meets(0),
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A OVERLAPS [l,u] B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../../images/overlaps.png> 
		 */
		Overlaps(0L, APSPSolver.INF),
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A FINISHED-BY B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../../images/finishedby.png> 
		 */
		FinishedBy(0),
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A CONTAINS [sl,su] [el,eu] B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../../images/contains.png> 
		 */
		Contains(0L, APSPSolver.INF,   0L, APSPSolver.INF),
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A STARTED-BY B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../../images/startedby.png> 
		 */
		StartedBy(0),
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A EQUALS B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../../images/equals.png> 
		 */
		Equals(0),
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A STARTS B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../../images/starts.png> 
		 */
		Starts(0),
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A DURING [sl,su] [el,eu] B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../../images/during.png> 
		 */
		During(0L, APSPSolver.INF,   0L, APSPSolver.INF),
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A FINISHES B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../../images/finishes.png> 
		 */
		Finishes(0),
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A OVERLAPPED-BY [l,u] B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../../images/overlappedby.png> 
		 */
		OverlappedBy(0L, APSPSolver.INF),
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A AFTER [l,u] B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../../images/after.png> 
		 */
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A MET-BY B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=../../../images/metby.png> 
		 */
		MetBy(0),
		
		After(0L, APSPSolver.INF),
		

		BeforeOrMeets(0L, APSPSolver.INF), 


		MetByOrAfter(0L, APSPSolver.INF),

		MetByOrOverlappedBy(0L, APSPSolver.INF),

		MetByOrOverlappedByOrAfter(0),
		
		MeetsOrOverlapsOrBefore(0),

		DuringOrEquals(0L, APSPSolver.INF,   0L, APSPSolver.INF),
		
		
		EndsDuring(0),
		EndEnd(0L, APSPSolver.INF),
		

		At(0L, APSPSolver.INF, 0L, APSPSolver.INF),		
		StartStart(0L, APSPSolver.INF),
		Duration(0L, APSPSolver.INF),		
		Release(0L, APSPSolver.INF),
		Deadline(0L, APSPSolver.INF),
		DisjunctionRelation;
		
		
		private static Bounds[] createDefaultBounds(Long... bounds) {
			assert bounds.length % 2 == 0;
			
			final Bounds[] intervals = new Bounds[bounds.length/2];
			
			for(int i = 0; i < bounds.length; i+=2) {
				intervals[i/2] = new Bounds(bounds[i], bounds[i+1]);
			}
			return intervals;
		}
		
		private final Bounds[] defaultIntervalBounds;
		protected final int numParams;
		
		
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
	
	protected Type type;
	private Bounds[] bounds;
	protected Type[] types;
	
	@Deprecated
	public AllenIntervalConstraint(Type type, Interval first, Interval... remainder) {		
		this.type = type;
		Interval[] intervals = Arrays.asList(first, remainder).toArray(new Interval[remainder.length + 1]);
		bounds = new Bounds[intervals.length];
		for(int i = 0; i < bounds.length; ++i) {
			bounds[i] = new Bounds(intervals[i].getLowerBound(), intervals[i].getUpperBound());
		}
		
		if(type.numParams >= 0 && type.numParams != bounds.length) {
			throw new IllegalArgumentException("Invalid numer of parameters for constraint " + type + ", expected: " + type.numParams);
		}
	}
	
	
	public AllenIntervalConstraint(Type type, Bounds ...bounds) {
		this.type = type;
		this.bounds = bounds;
		if(type.numParams >= 0 && type.numParams != bounds.length) {
			throw new IllegalArgumentException("Invalid numer of parameters for constraint " + type + ", expected: " + type.numParams + " got "+ bounds.length );
		}
	}
	
	
//	public AllenIntervalConstraint(Option opt, Type... types) {
	public AllenIntervalConstraint(Type[] types) {
		
		if(types.length == 1){
			this.type = types[0];
			this.bounds = type.getDefaultBounds();
		}
		else{
			//it assumed that creating convexity is done one step before, in other words, where it calls this constructor
			this.types = types;
			this.type = Type.DisjunctionRelation;
		
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
	public Type getType() { return type; }
	
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
			if(type.equals(Type.DisjunctionRelation)){
				
				TimePoint fs = from.getStart();
				TimePoint ts = to.getStart();
				TimePoint fe = from.getEnd();
				TimePoint te = to.getEnd();
				
				
				//SimpleDistanceConstraint[] simDisCons = new SimpleDistanceConstraint[4]; //fs - ts
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				SimpleDistanceConstraint second = new SimpleDistanceConstraint();//fs te
				SimpleDistanceConstraint third = new SimpleDistanceConstraint();//fe ts
				SimpleDistanceConstraint fourth = new SimpleDistanceConstraint();//fe te
				if(bounds[0].min == APSPSolver.MINUSINF && bounds[0].max == APSPSolver.INF)
					first = null;
				else if(bounds[0].min == APSPSolver.MINUSINF){
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

				if(bounds[1].min == APSPSolver.MINUSINF && bounds[1].max == APSPSolver.INF)
					second = null;
				else if(bounds[1].min == APSPSolver.MINUSINF){
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

				if(bounds[2].min == APSPSolver.MINUSINF && bounds[2].max == APSPSolver.INF)
					third = null;
				else if(bounds[2].min == APSPSolver.MINUSINF){
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
				
				if(bounds[3].min == APSPSolver.MINUSINF && bounds[3].max == APSPSolver.INF)
					fourth = null;
				else if(bounds[3].min == APSPSolver.MINUSINF){
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
			
			if (type.equals(Type.Equals)) {
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
			
			if (type.equals(Type.Before)) {
				TimePoint ts = to.getStart();
				TimePoint fe = from.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				first.setMinimum(bounds[0].min+1);
				first.setMaximum(bounds[0].max);
				first.setFrom(fe);
				first.setTo(ts);
				Constraint[] ret = {first};
				return ret;
			}
			
			if (type.equals(Type.After)) {
				TimePoint fs = from.getStart();
				TimePoint te = to.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				first.setMinimum(bounds[0].min+1);
				first.setMaximum(bounds[0].max);
				first.setFrom(te);
				first.setTo(fs);
				Constraint[] ret = {first};
				return ret;
			}
			
			if (type.equals(Type.Meets)) {
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
			
			if (type.equals(Type.MetBy)) {
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
			
			if (type.equals(Type.Starts)) {
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
				second.setMinimum(1);
				second.setMaximum(APSPSolver.INF);
				second.setFrom(fe);
				second.setTo(te);
				Constraint[] ret = {first,second};
				return ret;
			}
			
			if (type.equals(Type.StartedBy)) {
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
				second.setMinimum(1);
				second.setMaximum(APSPSolver.INF);
				second.setFrom(te);
				second.setTo(fe);
				Constraint[] ret = {first,second};
				return ret;
			}
			
			if (type.equals(Type.During)) {
				TimePoint fs = from.getStart();
				TimePoint ts = to.getStart();
				TimePoint fe = from.getEnd();
				TimePoint te = to.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				SimpleDistanceConstraint second = new SimpleDistanceConstraint();
				first.setMinimum(bounds[0].min+1);
				first.setMaximum(bounds[0].max);
				first.setFrom(ts);
				first.setTo(fs);
				second.setMinimum(bounds[1].min+1);
				second.setMaximum(bounds[1].max);
				second.setFrom(fe);
				second.setTo(te);
				Constraint[] ret = {first,second};
				return ret;
			}
			
			if (type.equals(Type.Contains)) {
				TimePoint fs = from.getStart();
				TimePoint ts = to.getStart();
				TimePoint fe = from.getEnd();
				TimePoint te = to.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				SimpleDistanceConstraint second = new SimpleDistanceConstraint();
				first.setMinimum(bounds[0].min+1);
				first.setMaximum(bounds[0].max);
				first.setFrom(fs);
				first.setTo(ts);
				second.setMinimum(bounds[1].min+1);
				second.setMaximum(bounds[1].max);
				second.setFrom(te);
				second.setTo(fe);
				Constraint[] ret = {first,second};
				return ret;
			}
			
			if (type.equals(Type.Finishes)) {
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
				second.setMaximum(0);
				second.setFrom(fe);
				second.setTo(te);
				Constraint[] ret = {first,second};
				return ret;
			}
			
			if (type.equals(Type.FinishedBy)) {
				TimePoint fs = from.getStart();
				TimePoint ts = to.getStart();
				TimePoint fe = from.getEnd();
				TimePoint te = to.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				SimpleDistanceConstraint second = new SimpleDistanceConstraint();
				first.setMinimum(1);
				first.setMaximum(APSPSolver.INF);
				first.setFrom(fs);
				first.setTo(ts);
				second.setMinimum(0);
				second.setMaximum(0);
				second.setFrom(fe);
				second.setTo(te);
				Constraint[] ret = {first,second};
				return ret;
			}
			
			if (type.equals(Type.Overlaps)) {
				TimePoint fs = from.getStart();
				TimePoint ts = to.getStart();
				TimePoint fe = from.getEnd();
				TimePoint te = to.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				SimpleDistanceConstraint second = new SimpleDistanceConstraint();
				SimpleDistanceConstraint third = new SimpleDistanceConstraint();
				first.setMinimum(1);
				first.setMaximum(APSPSolver.INF);
				first.setFrom(fs);
				first.setTo(ts);
				second.setMinimum(bounds[0].min+1);
				second.setMaximum(bounds[0].max);
				second.setFrom(ts);
				second.setTo(fe);
				third.setMinimum(1);
				third.setMaximum(APSPSolver.INF);
				third.setFrom(fe);
				third.setTo(te);
				Constraint[] ret = {first,second,third};
				return ret;
			}
			
		
			if (type.equals(Type.OverlappedBy)) {
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
				second.setMinimum(bounds[0].min+1);
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
			
			if (type.equals(Type.At)) {
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
						
			if (type.equals(Type.Duration)) {
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
			
			if (type.equals(Type.Release)) {
				
				TimePoint fs = from.getStart();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
				first.setMinimum(bounds[0].min-((APSPSolver)from.getInternalConstraintSolvers()[0]).getO());
				first.setMaximum(bounds[0].max-((APSPSolver)from.getInternalConstraintSolvers()[0]).getO());
				first.setFrom(((APSPSolver)from.getInternalConstraintSolvers()[0]).getSource());
				first.setTo(fs);
				Constraint[] ret = {first};
				return ret;	
			}
			
			if (type.equals(Type.Deadline)) {
				TimePoint fe = from.getEnd();
				SimpleDistanceConstraint first = new SimpleDistanceConstraint();
//				first.setMinimum(((APSPSolver)from.getInternalConstraintSolvers()[0]).getH()-bounds[0].max);
//				first.setMaximum(((APSPSolver)from.getInternalConstraintSolvers()[0]).getH()-bounds[0].min);
				first.setMinimum(bounds[0].min);
				first.setMaximum(bounds[0].max);
				first.setFrom(((APSPSolver)from.getInternalConstraintSolvers()[0]).getSource());
				first.setTo(fe);
				Constraint[] ret = {first};
				return ret;	
			}
			
			if (type.equals(Type.BeforeOrMeets)) {
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
			
			
			if (type.equals(Type.MetByOrAfter)) {
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
			
			if (type.equals(Type.MetByOrOverlappedBy)) {
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

			if (type.equals(Type.MetByOrOverlappedByOrAfter)) {
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

			if (type.equals(Type.MeetsOrOverlapsOrBefore)) {
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

			if (type.equals(Type.DuringOrEquals)) {
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
			
			if (type.equals(Type.StartStart)) {
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
			
			if (type.equals(Type.EndEnd)) {
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
		}

		return null;
	}

	@Override
	public String getEdgeLabel() {
		String ret = this.type.toString();
		if(!type.equals(Type.DisjunctionRelation))
			for (Bounds in : bounds) ret += " " + in.toString();
		return ret;
	}

	@Override
	public Object clone() {
		if (this.type.equals(Type.DisjunctionRelation))
			return new AllenIntervalConstraint(this.types);
		return new AllenIntervalConstraint(this.type, this.bounds);
	}


	@Override
	public boolean isEquivalent(Constraint c) {
		AllenIntervalConstraint ac = (AllenIntervalConstraint)c;
		if (this.type.equals(Type.DisjunctionRelation)) {
			for (Type t : this.types) {
				if (Arrays.binarySearch(ac.types, t) < 0) return false;
			}
			return true;
		}
		return (ac.getType().equals(this.getType()) && ac.getFrom().equals(this.getFrom()) && ac.getTo().equals(this.getTo()));
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
			bound[0] = new Bounds(APSPSolver.MINUSINF, 0);
			bound[1] = new Bounds(0, APSPSolver.INF);
			bound[2] = new Bounds(APSPSolver.MINUSINF, 0);
			bound[3] = new Bounds(APSPSolver.MINUSINF, 0);
		}
		
		else if(aType.equals(Type.Meets)){
			bound[0] = new Bounds(0, APSPSolver.INF);
			bound[1] = new Bounds(0, APSPSolver.INF);
			bound[2] = new Bounds(0, 0);
			bound[3] = new Bounds(0, APSPSolver.INF);
		}
		
		else if(aType.equals(Type.MetBy)){
			bound[0] = new Bounds(APSPSolver.MINUSINF, 0);
			bound[1] = new Bounds(0, 0);
			bound[2] = new Bounds(APSPSolver.MINUSINF, 0);
			bound[3] = new Bounds(APSPSolver.MINUSINF, 0);
		}
		
		else if(aType.equals(Type.Starts)){
			bound[0] = new Bounds(0, 0);
			bound[1] = new Bounds(0, APSPSolver.INF);
			bound[2] = new Bounds(APSPSolver.MINUSINF, 0);
			bound[3] = new Bounds(0, APSPSolver.INF);
		}
		
		else if(aType.equals(Type.StartedBy)){
			bound[0] = new Bounds(0, 0);
			bound[1] = new Bounds(0, APSPSolver.INF);
			bound[2] = new Bounds(APSPSolver.MINUSINF, 0);
			bound[3] = new Bounds(APSPSolver.MINUSINF, 0);
		}
		
		else if(aType.equals(Type.Finishes)){
			bound[0] = new Bounds(APSPSolver.MINUSINF, 0);
			bound[1] = new Bounds(0, APSPSolver.INF);
			bound[2] = new Bounds(APSPSolver.MINUSINF, 0);
			bound[3] = new Bounds(0, 0);
		}
		
		else if(aType.equals(Type.Equals)){
			bound[0] = new Bounds(0, 0);
			bound[1] = new Bounds(0, APSPSolver.INF);
			bound[2] = new Bounds(APSPSolver.MINUSINF, 0);
			bound[3] = new Bounds(0, 0);
		}
		
		else if(aType.equals(Type.Before)){
			bound[0] = new Bounds(0, APSPSolver.INF);
			bound[1] = new Bounds(0, APSPSolver.INF);
			bound[2] = new Bounds(0, APSPSolver.INF);
			bound[3] = new Bounds(0, APSPSolver.INF);
		}
		
		else if(aType.equals(Type.After)){
			bound[0] = new Bounds(APSPSolver.MINUSINF, 0);
			bound[1] = new Bounds(APSPSolver.MINUSINF, 0);
			bound[2] = new Bounds(APSPSolver.MINUSINF, 0);
			bound[3] = new Bounds(APSPSolver.MINUSINF, 0);
		}
		
		else if(aType.equals(Type.During)){
			bound[0] = new Bounds(APSPSolver.MINUSINF, 0);
			bound[1] = new Bounds(0, APSPSolver.INF);
			bound[2] = new Bounds(APSPSolver.MINUSINF, 0);
			bound[3] = new Bounds(0, APSPSolver.INF);
		}
		
		else if(aType.equals(Type.Contains)){
			bound[0] = new Bounds(0, APSPSolver.INF);
			bound[1] = new Bounds(0, APSPSolver.INF);
			bound[2] = new Bounds(APSPSolver.MINUSINF, 0);
			bound[3] = new Bounds(APSPSolver.MINUSINF, 0);
		}
		
		else if(aType.equals(Type.Overlaps)){
			bound[0] = new Bounds(0, APSPSolver.INF);
			bound[1] = new Bounds(0, APSPSolver.INF);
			bound[2] = new Bounds(APSPSolver.MINUSINF, 0);
			bound[3] = new Bounds(0, APSPSolver.INF);
		}

		else if(aType.equals(Type.FinishedBy)){
			bound[0] = new Bounds(0, APSPSolver.INF);
			bound[1] = new Bounds(0, APSPSolver.INF);
			bound[2] = new Bounds(APSPSolver.MINUSINF, 0);
			bound[3] = new Bounds(0, 0);
		}
		
		return bound;
	}
	
	
	
	
}
