package spatial.rectangleAlgebra;

public class SpatialRule {
	
	private String from = "";
	private String to = "";
	private String nominalConstraint = "";
	private AugmentedRectangleConstraint araCons;
	
	public SpatialRule(String from, String to, AugmentedRectangleConstraint aRAconstraint){
		this.from = from;
		this.to = to;
		this.araCons = aRAconstraint;
	}
	
	public String getTo() {
		return to;
	}
	
	public String getFrom() {
		return from;
	}
	
//	public String getNominalConstraint() {
//		return nominalConstraint;
//	}

	public AugmentedRectangleConstraint getRaCons() {
		return araCons;
	}
	
	
	
	

	
	
	

}
