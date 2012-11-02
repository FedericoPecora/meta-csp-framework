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
