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
package symbols;

import framework.ConstraintSolver;
import framework.Domain;
import framework.Variable;

public class SymbolicVariable extends Variable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9159383271134982131L;
	SymbolicDomain dom;
	
	protected SymbolicVariable(ConstraintSolver cs, int id) {
		super(cs, id);
	}

	@Override
	public Domain getDomain() {
		return this.dom;
	}
	
	@Override
	public void setDomain(Domain d) {
		if (d instanceof SymbolicDomain)
			this.dom = (SymbolicDomain)d;
	}
	
	public void setDomain(String ...symbols) {
		this.dom = new SymbolicDomain(this,symbols);
	}

	public void setDomain(SymbolicDomain s) {
		this.dom = s;
	}

	public String toString() {
		String ret = "SymbolicVariable " + this.getID();
		if (this.dom != null) ret += ": " + this.dom.toString();
		return ret;
	}

	@Override
	public int compareTo(Variable o) {
		// TODO Auto-generated method stub
		return 0;
	}


}
