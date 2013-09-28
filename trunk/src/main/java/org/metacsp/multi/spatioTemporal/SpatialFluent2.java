package org.metacsp.multi.spatioTemporal;

import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.spatial.blockAlgebra.RectangularCuboidRegion;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiVariable;

public class SpatialFluent2 extends MultiVariable{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -470271876714589025L;
	private String name= "";
	
	public SpatialFluent2(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers, Variable[] internalVars) {
		super(cs, id, internalSolvers, internalVars);
	}
		
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public int compareTo(Variable arg0) {
		return this.getID() - arg0.getID();
	}

	
	@Override
	protected Constraint[] createInternalConstraints(Variable[] variables) {
		return null;
	}

	@Override
	public void setDomain(Domain d) {

	}

	@Override
	public String toString() {
		String ret="<" + " RectangulatCuboidRegion: " + this.getInternalVariables()[0] + ", "+ "Activty: " + this.getInternalVariables()[1] + ">";
		return ret;
	}
	
	public Activity getActivity() {
		return (Activity)this.getInternalVariables()[1];
	}
	
	public RectangularCuboidRegion getRectangularCuboidRegion() {
		return (RectangularCuboidRegion)this.getInternalVariables()[0];
	}

}
