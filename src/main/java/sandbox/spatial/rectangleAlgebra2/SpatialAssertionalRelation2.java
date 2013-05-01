package sandbox.spatial.rectangleAlgebra2;

import spatial.rectangleAlgebra.AssertionalRelation;
import spatial.rectangleAlgebra.BoundingBox;
import spatial.rectangleAlgebra.OntologicalSpatialProperty;

public class SpatialAssertionalRelation2 extends AssertionalRelation{
	
	OntologicalSpatialProperty ontologicalProp;
	UnaryRectangleConstraint2 unaryRAConstraint;
	public SpatialAssertionalRelation2(String from, String to) {
		super(from, to);
		// TODO Auto-generated constructor stub
	}
	
	public void setUnaryAtRectangleConstraint(UnaryRectangleConstraint2 unaryRAConstraint){
		this.unaryRAConstraint = unaryRAConstraint;
	}
	
	public UnaryRectangleConstraint2 getUnaryAtRectangleConstraint(){
		return this.unaryRAConstraint;
	}
	
	
	public void setOntologicalProp(OntologicalSpatialProperty ontologicalProp) {
		this.ontologicalProp = ontologicalProp;
	}
	
	public OntologicalSpatialProperty getOntologicalProp() {
		return ontologicalProp;
	}
	

}
