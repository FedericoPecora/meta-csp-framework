package sandbox.spatial.rectangleAlgebra2;

import multi.activity.Activity;
import framework.Constraint;
import framework.ConstraintSolver;
import framework.Domain;
import framework.Variable;
import framework.multi.MultiVariable;

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
	
	public void setActivity(Activity activity){
		//this.activity = activity;
		this.variables[1] = activity;
	}

	public void setRectangularRegion(RectangularRegion2 rectangluarRegion){
		//this.rectangularRegion = rectangluarRegion;
		this.variables[0] = rectangluarRegion;
	}

	
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
		String ret="<" + " Rectangle Variable: " + this.getInternalVariables()[0] + ", "+ "Activty: " + this.getInternalVariables()[1] + ">";
		return ret;
	}
	
	public Activity getActivity() {
		//return activity;
		return (Activity)this.getInternalVariables()[1];
	}
	
	public RectangularRegion2 getRectangularRegion() {
		//return rectangularRegion;
		return (RectangularRegion2)this.getInternalVariables()[0];
	}

}
