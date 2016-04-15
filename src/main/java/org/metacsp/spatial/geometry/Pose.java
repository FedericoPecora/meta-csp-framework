package org.metacsp.spatial.geometry;

public class Pose {
	
	Position position;
	Quaternion orientation;
	
	
	public Position getPosition() {
		return position;
	}
	
	public Quaternion getOrientation() {
		return orientation;
	}
	
	public void setPosition(Position position) {
		this.position = position;
	}
	
	public void setOrientation(Quaternion orientation) {
		this.orientation = orientation;
	}

}
