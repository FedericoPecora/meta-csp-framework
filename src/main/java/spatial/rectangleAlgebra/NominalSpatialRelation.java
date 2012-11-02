package spatial.rectangleAlgebra;

public class NominalSpatialRelation {
	
	private String from = "";
	private String to = "";
	private String nominalConstraint = "";
	private RectangleConstraint raCons = new RectangleConstraint();
	
	public NominalSpatialRelation(String from, String to, String nominalConstraint){
		this.from = from;
		this.to = to;
		this.nominalConstraint = nominalConstraint;
	}
	
	public String getTo() {
		return to;
	}
	
	public String getFrom() {
		return from;
	}
	
	public String getNominalConstraint() {
		return nominalConstraint;
	}

	public RectangleConstraint getRaCons() {
		return raCons;
	}
	
	public void setRaCons(RectangleConstraint raCons) {
		this.raCons = raCons;
	}
	
	//to implement a mapper
	
	
	

	
	
	

}
