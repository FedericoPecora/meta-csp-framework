package org.metacsp.spatial.utility;

import org.metacsp.multi.spatial.blockAlgebra.UnaryBlockConstraint;
import org.metacsp.multi.spatial.rectangleAlgebra.UnaryRectangleConstraint;
import org.metacsp.multi.spatial.rectangleAlgebraNew.toRemove.OntologicalSpatialProperty;
import org.metacsp.multi.spatioTemporal.SpatialFluent;
import org.metacsp.multi.spatioTemporal.SpatialFluent2;


public class SpatialAssertionalRelation extends AssertionalRelation{
	
	private OntologicalSpatialProperty ontologicalProp;
	private UnaryRectangleConstraint unaryRAConstraint;
	private UnaryBlockConstraint unaryBAConstraint;

	
	
	public SpatialAssertionalRelation(String from, String to) {
		super(from, to);
		// TODO Auto-generated constructor stub
	}
	
	public void setUnaryAtRectangleConstraint(UnaryRectangleConstraint unaryRAConstraint){
		this.unaryRAConstraint = unaryRAConstraint;
	}

	public void setUnaryAtBlockConstraint(UnaryBlockConstraint unaryBAConstraint){
		this.unaryBAConstraint = unaryBAConstraint;
	}

	
	public UnaryRectangleConstraint getUnaryAtRectangleConstraint(){
		return this.unaryRAConstraint;
	}

	public UnaryBlockConstraint getUnaryAtBlockConstraint(){
		return this.unaryBAConstraint;
	}

	public void setOntologicalProp(OntologicalSpatialProperty ontologicalProp) {
		this.ontologicalProp = ontologicalProp;
	}
	
	public OntologicalSpatialProperty getOntologicalProp() {
		return ontologicalProp;
	}
	
	
}
