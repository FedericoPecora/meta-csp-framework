package sandbox.spatial.rectangleAlgebra2;

import spatial.rectangleAlgebra.AugmentedRectangleConstraint;

public class SpatialRule2 {
	

	private String from = "";
	private String to = "";
	private RectangleConstraint2 binaryRA;
	private UnaryRectangleConstraint2 unaryRA;
	
	public SpatialRule2(String from, String to, UnaryRectangleConstraint2 unaryRA){
		this.from = from;
		this.to = to;
		this.unaryRA = unaryRA;
	}
	
	public SpatialRule2(String from, String to, RectangleConstraint2 binaryRA){
		this.from = from;
		this.to = to;
		this.binaryRA = binaryRA;
	}

	
	
	public String getTo() {
		return to;
	}
	
	public String getFrom() {
		return from;
	}
	

	public RectangleConstraint2 getBinaryRectangleConstraint() {
		return binaryRA;
	}
	
	public UnaryRectangleConstraint2 getUnaryRectangleConstraint(){
		return unaryRA;
	}
	
	public String toString() {
		if(binaryRA != null)
			return "(" + this.getFrom() + ") --" + this.getBinaryRectangleConstraint() + "--> (" + this.getTo() + ")";
		if(unaryRA != null)
			return "(" + this.getFrom() + ") --" + this.getUnaryRectangleConstraint() + "--> (" + this.getTo() + ")";
		return null;
	}
	
	
	
	

}
