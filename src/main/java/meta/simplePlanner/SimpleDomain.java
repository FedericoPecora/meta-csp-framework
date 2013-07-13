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
package meta.simplePlanner;

import java.util.HashMap;
import java.util.Vector;

import meta.symbolsAndTime.Schedulable;
import multi.activity.Activity;
import multi.activity.ActivityNetwork;
import multi.activity.ActivityNetworkSolver;
import multi.allenInterval.AllenIntervalConstraint;
import framework.Constraint;
import framework.ConstraintNetwork;
import framework.ValueOrderingH;
import framework.Variable;
import framework.VariableOrderingH;
import framework.VariablePrototype;
import framework.meta.MetaConstraint;
import framework.meta.MetaVariable;

public class SimpleDomain extends MetaConstraint {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5143516447467774523L;
	private Vector<SimpleOperator> operators;
	private HashMap<String,SimpleReusableResource> resourcesMap;
	private HashMap<SimpleReusableResource,HashMap<Activity,Integer>> currentResourceUtilizers;

	private String name;
	
	public enum markings {UNJUSTIFIED, JUSTIFIED, DIRTY, STATIC, IGNORE, PLANNED, UNPLANNED, PERMANENT};
	
	public Schedulable[] getSchedulingMetaConstraints() {
		return currentResourceUtilizers.keySet().toArray(new Schedulable[currentResourceUtilizers.keySet().size()]);
	}

	public SimpleDomain(int[] capacities, String[] resourceNames, String domainName) {
		super(null, null);
		this.name = domainName;
		currentResourceUtilizers = new HashMap<SimpleReusableResource,HashMap<Activity,Integer>>();
		resourcesMap = new HashMap<String, SimpleReusableResource>();
		operators = new Vector<SimpleOperator>();
				
		for (int i = 0; i < capacities.length; i++) {
			//Most critical conflict is the one with most activities 
			VariableOrderingH varOH = new VariableOrderingH() {
				@Override
				public int compare(ConstraintNetwork arg0, ConstraintNetwork arg1) {
					return arg1.getVariables().length - arg0.getVariables().length;
				}
				@Override
				public void collectData(ConstraintNetwork[] allMetaVariables) { }
			};
			// no value ordering
			ValueOrderingH valOH = new ValueOrderingH() {
				@Override
				public int compare(ConstraintNetwork o1, ConstraintNetwork o2) { return 0; }
			};
			resourcesMap.put(resourceNames[i], new SimpleReusableResource(varOH, valOH, capacities[i], this, resourceNames[i]));
		}
		
		// for every RRS just created, put it coupled with a vector of variables
		for (SimpleReusableResource rr : resourcesMap.values()) currentResourceUtilizers.put(rr,new HashMap<Activity, Integer>());
	}
	
	public void addOperator(SimpleOperator r) {
		operators.add(r);
	}
	
	public SimpleOperator[] getOperators() {
		return operators.toArray(new SimpleOperator[operators.size()]);
	}		

