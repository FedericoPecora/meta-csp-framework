package spatial.rectangleAlgebra;

public class SpatialAssertionalRelation extends AssertionalRelation{

	OntologicalSpatialProperty ontologicalProp;
	BoundingBox boundingBox;
	public SpatialAssertionalRelation(String from, String to) {
		super(from, to);
		// TODO Auto-generated constructor stub
	}
	
	public void setCoordinate(BoundingBox boundingBox){
		this.boundingBox = boundingBox;
	}
	
	public BoundingBox getCoordinate(){
		return this.boundingBox;
	}
	
	
	public void setOntologicalProp(OntologicalSpatialProperty ontologicalProp) {
		this.ontologicalProp = ontologicalProp;
	}
	
	public OntologicalSpatialProperty getOntologicalProp() {
		return ontologicalProp;
	}

}
