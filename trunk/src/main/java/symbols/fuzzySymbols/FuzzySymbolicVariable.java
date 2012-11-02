package symbols.fuzzySymbols;

import java.util.HashMap;

import framework.ConstraintSolver;
import framework.Domain;
import framework.Variable;

public class FuzzySymbolicVariable extends Variable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6872340540095116373L;
	private FuzzySymbolicDomain dom;
	private FuzzySymbolicDomain backupDom;
	
	protected FuzzySymbolicVariable(ConstraintSolver cs, int id) {
		super(cs, id);
	}

	@Override
	public Domain getDomain() {
		return this.dom;
	}
	
	@Override
	public void setDomain(Domain d) {
		if (d instanceof FuzzySymbolicDomain) {
			this.dom = (FuzzySymbolicDomain)d;
			this.backupDom = (FuzzySymbolicDomain)((FuzzySymbolicDomain)d).clone();
		}
	}
	
	public void setDomain(String[] symbols, double[] possibilities) {
		this.dom = new FuzzySymbolicDomain(this,symbols,possibilities);
		this.backupDom = (FuzzySymbolicDomain)dom.clone();
	}

	public void setDomain(FuzzySymbolicDomain s) {
		this.dom = s;
		this.backupDom = (FuzzySymbolicDomain)dom.clone();
	}
	
	
	public HashMap<String, Double> getSymbolsAndPossibilities() {
		return dom.getSymbolsAndPossibilities();
	}
	

	public String toString() {
		//return "Variable " + this.getID() + ": " + this.dom.toString();
		return this.getClass().getSimpleName() + " " + this.id + " " + this.getDomain();
	}

	@Override
	public int compareTo(Variable o) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void resetDomain() {
		if (this.backupDom != null) this.dom = (FuzzySymbolicDomain)backupDom.clone();
	}


}
