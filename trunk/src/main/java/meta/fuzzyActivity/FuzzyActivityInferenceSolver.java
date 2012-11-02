package meta.fuzzyActivity;

import multi.fuzzyActivity.FuzzyActivityNetworkSolver;
import symbols.SymbolicValueConstraint;
import framework.ConstraintNetwork;
import framework.meta.MetaConstraintSolver;
import framework.meta.MetaVariable;
import fuzzyAllenInterval.FuzzyAllenIntervalConstraint;

public class FuzzyActivityInferenceSolver extends MetaConstraintSolver{

	/**
	 * 
	 */
	private static final long serialVersionUID = -9129980934841620989L;
	private double upperBound = 0;
	private double lowerBound = -1;
	private double tmpLoweBound = -1;
	
	public FuzzyActivityInferenceSolver(long animationTime) {
		//super(new Class[] {AllenIntervalConstraint.class, SymbolicValueConstraint.class}, animationTime, new Scheduler(origin, horizon, 0));
		super(new Class[]{FuzzyAllenIntervalConstraint.class, SymbolicValueConstraint.class}, animationTime, new FuzzyActivityNetworkSolver());
		// TODO Auto-generated constructor stub
	}
	
		
	@Override
	public void preBacktrack() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postBacktrack(MetaVariable mv) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void retractResolverSub(ConstraintNetwork metaVariable, ConstraintNetwork metaValue) {
		//FuzzyActivityNetworkSolver groundSolver = (FuzzyActivityNetworkSolver)((FuzzyActivityDomain)this.domainFeatures.get(0)).getConstraintSolver();
	
//		Vector<Variable> toRemove = new Vector<Variable>();
//		for (Variable v : metaValue.getVariables()) 
//			if (!metaVariable.containsVariable(v))
//				toRemove.add(v);
		
		//((FuzzyActivityDomain)this.metaConstraints.get(0)).removeFromNetwork(metaVariable, toRemove);
		((FuzzyActivityDomain)this.metaConstraints.get(0)).setUnjustified(metaVariable);
	}
	


	@Override
	protected void addResolverSub(ConstraintNetwork metaVariable,
			ConstraintNetwork metaValue) {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected double getUpperBound() {
		// TODO Auto-generated method stub
		return this.upperBound;
	}


	@Override
	protected void setUpperBound() {
		
		this.upperBound = ((FuzzyActivityDomain)this.metaConstraints.get(0)).getConsitency();
		tmpLoweBound = upperBound;
		System.out.println("getupperbound: " + upperBound);
	}


	@Override
	protected double getLowerBound() {
		
		return this.lowerBound;
	}


	@Override
	protected void setLowerBound() {
		if(tmpLoweBound > lowerBound)
			this.lowerBound = tmpLoweBound;
		System.out.println("getLowebound: " + lowerBound);

	}


	@Override
	protected boolean hasConflictClause(ConstraintNetwork metaValue) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	protected void resetFalseClause() {
		// TODO Auto-generated method stub
		
	}

}
