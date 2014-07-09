package org.metacsp.multi.spatioTemporal;

import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangularRegion;
import org.metacsp.spatial.reachability.ConfigurationVariable;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiVariable;

public class SpatialFluent extends MultiVariable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6226801218238415115L;
	private String name= "";
//	private Activity activity;
//	private RectangularRegion2 rectangularRegion;
	
	public SpatialFluent(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers, Variable[] internalVars) {
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

//	@Override
//	protected Variable[] createInternalVariables() {
//		RectangularRegion2 rectangleVariable = (RectangularRegion2)internalSolvers[0].createVariable();
//		Activity activityVariable = (Activity)internalSolvers[1].createVariable();
//		this.activity = activityVariable;
//		this.rectangularRegion = rectangleVariable;
//		return new Variable[]{rectangleVariable, activityVariable};
//	}
	
	@Override
	protected Constraint[] createInternalConstraints(Variable[] variables) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDomain(Domain d) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String toString() {
//		String ret="<" + " Rectangle Variable: " + this.rectangularRegion + ", "+ "Activty: " + this.activity + ">";
		String ret="<" + " Rectangle Variable: " + this.getInternalVariables()[0] + ", "+ "Activty: " + this.getInternalVariables()[1] + ", "+ 
				" Configurationvariable " + this.getInternalVariables()[2]+ ">";
		return ret;
	}
	
	public Activity getActivity() {
		//return activity;
		return (Activity)this.getInternalVariables()[1];
	}
	
	public RectangularRegion getRectangularRegion() {
		//return rectangularRegion;
		return (RectangularRegion)this.getInternalVariables()[0];
	}
	
	public ConfigurationVariable getConfigurationVariable(){
		
		return (ConfigurationVariable)this.getInternalVariables()[2];
	}

}
