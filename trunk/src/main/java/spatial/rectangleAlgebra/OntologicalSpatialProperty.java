package spatial.rectangleAlgebra;

public class OntologicalSpatialProperty {
	
	private boolean isGraspable = true;
	private boolean isMovable = true;
	
	public void setGraspable(boolean isGraspable) {
		this.isGraspable = isGraspable;
	}
	
	public void setMovable(boolean isMovable) {
		this.isMovable = isMovable;
	}
	
	public boolean isGraspable() {
		return isGraspable;
	}
	
	public boolean isMovable() {
		return isMovable;
	}
}
