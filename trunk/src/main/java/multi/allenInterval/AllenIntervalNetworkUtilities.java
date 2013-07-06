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
package multi.allenInterval;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import time.APSPSolver;
import time.Bounds;
import time.SimpleDistanceConstraint;
import time.TimePoint;
import framework.Constraint;
import framework.ConstraintSolver;
import framework.Variable;

public class AllenIntervalNetworkUtilities {
	
	private final static Logger logger = Logger.getLogger(AllenIntervalNetworkUtilities.class.getPackage().getName());
	
	/**
	 * An {@link AllenInterval} that does not contain the default start to end time constraint 
	 */
	private final static class ReducedAllenInterval extends AllenInterval {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6237200272058310041L;

		private ReducedAllenInterval(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers, Variable[] internalVars) {
			super(cs, id, internalSolvers, internalVars);
		}
		
		@Override
		protected Constraint[] createInternalConstraints(Variable[] variables) {
			//Don't add the constraint constraint enforcing start <= stop
			return new Constraint[0];
		}
	}
	
	/**
	 * A {@link AllenIntervalNetworkSolver} that creates {@link ReducedAllenInterval}s
	 * instead of {@link AllenInterval}s. 
	 */
	private final static class ReducedAllenIntervalNetworkSolver extends AllenIntervalNetworkSolver {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 7109350824536430047L;

		private boolean createReducedIntervals = true;
		
		private static int IDs = 1;
		
		public ReducedAllenIntervalNetworkSolver(long origin, long horizon, int maxActivities) {
			super(origin, horizon, maxActivities);
		}
		
		public void setCreateReducedIntervals(boolean createReducedIntervals) {
			this.createReducedIntervals = createReducedIntervals;
		}
		
		private AllenInterval createReducedIfNecessary() {
			if(createReducedIntervals) {
				return new ReducedAllenInterval(this, IDs++, this.constraintSolvers, this.getVariables());
			} else {
				return (AllenInterval)super.createVariablesSub(1)[0];
			}
		}
		
		@Override
		protected AllenInterval[] createVariablesSub(int num) {
			AllenInterval[] intervals = new AllenInterval[num];
			for(int i = 0; i < num; ++i) {
				intervals[i] = createReducedIfNecessary();
			}
			return intervals;
		}
	}
	
