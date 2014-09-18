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
package org.metacsp.multi.activity;

import org.metacsp.multi.allenInterval.AllenInterval;
import org.metacsp.multi.symbols.SymbolicVariable;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiVariable;

public class Activity extends MultiVariable {
	
	private static final long serialVersionUID = 4709760631961797060L;
	private String[] symbols;

	public Activity(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers, Variable[] internalVars) {
		super(cs,id,internalSolvers,internalVars);
	}
	
	public void setSymbolicDomain(String... symbols) {
		this.symbols = symbols;
		((SymbolicVariable)this.getInternalVariables()[1]).setDomain(this.symbols);
	}

	@Override
	protected Constraint[] createInternalConstraints(Variable[] variables) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDomain(Domain d) {
		// TODO Auto-generated method stub
	}

	@Override
	public String toString() {
		String ret="";
		ret += this.getComponent()+"("+this.getID()+")" + "::<" + this.getInternalVariables()[1].toString() + ">U<" + this.getInternalVariables()[0].toString() + ">";
		if (this.getMarking() != null) ret += "/" + this.getMarking();
		return ret;
	}
	
	/**
	 * @return The {@link SymbolicVariable} representing the symbolic value of this {@link Activity}.
	 */
	public SymbolicVariable getSymbolicVariable() {
		return (SymbolicVariable)this.getInternalVariables()[1];
	}

	/**
	 * @return The {@link AllenInterval} representing the temporal value of this {@link Activity}.
	 */
	public AllenInterval getTemporalVariable() {
		return (AllenInterval)this.getInternalVariables()[0];
	}

	@Override
	public int compareTo(Variable arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	
}
