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

package booleanSAT;

import framework.ConstraintSolver;
import framework.Domain;
import framework.Variable;

public class BooleanVariable extends Variable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7608886359043666166L;
	
	private BooleanDomain domain;
	
	protected BooleanVariable(ConstraintSolver cs, int id) {
		super(cs, id);
		domain = new BooleanDomain(this,true,true);
	}

	public void allowTrue() {
		((BooleanDomain)this.getDomain()).allowTrue();
	}
	
	public void allowFalse() {
		((BooleanDomain)this.getDomain()).allowFalse();
	}

	@Override
	public int compareTo(Variable arg0) {
		return this.domain.compareTo(arg0);
	}

	@Override
	public Domain getDomain() {
		return this.domain;
	}

	@Override
	public void setDomain(Domain d) {
		this.domain = (BooleanDomain)d;
	}

	@Override
	public String toString() {
		return "x" + this.getID() + " " + this.domain.toString();
	}

}