	@Override
	public ConstraintNetwork[] getMetaVariables() {
		ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)this.metaCS.getConstraintSolvers()[0];
		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		// for every variable that is marked as UNJUSTIFIED an ActivityNetwork is built
		// this becomes a task
		for (Variable task : groundSolver.getVariables()) {
			if (task.getMarking().equals(markings.UNJUSTIFIED)) {
				ActivityNetwork nw = new ActivityNetwork(null);
				nw.addVariable(task);
				ret.add(nw);
			}
		}
		return ret.toArray(new ConstraintNetwork[ret.size()]);
	}

	private ConstraintNetwork expandOperator(SimpleOperator possibleOperator, Activity problematicActivity) {		
		ActivityNetwork activityNetworkToReturn = new ActivityNetwork(null);
		ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)this.metaCS.getConstraintSolvers()[0];
		
		String possibleOperatorHead = possibleOperator.getHead();
		String possibleOperatorSymbol = possibleOperatorHead.substring(possibleOperatorHead.indexOf("::")+2, possibleOperatorHead.length());
		String possibleOperatorComponent = possibleOperatorHead.substring(0, possibleOperatorHead.indexOf("::"));
		
		Vector<Variable> operatorTailActivitiesToInsert = new Vector<Variable>();
		
		if (possibleOperator.getRequirementActivities() != null) {
			for (String possibleOperatorTail : possibleOperator.getRequirementActivities()) {
				String possibleOperatorTailComponent = possibleOperatorTail.substring(0, possibleOperatorTail.indexOf("::"));
				String possibleOperatorTailSymbol = possibleOperatorTail.substring(possibleOperatorTail.indexOf("::")+2, possibleOperatorTail.length());
				if (possibleOperatorTailComponent.equals(possibleOperatorComponent) && possibleOperatorTailSymbol.equals(possibleOperatorSymbol)) {
					operatorTailActivitiesToInsert.add(problematicActivity);
				}
				else {
					VariablePrototype tailActivity = new VariablePrototype(groundSolver, possibleOperatorTailComponent, possibleOperatorTailSymbol);
					tailActivity.setMarking(markings.UNJUSTIFIED);
					operatorTailActivitiesToInsert.add(tailActivity);
				}
			}

			Vector<AllenIntervalConstraint> allenIntervalConstraintsToAdd = new Vector<AllenIntervalConstraint>();

			for (int i = 0; i < possibleOperator.getRequirementConstraints().length; i++) {
				AllenIntervalConstraint con = (AllenIntervalConstraint)possibleOperator.getRequirementConstraints()[i].clone();
				con.setFrom(problematicActivity);
				con.setTo(operatorTailActivitiesToInsert.elementAt(i));
				allenIntervalConstraintsToAdd.add(con);
			}
			for (AllenIntervalConstraint con : allenIntervalConstraintsToAdd) activityNetworkToReturn.addConstraint(con);
			
			Vector<AllenIntervalConstraint> toAddExtra = new Vector<AllenIntervalConstraint>();
			for (int i = 0; i < operatorTailActivitiesToInsert.size(); i++) {
				AllenIntervalConstraint[][] ec = possibleOperator.getExtraConstraints();
				if (ec != null) {
					AllenIntervalConstraint[] con = ec[i];
					for (int j = 0; j < con.length; j++) {
						if (con[j] != null) {
							AllenIntervalConstraint newCon = (AllenIntervalConstraint) con[j].clone();
							if (i == 0) newCon.setFrom(problematicActivity);
							else newCon.setFrom(operatorTailActivitiesToInsert.elementAt(i-1));
							if (j == 0) newCon.setTo(problematicActivity);
							else newCon.setTo(operatorTailActivitiesToInsert.elementAt(j-1));
							toAddExtra.add(newCon);
						}
					}
				}
			}

			if (!toAddExtra.isEmpty()) {
				for (AllenIntervalConstraint con : toAddExtra) activityNetworkToReturn.addConstraint(con);
			}
		}
		else if (possibleOperator.getExtraConstraints()[0][0] != null) {
			AllenIntervalConstraint ec = possibleOperator.getExtraConstraints()[0][0];
			AllenIntervalConstraint newCon = (AllenIntervalConstraint) ec.clone();
			newCon.setFrom(problematicActivity);
			newCon.setTo(problematicActivity);
			activityNetworkToReturn.addConstraint(newCon);
		}
		
		if (possibleOperator.getUsages() != null) {
			String resource = possibleOperatorSymbol.substring(possibleOperatorSymbol.indexOf("(")+1,possibleOperatorSymbol.indexOf(")"));
			String[] resourceArray = resource.split(",");
			if (!resource.equals("")) {
				for (int i = 0; i < resourceArray.length; i++) {
					String oneResource = resourceArray[i];
					HashMap<Activity, Integer> utilizers = currentResourceUtilizers.get(resourcesMap.get(oneResource));
					utilizers.put(problematicActivity, possibleOperator.getUsages()[i]);
					activityNetworkToReturn.addVariable(problematicActivity);
				}
			}
		}
		return activityNetworkToReturn;						
	}
	
	
	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable, int initialTime) {
		return getMetaValues(metaVariable);
	}
	
	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable) {
		Vector<ConstraintNetwork> retPossibleConstraintNetworks = new Vector<ConstraintNetwork>();
		ConstraintNetwork problematicNetwork = metaVariable.getConstraintNetwork();
		Activity problematicActivity = (Activity)problematicNetwork.getVariables()[0]; 

		for (SimpleOperator r : operators) {
			String problematicActivitySymbolicDomain = problematicActivity.getSymbolicVariable().getSymbols()[0];
			String operatorHead = r.getHead();
			String opeatorHeadComponent = operatorHead.substring(0, operatorHead.indexOf("::"));
			String operatorHeadSymbol = operatorHead.substring(operatorHead.indexOf("::")+2, operatorHead.length());
			if (opeatorHeadComponent.equals(problematicActivity.getComponent())) {
				if (problematicActivitySymbolicDomain.contains(operatorHeadSymbol)) {
					ConstraintNetwork newResolver = expandOperator(r,problematicActivity);
					retPossibleConstraintNetworks.add(newResolver);
				}
			}
		}
		
		if (!retPossibleConstraintNetworks.isEmpty()) return retPossibleConstraintNetworks.toArray(new ConstraintNetwork[retPossibleConstraintNetworks.size()]);
		ActivityNetwork nullActivityNetwork = new ActivityNetwork(null);
		return new ConstraintNetwork[] {nullActivityNetwork};
	}

	@Override
	public void markResolvedSub(MetaVariable con, ConstraintNetwork metaValue) {
		con.getConstraintNetwork().getVariables()[0].setMarking(markings.JUSTIFIED);
	}

	@Override
	public void draw(ConstraintNetwork network) {
		// TODO Auto-generated method stub	
	}
	
	public HashMap<String, SimpleReusableResource> getResources() {
		return resourcesMap;
	}
	// Given a variable act, it returns all the RubReusRes that are currently exploited by the variable
	public SimpleReusableResource[] getCurrentReusableResourcesUsedByActivity(Variable act) {
		Vector<SimpleReusableResource> ret = new Vector<SimpleReusableResource>();
		for (SimpleReusableResource rr : currentResourceUtilizers.keySet()) {
			if (currentResourceUtilizers.get(rr).containsKey(act)) 
				ret.add(rr);
		}
		return ret.toArray(new SimpleReusableResource[ret.size()]);
	}

	public int getResourceUsageLevel(SimpleReusableResource rr, Variable act) {
		return currentResourceUtilizers.get(rr).get(act);
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "SimpleDomain " + this.name;
	}

	@Override
	public String getEdgeLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEquivalent(Constraint c) {
		// TODO Auto-generated method stub
		return false;
	}

}
