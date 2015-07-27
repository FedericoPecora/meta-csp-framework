package org.metacsp.multi.spatioTemporal;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiVariable;
import org.metacsp.multi.allenInterval.AllenInterval;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.spatial.DE9IM.DE9IMRelation;
import org.metacsp.multi.spatial.DE9IM.GeometricShapeDomain;
import org.metacsp.multi.spatial.DE9IM.GeometricShapeVariable;

/**
 * A {@link SpatioTemporalVariable} is a {@link MultiVariable} composed of an {@link AllenInterval} (temporal part)
 * and a {@link GeometricShapeVariable} (spatial part). Constraints of type {@link AllenIntervalConstraint} and
 * {@link DE9IMRelation} can be added to {@link SpatioTemporalVariable}s.
 * 
 * @author Federico Pecora
 */
public class SpatioTemporalVariable extends MultiVariable {

	private static final long serialVersionUID = 183736569434737103L;

	public SpatioTemporalVariable(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers, Variable[] internalVars) {
		super(cs, id, internalSolvers, internalVars);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public int compareTo(Variable o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected Constraint[] createInternalConstraints(Variable[] variables) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDomain(Domain d) {
		if (d instanceof GeometricShapeDomain) {
			this.getInternalVariables()[1].setDomain(d);
		}
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.id+"";
	}
	
	/**
	 * Returns the temporal part of this {@link SpatioTemporalVariable}.
	 * @return An {@link AllenInterval} representing the temporal part of this {@link SpatioTemporalVariable}.
	 */
	public AllenInterval getTemporalVariable() {
		return (AllenInterval)this.getInternalVariables()[0];
	}

	/**
	 * Returns the spatial part of this {@link SpatioTemporalVariable}.
	 * @return A {@link GeometricShapeVariable} representing the spatial part of this {@link SpatioTemporalVariable}.
	 */
	public GeometricShapeVariable getSpatialVariable() {
		return (GeometricShapeVariable)this.getInternalVariables()[1];
	}

}
