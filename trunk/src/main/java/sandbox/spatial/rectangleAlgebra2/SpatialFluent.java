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
	private Activity activity;
	private RectangularRegion2 rectangularRegion;

	
	protected SpatialFluent(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers) {
		super(cs, id, internalSolvers);
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
	protected Variable[] createInternalVariables() {
		RectangularRegion2 rectangleVariable = (RectangularRegion2)internalSolvers[0].createVariable();
		Activity activityVariable= (Activity)internalSolvers[1].createVariable("robot1");
		this.activity = activityVariable;
		this.rectangularRegion = rectangleVariable;
		return new Variable[]{rectangleVariable, activityVariable};
	}
	
	public void setActivity(Activity activity){
		this.activity = activity;
	}

	public void setRectangularRegion(RectangularRegion2 rectangluarRegion){
		this.rectangularRegion = rectangluarRegion;
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
		String ret="<" + " Rectangle Variable: " + this.rectangularRegion + ", "+ "Activty: " + this.activity + ">";
		return ret;
	}
	
	public Activity getActivity() {
		return activity;
	}
	
	public RectangularRegion2 getRectangularRegion() {
		return rectangularRegion;
	}

}
