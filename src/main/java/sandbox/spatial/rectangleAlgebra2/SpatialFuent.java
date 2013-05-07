package sandbox.spatial.rectangleAlgebra2;

import symbols.SymbolicVariable;
import multi.activity.Activity;
import multi.allenInterval.AllenInterval;
import framework.Constraint;
import framework.ConstraintSolver;
import framework.Domain;
import framework.Variable;
import framework.multi.MultiVariable;

public class SpatialFuent extends MultiVariable{
	
	private String name= "";
	private Activity activity;
	private RectangularRegion2 rectangularRegion;
	
	
	protected SpatialFuent(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers) {
		super(cs, id, internalSolvers);
		// TODO Auto-generated constructor stub
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
		String ret="";
		ret += "<" + " Rectangle Variable: " + this.rectangularRegion + ", "+ "Activty: " + this.activity;
		if (this.activity.getMarking() != null) ret += "/" + this.activity.getMarking();
		ret += ">";
		return ret;
	}
	
	public Activity getActivity() {
		return activity;
	}
	
	public RectangularRegion2 getRectangularRegion() {
		return rectangularRegion;
	}

}
