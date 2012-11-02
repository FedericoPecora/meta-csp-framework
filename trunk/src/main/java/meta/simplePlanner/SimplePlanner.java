package meta.simplePlanner;

import java.util.Vector;

import meta.simplePlanner.SimpleDomain.markings;
import multi.activity.Activity;
import multi.activity.ActivityNetworkSolver;
import multi.allenInterval.AllenIntervalConstraint;
import symbols.SymbolicValueConstraint;
import framework.Constraint;
import framework.ConstraintNetwork;
import framework.Variable;
import framework.VariablePrototype;
import framework.meta.MetaConstraintSolver;
import framework.meta.MetaVariable;


public class SimplePlanner extends MetaConstraintSolver {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4415954974488050840L;

	public SimplePlanner(long origin, long horizon, long animationTime) {
		// Through the constructor, the Scheduler is put in the "InternalSolver variable"
		super(new Class[] {AllenIntervalConstraint.class, SymbolicValueConstraint.class}, animationTime, new ActivityNetworkSolver(origin, horizon, 500));
		// Through the following line, the Scheduler is put in the "nextMetaConstraintSolver" belongin
		// to the MetaConstraintSolver class
	}
	
	@Override
	public void preBacktrack() { }

	@Override
	protected void retractResolverSub(ConstraintNetwork metaVariable, ConstraintNetwork metaValue) {
		ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)this.getConstraintSolvers()[0];
		Vector<Variable> activityToRemove = new Vector<Variable>();
		
		for (Variable v : metaValue.getVariables()) {
			if (!metaVariable.containsVariable(v)) {
				if (v instanceof VariablePrototype) {
					Variable vReal = metaValue.getSubstitution((VariablePrototype)v);
					if (vReal != null) {
						activityToRemove.add(vReal);
					}
				}
			}
		}

		SimpleDomain sd = (SimpleDomain)this.metaConstraints.elementAt(0);
		for (Variable v : activityToRemove) {
			for (SimpleReusableResource rr : sd.getCurrentReusableResourcesUsedByActivity((Activity)v)) {
				rr.removeUsage((Activity)v);
			}
		}
		
		groundSolver.removeVariables(activityToRemove.toArray(new Variable[activityToRemove.size()]));
	}

	@Override
	protected void addResolverSub(ConstraintNetwork currentProblematicConstraintNetwork, ConstraintNetwork possibleOperatorConstraintNetwork) {
		ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)this.getConstraintSolvers()[0];

		//Make real variables from variable prototypes
		for (Variable v :  possibleOperatorConstraintNetwork.getVariables()) {
			if (v instanceof VariablePrototype) {
				// 	Parameters for real instantiation: the first is the component itself, the second is
				//	the symbol of the Activity to be instantiated
				String component = (String)((VariablePrototype) v).getParameters()[0];
				String symbol = (String)((VariablePrototype) v).getParameters()[1];
				Activity tailActivity = (Activity)groundSolver.createVariable(component);
				tailActivity.setSymbolicDomain(symbol);
				tailActivity.setMarking(v.getMarking());
				possibleOperatorConstraintNetwork.addSubstitution((VariablePrototype)v, tailActivity);
			}
		}

		//Involve real variables in the constraints
		for (Constraint con : possibleOperatorConstraintNetwork.getConstraints()) {
			Constraint clonedConstraint = (Constraint)con.clone();  
			Variable[] oldScope = con.getScope();
			Variable[] newScope = new Variable[oldScope.length];
			for (int i = 0; i < oldScope.length; i++) {
				if (oldScope[i] instanceof VariablePrototype) newScope[i] = possibleOperatorConstraintNetwork.getSubstitution((VariablePrototype)oldScope[i]);
				else newScope[i] = oldScope[i];
			}
			clonedConstraint.setScope(newScope);
			possibleOperatorConstraintNetwork.removeConstraint(con);
			possibleOperatorConstraintNetwork.addConstraint(clonedConstraint);
		}
		
		//Set resource usage if necessary
		for (Variable v : possibleOperatorConstraintNetwork.getVariables()) {
			SimpleDomain sd = (SimpleDomain)this.metaConstraints.elementAt(0);
			for (SimpleReusableResource rr : sd.getCurrentReusableResourcesUsedByActivity(v)) {
				rr.setUsage((Activity)v);
			}
		}
	}

	@Override
	public void postBacktrack(MetaVariable mv) {
		if (mv.getMetaConstraint() instanceof SimpleDomain)
			for (Variable v : mv.getConstraintNetwork().getVariables()) v.setMarking(markings.UNJUSTIFIED);
	}

	@Override
	protected double getUpperBound() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void setUpperBound() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected double getLowerBound() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void setLowerBound() {
		// TODO Auto-generated method stub
		
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
