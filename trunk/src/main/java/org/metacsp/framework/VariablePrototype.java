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
package org.metacsp.framework;

import java.util.Arrays;

/**
 * Objects of this type can be used to represent prototype variables.  This is useful since the only way to create a {@link Variable} is to
 * invoke a {@link ConstraintSolver}'s factory methods, which in turn perform other operations to keep track of the created variable(s).
 * If a variable is to be used only as a prototype, then all of these operations can be skipped, and this class used.  Variable prototypes are
 * not used for reasoning, rather real {@link Variable}s are automatically instantiated through the factory methods of concrete {@link ConstraintSolver}s on the
 * basis of the information contained in variable prototypes.
 *   
 * @author Federico Pecora
 *
 */
public class VariablePrototype extends Variable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6552888283766914530L;

	private Object[] parameters;
	
	/**
	 * Static ID counter for {@link VariablePrototype}s. 
	 */
	public static int id = 0;
	
	/**
	 * Create a new {@link VariablePrototype} with a given {@link ConstraintSolver} and given parameters. 
	 * @param cs The {@link ConstraintSolver} to which concrete {@link Variable}s created on the basis of this prototype
	 * should refer to.
	 * @param parameters Parameters useful for the creation of a concrete {@link Variable}.
	 */
	public VariablePrototype(ConstraintSolver cs, Object ... parameters) {
		super(cs, id++);
		this.parameters = parameters;
	}
	
	
	/**
	 * Get the parameters of this {@link VariablePrototype}.
	 * @return The parameters of this {@link VariablePrototype}.
	 */
	public Object[] getParameters() { return this.parameters; } 

	@Override
	public int compareTo(Variable arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Domain getDomain() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDomain(Domain d) {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return (Arrays.toString(parameters)+" ID "+ this.getID() +" - "+ this.getMarking()+ " |");
	}
	
	private VariablePrototype(VariablePrototype cp){
		super(cp.getConstraintSolver(),id++);
		this.parameters=cp.getParameters().clone();
	}
	
	public VariablePrototype clone(){
		return new VariablePrototype(this);
	}
	
	

}
