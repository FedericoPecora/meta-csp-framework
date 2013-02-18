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
package sandbox.spatial.rectangleAlgebra2;

import framework.Constraint;
import framework.ConstraintSolver;
import framework.Domain;
import framework.Variable;
import framework.multi.MultiVariable;

public class RectangularRegion2 extends MultiVariable {

	protected RectangularRegion2(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers) {
		super(cs, id, internalSolvers);
	}

	private static final long serialVersionUID = -864200952441853571L;	
	private String name = "";

	public void setName(String name) {
		this.name = name;
	}
				
	@Override
	public String toString() {
		return "{" + this.getClass().getSimpleName() + " " + (this.name != null ? this.name + " " : "" ) + this.getDomain() +"}";
	}

	@Override
	public int compareTo(Variable arg0) {
		return this.getID() - arg0.getID();
	}

	@Override
	protected Variable[] createInternalVariables() {
		Variable[] ret = new Variable[2];
		//X
		ret[0] = this.getInternalConstraintSolvers()[0].createVariable();
		//Y
		ret[1] = this.getInternalConstraintSolvers()[1].createVariable();
		return ret;
	}

	@Override
	protected Constraint[] createInternalConstraints(Variable[] variables) {
		return null;
	}

	@Override
	public void setDomain(Domain d) {
		// TODO Auto-generated method stub		
	}
	


}
