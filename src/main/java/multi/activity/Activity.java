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
package multi.activity;

import multi.allenInterval.AllenInterval;
import multi.symbols.SymbolicVariable;
import framework.Constraint;
import framework.ConstraintSolver;
import framework.Domain;
import framework.Variable;
import framework.multi.MultiVariable;

public class Activity extends MultiVariable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4709760631961797060L;
	private String[] symbols;
//	private SymbolicVariable symbolicVariable;
//	private AllenInterval temporalVariable;
	
	
//	protected Activity(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers) {
//		super(cs, id, internalSolvers);
//	}

	public Activity(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers, Variable[] internalVars) {
		super(cs,id,internalSolvers,internalVars);
	}
	
	public void setSymbolicDomain(String... symbols) {
		this.symbols = symbols;
//		symbolicVariable.setDomain(this.symbols);
		((SymbolicVariable)this.getInternalVariables()[1]).setDomain(this.symbols);
	}

	@Override
	protected Constraint[] createInternalConstraints(Variable[] variables) {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	protected Variable[] createInternalVariables() {
//		AllenInterval temporalVariable = (AllenInterval)internalSolvers[0].createVariable();
//		SymbolicVariable symbolicVariable = (SymbolicVariable)internalSolvers[1].createVariable();
////		this.symbolicVariable = symbolicVariable;
////		this.temporalVariable = temporalVariable;
//		return new Variable[]{temporalVariable,symbolicVariable};
//	}

	@Override
	public void setDomain(Domain d) {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		String ret="";
//		ret+="\n<===================================>\n";
//		ret += this.getComponent() + "::<" + this.symbolicVariable.toString() + ">U<" + this.temporalVariable.toString() + ">";
		ret += this.getComponent() + "::<" + this.getInternalVariables()[1].toString() + ">U<" + this.getInternalVariables()[0].toString() + ">";
		if (this.getMarking() != null) ret += "/" + this.getMarking()+"\n";
//		ret+=("\tLLLLLL");
//		for(Variable v: this.variables){
//			ret+="\n"+v.toString();
//		}
//		ret+=("\n\tMMMMM");
//		ret+="\n<_____________________________________>\n"; 
		return ret;
	}
	
	/**
	 * @return The {@link SymbolicVariable} representing the symbolic value of this {@link Activity}.
	 */
	public SymbolicVariable getSymbolicVariable() {
		//return symbolicVariable;
		return (SymbolicVariable)this.getInternalVariables()[1];
	}

	/**
	 * @return The {@link AllenInterval} representing the temporal value of this {@link Activity}.
	 */
	public AllenInterval getTemporalVariable() {
		//return temporalVariable;
		return (AllenInterval)this.getInternalVariables()[0];
	}

	@Override
	public int compareTo(Variable arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	
}
