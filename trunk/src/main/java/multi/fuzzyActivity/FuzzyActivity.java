package multi.fuzzyActivity;

import java.util.Vector;

import symbols.fuzzySymbols.FuzzySymbolicVariable;
import framework.Constraint;
import framework.ConstraintSolver;
import framework.Domain;
import framework.Variable;
import framework.multi.MultiVariable;
import fuzzyAllenInterval.SimpleAllenInterval;

public class FuzzyActivity extends MultiVariable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6700038298629079368L;
	//private String[] symbols;
	private SimpleAllenInterval temporalVariable;
	private FuzzySymbolicVariable fuzzySymbolicVariable; 
	//private boolean mask = true;//iran 
	private Vector<FuzzyActivity> dependencies;//iran
	private boolean isHypothesis = false;//iran
	
	protected FuzzyActivity(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers) {
		super(cs, id, internalSolvers);
		dependencies = new Vector<FuzzyActivity>();
	}
	
	public void setDependencies(Vector<FuzzyActivity> dependencies) {
		this.dependencies = dependencies;
	}
	
	public Vector<FuzzyActivity> getDependencies() {
		return dependencies;
	}
	
	public void setIsHypothesis(boolean isHypothesis) {
		this.isHypothesis = isHypothesis;
	}
	
	public boolean IsHypothesis()
	{
		return isHypothesis;
	}
	
	/*
	public void setSymbolicDomain(String... symbols) {
		this.symbols = symbols;
		fuzzySymbolicVariable.setDomain(this.symbols);
	}
	
	public void setPossibilityDegree(double... vals) throws PossibilityDegreeMismathcException{
		fuzzySymbolicVariable.setPossibilityDegree(vals);
	}
	*/
	
	public void setDomain(String[] symbols, double[] vals) {
		fuzzySymbolicVariable.setDomain(symbols, vals);
	}

	
	
	@Override
	protected Constraint[] createInternalConstraints(Variable[] variables) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Variable[] createInternalVariables() {
		SimpleAllenInterval temporalVariable = (SimpleAllenInterval)internalSolvers[0].createVariable();
		FuzzySymbolicVariable fuzzySymbolicVariable = (FuzzySymbolicVariable)internalSolvers[1].createVariable();
		this.fuzzySymbolicVariable = fuzzySymbolicVariable; 
		this.temporalVariable = temporalVariable;
		return new Variable[]{temporalVariable,fuzzySymbolicVariable};
	}

	@Override
	public void setDomain(Domain d) {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		return "<" + this.fuzzySymbolicVariable.toString() + ">U<" + this.temporalVariable.toString() + ">";
		//return "<" + this.fuzzySymbolicVariable.toString();
	}

	@Override
	public int compareTo(Variable arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

}
