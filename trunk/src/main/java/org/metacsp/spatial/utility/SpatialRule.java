package org.metacsp.spatial.utility;

import org.metacsp.multi.spatial.blockAlgebra.BlockAlgebraConstraint;
import org.metacsp.multi.spatial.blockAlgebra.UnaryBlockConstraint;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangleConstraint;
import org.metacsp.multi.spatial.rectangleAlgebra.UnaryRectangleConstraint;


public class SpatialRule {
	

	private String from = "";
	private String to = "";
	private RectangleConstraint binaryRA;
	private UnaryRectangleConstraint unaryRA;
	private BlockAlgebraConstraint binaryBA;
	private UnaryBlockConstraint unaryBA;
	private boolean isBlockAlgebra = false;
	
	public SpatialRule(String from, String to, UnaryRectangleConstraint unaryRA){
		this.from = from;
		this.to = to;
		this.unaryRA = unaryRA;
	}

	public SpatialRule(String from, String to, UnaryBlockConstraint unaryBA){
		this.from = from;
		this.to = to;
		this.unaryBA = unaryBA;
		isBlockAlgebra = true;
	}

	
	public SpatialRule(String from, String to, RectangleConstraint binaryRA){
		this.from = from;
		this.to = to;
		this.binaryRA = binaryRA;
	}

	public SpatialRule(String from, String to, BlockAlgebraConstraint binaryBA){
		this.from = from;
		this.to = to;
		this.binaryBA = binaryBA;
	}
	
	
	public String getTo() {
		return to;
	}
	
	public String getFrom() {
		return from;
	}
	
	
	
	public UnaryRectangleConstraint getUnaryRAConstraint(){
		return this.unaryRA;
	}

	public UnaryBlockConstraint getUnaryBAConstraint(){
		return this.unaryBA;
	}

	
	public RectangleConstraint getBinaryRAConstraint(){
		return this.binaryRA;
	}
	
	public BlockAlgebraConstraint getBinaryBAConstraint(){
		return this.binaryBA;
	}

	
	public String toString() {
		if(isBlockAlgebra){
			if(binaryBA != null)
				return "(" + this.getFrom() + ") --" + this.binaryBA + "--> (" + this.getTo() + ")";
			if(unaryBA != null)
				return "(" + this.getFrom() + ") --" + this.unaryBA + "--> (" + this.getTo() + ")";

		}
		else{
			if(binaryRA != null)
				return "(" + this.getFrom() + ") --" + this.binaryRA + "--> (" + this.getTo() + ")";
			if(unaryRA != null)
				return "(" + this.getFrom() + ") --" + this.unaryRA + "--> (" + this.getTo() + ")";
		}
		return null;
	}
	
	
	
	

}
