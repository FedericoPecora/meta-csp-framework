/*******************************************************************************
 * Copyright (c) 2010-2013 Federico Pecora <federico.pecora@oru.se>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
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
