package meta.fuzzyActivity;

import java.util.Vector;

import multi.fuzzyActivity.FuzzyActivityNetworkSolver;
import symbols.SymbolicValueConstraint;
import framework.Constraint;
import framework.ConstraintNetwork;
import framework.meta.MetaConstraintSolver;
import framework.meta.MetaVariable;
import fuzzyAllenInterval.FuzzyAllenIntervalConstraint;

public class FuzzyActivityMetaSolver extends MetaConstraintSolver{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3342951089757068845L;
	private double upperBound = 0;
	private double lowerBound = 0;
	private double tmpLoweBound = 0;
	private ConstraintNetwork cn;
	private ConstraintNetwork optCn;
	private double valueConsistency = 0;
	private double temporalConsistency = 0;
	private double vcTmp = 0;
	private double tcTmp = 0;
	public FuzzyActivityMetaSolver(long animationTime) {
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
		cn = ((FuzzyActivityDomain)this.metaConstraints.get(0)).getConstraintNetwork();
		vcTmp = ((FuzzyActivityDomain)this.metaConstraints.get(0)).getValueConsistency();
		tcTmp = ((FuzzyActivityDomain)this.metaConstraints.get(0)).getTemporalConsistency();
		tmpLoweBound = upperBound;
	}


	@Override
	protected double getLowerBound() {
		return this.lowerBound;
	}


	@Override
	protected void setLowerBound() {
		if(tmpLoweBound > lowerBound){
			this.lowerBound = tmpLoweBound;
			optCn = cn;
			valueConsistency = vcTmp;
			temporalConsistency = tcTmp;
			System.out.println("getLowebound: " + lowerBound);
			//System.out.println("optCn: " + optCn);
		}
		System.out.println("...........................................................");
	}

	@Override
	protected boolean hasConflictClause(ConstraintNetwork metaValue){
		Vector<Constraint> cons = new Vector<Constraint>();
		cons = ((FuzzyActivityDomain)this.metaConstraints.get(0)).getFalseClause();
		for (int i = 0; i < metaValue.getConstraints().length; i++) {
			for (int j = 0; j < cons.size(); j++) {
				if(isAFalseClause(metaValue.getConstraints()[i], cons.get(j)))
					return true;
			}
		}
		return false;
	}
	
	@Override
	protected void resetFalseClause(){

		((FuzzyActivityDomain)this.metaConstraints.get(0)).resetFalseClause();

	}
	

	private boolean isAFalseClause(Constraint c1, Constraint c2) {
		if((c1.getScope()[0].getID() == c2.getScope()[0].getID()) && (c1.getScope()[1].getID() == c2.getScope()[1].getID()))
			return true;
		if((c1.getScope()[0].getID() == c2.getScope()[1].getID()) && (c1.getScope()[0].getID() == c2.getScope()[1].getID()))
			return true;
		return false;
	}


	public ConstraintNetwork getOptimalConstraint() {
		return optCn;
		
	}
	
	public String getMostLiklyOccuredActivities(){
		
		String str = ((FuzzyActivityDomain)this.metaConstraints.get(0)).getOptimalHypothesis(optCn, valueConsistency, temporalConsistency);	
		return str;
	}
}
