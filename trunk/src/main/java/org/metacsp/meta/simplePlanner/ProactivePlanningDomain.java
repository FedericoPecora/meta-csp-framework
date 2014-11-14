package org.metacsp.meta.simplePlanner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariableOrderingH;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.framework.meta.MetaVariable;
import org.metacsp.multi.activity.SymbolicVariableActivity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;

public class ProactivePlanningDomain extends SimpleDomain {

	private boolean triggered = false;
	private HashMap<String,SymbolicVariableActivity> oldInferences = new HashMap<String,SymbolicVariableActivity>();
	private long timeNow = -1;
	
	public void setOldInference(String component, SymbolicVariableActivity oldInf) {
		oldInferences.put(component,oldInf);
	}
	
	//Should be created with SimpleDomain's factory method
	protected ProactivePlanningDomain(int[] capacities, String[] resourceNames, String domainName, String everything) {
		super(capacities, resourceNames, domainName, everything);
	}

	private static final long serialVersionUID = 5232380823036756902L;
	
	private VariablePrototype[] generateGoals() {
		ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)this.metaCS.getConstraintSolvers()[0];
		SimpleOperator[] ops = this.getOperators();
		HashSet<VariablePrototype> vars = new HashSet<VariablePrototype>();
		for (SimpleOperator op : ops) {
			String head = op.getHead();
			String headComponent = head.substring(0,head.indexOf("::"));
			String headValue = head.substring(head.indexOf("::")+2);
			if (this.isContextVar(headComponent)) {
				VariablePrototype toInfer = new VariablePrototype(groundSolver, headComponent, headValue, "Inference");
				toInfer.setMarking(markings.UNJUSTIFIED);
				vars.add(toInfer);
			}
		}
		return vars.toArray(new VariablePrototype[vars.size()]);
	}

	public void resetContextInference() { this.triggered = false; }

	public ConstraintNetwork[] getMetaVariables() {
		//Add the normal metavariables for planning (UNJUSTIFIED activities)
		ConstraintNetwork[] ret = super.getMetaVariables();
		Vector<ConstraintNetwork> newRet = new Vector<ConstraintNetwork>();
		if (ret != null) for (ConstraintNetwork cn : ret) newRet.add(cn);
		if (!triggered) {
			triggered = true;
			//Add a null constraint network to signal that context inference should be done
			ConstraintNetwork nullConstraintNetwork = new ConstraintNetwork(null);
			newRet.add(nullConstraintNetwork);
		}
		return newRet.toArray(new ConstraintNetwork[newRet.size()]);
	}
	
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable) {
		ConstraintNetwork mv = metaVariable.getConstraintNetwork();

		//If this is not context inference, get metavalues as usual 
		if (mv.getConstraints().length != 0 || mv.getVariables().length != 0) {
			ConstraintNetwork[] ret = super.getMetaValues(metaVariable);
			if (ret != null && ret.length > 0) {
				//If this is an actuator, add timeNow release to activity representing the metavariable
				Variable flaw = mv.getVariables()[0];
				if (isActuator(flaw.getComponent())) {
					AllenIntervalConstraint release = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(timeNow,APSPSolver.INF));
					release.setFrom(flaw);
					release.setTo(flaw);
					for (ConstraintNetwork cn : ret) cn.addConstraint(release);
				}
			}
			return ret;
		}
		
		//We have a context inference metavariable - let's generate all possible worlds
		VariablePrototype[] possibleGoals = generateGoals();
		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		for (VariablePrototype oneGoal : possibleGoals) {
			ConstraintNetwork cn = new ConstraintNetwork(null);
			cn.addVariable(oneGoal);
			SymbolicVariableActivity oldInf = oldInferences.get(oneGoal.getParameters()[0]);
			boolean skip = false;
			//Do not re-infer the last thing that was inferred
			//(prevents from having to "model" impossibility to infer something continuously as temporal constraints in the domain)
			if (oldInf != null) {
				if (oldInf.getSymbolicVariable().getSymbols()[0].equals(oneGoal.getParameters()[1])) {
					skip = true;
					logger.finest("Skipping " + oneGoal.getParameters()[1] + " because of " + oldInf);
				}
				else {
					AllenIntervalConstraint before = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before);
					before.setFrom(oldInf);
					before.setTo(oneGoal);
					cn.addConstraint(before);
				}
			}
			if (!skip) ret.add(cn);
		}
		if (ret.isEmpty()) return null;
		return ret.toArray(new ConstraintNetwork[ret.size()]);
	}

	public void updateTimeNow(long timeNow) {
		this.timeNow = timeNow;
	}
	
}
