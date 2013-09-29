package org.metacsp.multi.spatial.blockAlgebra;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiVariable;
import org.metacsp.multi.spatial.rectangleAlgebraNew.toRemove.OntologicalSpatialProperty;

public class RectangularCuboidRegion extends MultiVariable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3670510682773017173L;
	private OntologicalSpatialProperty ontologicalProp = null;
	private String name = "";
	
	public RectangularCuboidRegion(ConstraintSolver cs, int id,
			ConstraintSolver[] internalSolvers, Variable[] internalVars) {
		super(cs, id, internalSolvers, internalVars);
		// TODO Auto-generated constructor stub
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	
	@Override
	public String toString() {
		return "{" + this.getClass().getSimpleName() + " " + (this.name != null ? this.name + " " : "" ) + this.getDomain() +"}";
	}

	@Override
	public int compareTo(Variable arg0) {
		return this.getID() - arg0.getID();
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

	public OntologicalSpatialProperty getOntologicalProp() {
		if(ontologicalProp == null)
			return new OntologicalSpatialProperty();
		return ontologicalProp;
	}
	
	public void setOntologicalProp(OntologicalSpatialProperty ontologicalProp) {
		this.ontologicalProp = ontologicalProp;
	}


}