	/**
	 * Creates a clone of an {@link AllenIntervalNetworkSolver}. This is done by copying the high level {@link AllenIntervalConstraint}s,
	 * thus, care should be exercised when using this function together with {@link #createReducedCopy},
	 * since this function discards the high-level constraints (by translating them into {@link SimpleDistanceConstraint}s).
	 * @param oldSolver the {@link AllenIntervalNetworkSolver} that should be cloned.
	 * @param intervals An array of {@link AllenInterval}s that should be re-mapped. The references to the in this array will be replaced
	 * with their corresponding {@link AllenInterval}s in the cloned {@link AllenIntervalNetworkSolver}. (All {@link AllenInterval}s in the old 
	 * {@link AllenIntervalNetworkSolver} will be cloned regardless of the contents of this array.)
	 * @param newSize The new desired size of the network or 0 if the network should be minimized
	 * @return A new {@link AllenIntervalNetworkSolver}.
	 */
	public static AllenIntervalNetworkSolver clone(AllenIntervalNetworkSolver oldSolver, Variable[] intervals, Constraint[] constraints, int newSize) {
		
		final AllenIntervalNetworkSolver newSolver;
		{
			final APSPSolver apspSolver = (APSPSolver) oldSolver.getConstraintSolvers()[0];
			
			final long newO = apspSolver.getO();
			final long newH = apspSolver.getH();
			final int newMaxActivities = newSize == 0 ? oldSolver.getVariables().length : newSize;
			newSolver = new AllenIntervalNetworkSolver(newO, newH, newMaxActivities);
		}
		
		final Variable[] old_variables = oldSolver.getVariables();
		final IdentityHashMap<AllenInterval, AllenInterval> translate_intervals = new IdentityHashMap<AllenInterval, AllenInterval>();
		for (Variable variable : old_variables) {
			final AllenInterval oldAllenInterval = (AllenInterval) variable;
			final AllenInterval newAllenInterval = (AllenInterval) newSolver.createVariable();
			newAllenInterval.setMarking(oldAllenInterval.getMarking());
			translate_intervals.put(oldAllenInterval, newAllenInterval);
		}
		
		//Clone the constraints
		final Constraint[] old_constraints = oldSolver.getConstraints();
		final IdentityHashMap<Constraint, Constraint> translate_constraints = new IdentityHashMap<Constraint, Constraint>();
		for(Constraint constraint : old_constraints) {
			final AllenIntervalConstraint old_constraint = (AllenIntervalConstraint) constraint;

			final AllenInterval old_from = (AllenInterval) old_constraint.getFrom();
			final AllenInterval old_to = (AllenInterval) old_constraint.getTo();

			final AllenInterval new_from = translate_intervals.get(old_from);
			final AllenInterval new_to = translate_intervals.get(old_to);

			final AllenIntervalConstraint.Type old_type = old_constraint.getTypes()[0];
			final Bounds[] old_intervals = old_constraint.getBounds();

			AllenIntervalConstraint new_constraint = new AllenIntervalConstraint(old_type,  old_intervals);
			new_constraint.setFrom(new_from);
			new_constraint.setTo(new_to);

			if(!newSolver.addConstraint(new_constraint)) {
				//This should not happen
				throw new Error("Could not add a constraint new constraint for " + old_constraint + "when cloning");
			}
			
			translate_constraints.put(old_constraint, new_constraint);
		}
		
		if(old_constraints.length == 0) {
			logger.log(Level.WARNING, "No constraints cloned()");
		}
		
		//Replace the old intervals in the array with the new ones 
		if(intervals != null) {
			for(int i = 0; i < intervals.length; ++i) {
				final AllenInterval newInterval = translate_intervals.get(intervals[i]);
				if(newInterval == null) {
					throw new Error("Failed to translate interval " + intervals[i]);
				}
				intervals[i] = newInterval;
			}
		}
		
		//Replace the old constraints in the array with the new ones
		if(constraints != null) {
			for(int i = 0; i < constraints.length; ++i) {
				Constraint newConstraint = translate_constraints.get(constraints[i]);
				if(newConstraint == null) {
					throw new Error("Failed to translate constraint " + constraints[i]);
				}
				
				constraints[i] = newConstraint;
			}
		}
		
		return newSolver;
	}
	
	/**
	 * Creates a "reduced copy" of an {@link AllenIntervalNetworkSolver}. The reduced copy contains a subset 
	 * of intervals from the original and their temporal properties are equivalent. The reduced copy does not
	 * contain the constraints from the original however, these are replaced with lower-level {@link SimpleDistanceConstraint}s. 
	 * @param oldSolver the solver that should be copied. 
	 * @param intervals The intervals of interest in the old solver, each {@link AllenInterval} reference in this array will
	 * be replaced with a corresponding reference to its copy in the new {@link AllenIntervalNetworkSolver}. 
	 * @return the copied {@link AllenIntervalNetworkSolver}.
	 */
	public static AllenIntervalNetworkSolver createReducedCopy(AllenIntervalNetworkSolver oldSolver, AllenInterval[] intervals) {
		return createReducedCopy(oldSolver, intervals, 0);
	}
	
