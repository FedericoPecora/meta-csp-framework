package org.metacsp.meta.simplePlanner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariableOrderingH;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.framework.meta.MetaVariable;
import org.metacsp.meta.symbolsAndTime.Schedulable;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.PowerSet;

import cern.colt.Arrays;

public class ProactivePlanningDomain extends SimpleDomain {

	private boolean triggered = false;
	private Activity[] oldInference = null;
	private long timeNow = -1;
	
	public void setOldInference(Activity[] oldInf) {
		this.oldInference = oldInf;
	}
	
	public ProactivePlanningDomain(int[] capacities, String[] resourceNames, String domainName) {
		super(capacities, resourceNames, domainName);
	}

	private static final long serialVersionUID = 5232380823036756902L;

	private static VariableOrderingH getVariableOrderingH() {
		//No variable ordering
		VariableOrderingH varOH = new VariableOrderingH() {
			@Override
			public int compare(ConstraintNetwork o1, ConstraintNetwork o2) { return 0; }
			@Override
			public void collectData(ConstraintNetwork[] allMetaVariables) { }
		};
		return varOH;
	}

	private static ValueOrderingH getValueOrderingH() {
		//Try to support most inferred activities first
		ValueOrderingH valOH = new ValueOrderingH() {
			@Override
			public int compare(ConstraintNetwork arg0, ConstraintNetwork arg1) {
				if (arg0.getAnnotation() != null && arg1.getAnnotation() != null) {
					if (arg0.getAnnotation() instanceof Integer && arg1.getAnnotation() instanceof Integer) {
						return (Integer)arg1.getAnnotation()-(Integer)arg0.getAnnotation(); 
					}
				}
				return arg1.getVariables().length - arg0.getVariables().length;
			}
		};
		return valOH;
	}
	

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
		for (ConstraintNetwork cn : ret) newRet.add(cn);
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
				//Add timeNow release to activity representing the metavariable
				Variable flaw = mv.getVariables()[0];
				if (!isContextVar(flaw.getComponent()) && !isSensor(flaw.getComponent())) {
					AllenIntervalConstraint release = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(timeNow,APSPSolver.INF));
					release.setFrom(flaw);
					release.setTo(flaw);
					System.out.println("Added TIMENOW constraint: " + release);
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
			if (oldInference != null) {
				for (Activity oldVar : oldInference) {
					if (oneGoal.getParameters()[0].equals(oldVar.getComponent())) {
						AllenIntervalConstraint before = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before);
						before.setFrom(oldVar);
						before.setTo(oneGoal);
						cn.addConstraint(before);
					}
				}
			}
			ret.add(cn);
		}
		if (ret.isEmpty()) return null;
		return ret.toArray(new ConstraintNetwork[ret.size()]);
	}

	public void updateTimeNow(long timeNow) {
		this.timeNow = timeNow;
	}
}
