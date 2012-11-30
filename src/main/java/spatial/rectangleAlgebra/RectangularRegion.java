package spatial.rectangleAlgebra;

import spatial.RCC.Rectangle;
import spatial.RCC.Region;
import framework.ConstraintSolver;
import framework.Domain;
import framework.Variable;

public class RectangularRegion extends Region{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -864200952441853571L;
	private Domain dom;
	private BoundingBox boundingbox;
	private String name = "";
	private OntologicalSpatialProperty ontologicalProp = null;
	
	protected RectangularRegion(ConstraintSolver cs, int id) {
		super(cs, id);
		setDomain(new Rectangle(this));
	}
	
	public void setBoundingBox(BoundingBox booundingBox){
		this.boundingbox = booundingBox;
	}
	
	public BoundingBox getBoundingbox() {
		return boundingbox;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public void setDomain(Domain d) {
		this.dom = d;
	}
	
	@Override
	public String toString() {
//		if(name == "")
//			return "{" +this.getClass().getSimpleName() + this.id + " " + this.getDomain() +"}";
//		else
//			return "{" + name + this.id + " " + this.getDomain() +"}";
		
		if(name == "")
			return "{" +this.getClass().getSimpleName() + " " + this.getDomain() +"}";
		else
			return "{" + name  + " " + this.getDomain() +"}";
	}

	@Override
	public int compareTo(Variable arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	

	@Override
	public Domain getDomain() {
		// TODO Auto-generated method stub
		return dom;
	}
	
	public OntologicalSpatialProperty getOntologicalProp() {
		if(ontologicalProp == null)
			return new OntologicalSpatialProperty();
		return ontologicalProp;
	}
	
	public void setOntologicalProp(OntologicalSpatialProperty ontologicalProp) {
		this.ontologicalProp = ontologicalProp;
	}

}