	/**
	 * Creates a "reduced copy" of an {@link AllenIntervalNetworkSolver}. The reduced copy contains a subset 
	 * of intervals from the original and their temporal properties are equivalent. The reduced copy does not
	 * contain the constraints from the original however, these are replaced with lower-level {@link SimpleDistanceConstraint}s. 
	 * @param oldSolver the solver that should be copied. 
	 * @param intervalsInOut The intervals of interest in the old solver, each {@link AllenInterval} reference in this array will
	 * be replaced with a corresponding reference to its copy in the new {@link AllenIntervalNetworkSolver}. 
	 * @param additionalCapactity additional capacity for creating new intervals in the new solver. 
	 * @return the copied {@link AllenIntervalNetworkSolver}.
	 */
	public static AllenIntervalNetworkSolver createReducedCopy(AllenIntervalNetworkSolver oldSolver, AllenInterval[] intervalsInOut, int additionalCapactity) {
		
		//Create a new AllenIntervalNetworkSolver
		final ReducedAllenIntervalNetworkSolver newSolver;
		{
			final long newO = ((APSPSolver)oldSolver.getConstraintSolvers()[0]).getO();
			final long newH = ((APSPSolver)oldSolver.getConstraintSolvers()[0]).getH();
			newSolver = new ReducedAllenIntervalNetworkSolver(newO, newH, intervalsInOut.length + additionalCapactity);
		}
		
		//Create arrays containing input/output intervals
		final AllenInterval[] old_intervals = intervalsInOut;
		final AllenInterval[] new_intervals = new AllenInterval[old_intervals.length];
		
		for(int i = 0; i < old_intervals.length; ++i) {
			new_intervals[i] = (AllenInterval) newSolver.createVariable();
			new_intervals[i].setMarking(old_intervals[i].getMarking());
		}
		
		APSPSolver oldApspSolver = (APSPSolver) oldSolver.getConstraintSolvers()[0];
		APSPSolver newApspSolver = (APSPSolver) newSolver.getConstraintSolvers()[0];

		//Create arrays containing input/output time points
		final TimePoint[] old_timePoints = new TimePoint[2*old_intervals.length + 2];
		final TimePoint[] new_timePoints = new TimePoint[2*old_intervals.length + 2];
		
		old_timePoints[0] = oldApspSolver.getSource();
		old_timePoints[1] = oldApspSolver.getSink();
		new_timePoints[0] = newApspSolver.getSource();
		new_timePoints[1] = newApspSolver.getSink();
		
		for(int i = 0; i < old_intervals.length; ++i) {
			final int baseIdx = 2+i*2;
			old_timePoints[baseIdx + 0] = old_intervals[i].getStart();
			old_timePoints[baseIdx + 1] = old_intervals[i].getEnd();
			
			new_timePoints[baseIdx + 0] = new_intervals[i].getStart();
			new_timePoints[baseIdx + 1] = new_intervals[i].getEnd();			
		}
		
		ArrayList<SimpleDistanceConstraint> toAdd = new ArrayList<SimpleDistanceConstraint>(old_timePoints.length*old_timePoints.length);
		for(int i = 0; i < old_timePoints.length; ++i) {
			for(int j = 0; j < i; ++j) {
				final Bounds bounds = oldApspSolver.getDistanceBounds(old_timePoints[i], old_timePoints[j]);
				
				SimpleDistanceConstraint sdc = new SimpleDistanceConstraint();
				sdc.setFrom(new_timePoints[i]);
				sdc.setTo(new_timePoints[j]);
				sdc.setMinimum(bounds.min);
				sdc.setMaximum(bounds.max);
				
				//We need to make sure the constraint is positive, otherwise we can't add the constraint
				sdc = sdc.normalize();
				
				//Add the constraint
				toAdd.add(sdc);
			}
		}
		
		//Add all constraints to the new STP as a batch
		if(!newApspSolver.addConstraints(toAdd.toArray(new SimpleDistanceConstraint[toAdd.size()]))) {
			throw new Error("Failed to add SimpleDistanceConstraints to new solver");
		}
		
		//Do same basic testing, the entire loop should be optimized away when assertions are disabled
		for(int i = 0; i < old_intervals.length; ++i) {
			assert old_intervals[i].getEST() == new_intervals[i].getEST();
			assert old_intervals[i].getLST() == new_intervals[i].getLST();
			assert old_intervals[i].getEET() == new_intervals[i].getEET();
			assert old_intervals[i].getLET() == new_intervals[i].getLET();
		}
		
		//Do some more testing
		for(int i = 0; i < old_timePoints.length; ++i) {
			for(int j = 0; j < old_timePoints.length; ++j) {
				assert oldSolver.getAdmissibleDistanceBounds(old_timePoints[i], old_timePoints[j]).equals(
						newSolver.getAdmissibleDistanceBounds(new_timePoints[i], new_timePoints[j]));
			}
		}
		
		//Replace the interval references in the input array 
		System.arraycopy(new_intervals, 0, intervalsInOut, 0, new_intervals.length);
		
		newSolver.setCreateReducedIntervals(false);
		
		return newSolver;
	}
}
