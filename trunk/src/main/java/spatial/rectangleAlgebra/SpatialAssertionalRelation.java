package spatial.rectangleAlgebra;

public class SpatialAssertionalRelation extends AssertionalRelation{

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

}
