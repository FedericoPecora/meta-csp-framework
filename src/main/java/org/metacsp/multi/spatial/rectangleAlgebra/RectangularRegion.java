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
package org.metacsp.multi.spatial.rectangleAlgebra;


import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiVariable;
import org.metacsp.multi.allenInterval.AllenInterval;
import org.metacsp.multi.spatial.rectangleAlgebraNew.toRemove.OntologicalSpatialProperty;
import org.metacsp.time.Bounds;

public class RectangularRegion extends MultiVariable {
	
	
	private OntologicalSpatialProperty ontologicalProp = null;

	
	public RectangularRegion(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers, Variable[] internalVars) {
		super(cs, id, internalSolvers, internalVars);
	}

	private static final long serialVersionUID = -864200952441853571L;	
	private String name = "";

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
				
	@Override
	public String toString() {
		return "{" + this.getClass().getSimpleName() + " " + (this.name != null ? this.name + " " : "" ) + this.getDomain() +"}";
	}

	@Override
	public int compareTo(Variable arg0) {
		return this.getID() - arg0.getID();
	}

//	@Override
//	protected Variable[] createInternalVariables() {
//		Variable[] ret = new Variable[2];
//		//X
//		ret[0] = this.getInternalConstraintSolvers()[0].createVariable();
//		((AllenInterval)ret[0]).setName("!X!");
//		//Y
//		ret[1] = this.getInternalConstraintSolvers()[1].createVariable();
//		((AllenInterval)ret[1]).setName("!Y!");
//		return ret;
//	}
	
	
	
	@Override
	protected Constraint[] createInternalConstraints(Variable[] variables) {
		return null;
	}

	@Override
	public void setDomain(Domain d) {
		// TODO Auto-generated method stub		
	}
	
	public OntologicalSpatialProperty getOntologicalProp() {
		if(ontologicalProp == null)
			return new OntologicalSpatialProperty();
		return ontologicalProp;
	}
	
	public void setOntologicalProp(OntologicalSpatialProperty ontologicalProp) {
		this.ontologicalProp = ontologicalProp;
	}
	
	public boolean isUnbounded(){
		
		AllenInterval intervalX = ((AllenInterval)this.getInternalVariables()[0]);
		AllenInterval intervalY = ((AllenInterval)this.getInternalVariables()[1]);
		
		Bounds xLB =  new Bounds(intervalX.getEST(), intervalX.getLST());
		Bounds xUB = new Bounds(intervalX.getEET(), intervalX.getLET());
		Bounds yLB = new Bounds(intervalY.getEST(), intervalY.getLST()); 
		Bounds yUB = new Bounds(intervalY.getEET(), intervalY.getLET());
		long horizon = ((RectangleConstraintSolver)this.solver).getHorizon();
		
		if( (xLB.min == 0 && xLB.max == horizon) && (xUB.min == 0&& xUB.max == horizon) &&
				(yLB.min == 0 && yLB.max == horizon) &&(yLB.min == 0 && yUB.max == horizon))
			return true;

		return false;
		
		
	}
	
	public BoundingBox getBoundingBox(){

		AllenInterval intervalX = ((AllenInterval)this.getInternalVariables()[0]);
		AllenInterval intervalY = ((AllenInterval)this.getInternalVariables()[1]);
		
		Bounds xLB =  new Bounds(intervalX.getEST(), intervalX.getLST());
		Bounds xUB = new Bounds(intervalX.getEET(), intervalX.getLET());
		Bounds yLB = new Bounds(intervalY.getEST(), intervalY.getLST()); 
		Bounds yUB = new Bounds(intervalY.getEET(), intervalY.getLET());

		return new BoundingBox(xLB, xUB, yLB, yUB);
	}
	
}
