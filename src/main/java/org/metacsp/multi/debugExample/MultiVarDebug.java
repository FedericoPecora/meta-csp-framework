package org.metacsp.multi.debugExample;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiVariable;
import org.metacsp.multi.activity.SymbolicVariableActivity;
import org.metacsp.multi.spatial.DE9IM.GeometricShapeDomain;
import org.metacsp.multi.spatial.DE9IM.GeometricShapeVariable;

import com.vividsolutions.jts.geom.Geometry;

public class MultiVarDebug extends MultiVariable{

	private static final long serialVersionUID = 5307859476353470487L;


	public MultiVarDebug(ConstraintSolver cs, int id,
			ConstraintSolver[] internalSolvers, Variable[] internalVars) {
		super(cs, id, internalSolvers, internalVars);
	}

	private String name;


	@Override
	protected Constraint[] createInternalConstraints(Variable[] variables) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int compareTo(Variable o) {
		return this.getID() - o.getID();
	}

	@Override
	public void setDomain(Domain d) {
		if (d instanceof GeometricShapeDomain) {
			this.getInternalVariables()[1].setDomain(d);
		}
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		if (this.name != null) {
			ret.append(this.name);
			ret.append(": ");
		} else {
			ret.append("SpatialActivity: ");
		}
		ret.append(", Activty: ");
		ret.append(this.getInternalVariables()[0]);
		
		ret.append("/");
		ret.append(this.getMarking());
		return ret.toString();
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public SymbolicVariableActivity getActivity() {
		return (SymbolicVariableActivity)this.getInternalVariables()[0];
	}
	
	public GeometricShapeVariable getGeometricShapeVariable() {
		return (GeometricShapeVariable)this.getInternalVariables()[1];
	}
	
	public Geometry getGeometry() {
		return ((GeometricShapeDomain) getGeometricShapeVariable().getDomain()).getGeometry();
	}

}
