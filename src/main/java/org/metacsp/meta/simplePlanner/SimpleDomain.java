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
package org.metacsp.meta.simplePlanner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import org.metacsp.meta.simplePlanner.SimpleOperator.ReservedWord;
import org.metacsp.meta.symbolsAndTime.Schedulable;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint.Type;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariableOrderingH;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.framework.meta.MetaVariable;

import cern.colt.Arrays;

public class SimpleDomain extends MetaConstraint {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5143516447467774523L;
	private Vector<SimpleOperator> operators;
	private String[] resourceNames;
	private HashMap<String,SimpleReusableResource> resourcesMap;
	private HashMap<SimpleReusableResource,HashMap<Activity,Integer>> currentResourceUtilizers;

	private String name;
	
	private Vector<String> sensors = new Vector<String>();
	private Vector<String> contextVars = new Vector<String>();
	
	public enum markings {UNJUSTIFIED, JUSTIFIED, DIRTY, STATIC, IGNORE, PLANNED, UNPLANNED, PERMANENT};
	
	public Schedulable[] getSchedulingMetaConstraints() {
		return currentResourceUtilizers.keySet().toArray(new Schedulable[currentResourceUtilizers.keySet().size()]);
	}

	public SimpleDomain(int[] capacities, String[] resourceNames, String domainName) {
		super(null, null);
		this.name = domainName;
		this.resourceNames = resourceNames;
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
		
		// for every RRS just created, couple it with a vector of variables
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
		// for every variable that is marked as UNJUSTIFIED a ConstraintNetwork is built
		for (Variable task : groundSolver.getVariables()) {
			if (task.getMarking().equals(markings.UNJUSTIFIED)) {
				ConstraintNetwork nw = new ConstraintNetwork(null);
				nw.addVariable(task);
				ret.add(nw);
			}
		}
		return ret.toArray(new ConstraintNetwork[ret.size()]);
	}

	private ConstraintNetwork expandOperator(SimpleOperator possibleOperator, Activity problematicActivity) {		
		ConstraintNetwork activityNetworkToReturn = new ConstraintNetwork(null);
		ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)this.metaCS.getConstraintSolvers()[0];
		
		String possibleOperatorHead = possibleOperator.getHead();
		String possibleOperatorHeadSymbol = possibleOperatorHead.substring(possibleOperatorHead.indexOf("::")+2, possibleOperatorHead.length());
		String possibleOperatorHeadComponent = possibleOperatorHead.substring(0, possibleOperatorHead.indexOf("::"));
		
		Vector<Variable> operatorTailActivitiesToInsert = new Vector<Variable>();
		
		if (possibleOperator.getRequirementActivities() != null) {
			for (String possibleOperatorTail : possibleOperator.getRequirementActivities()) {
				String possibleOperatorTailComponent = possibleOperatorTail.substring(0, possibleOperatorTail.indexOf("::"));
				String possibleOperatorTailSymbol = possibleOperatorTail.substring(possibleOperatorTail.indexOf("::")+2, possibleOperatorTail.length());
				if (possibleOperatorTailComponent.equals(possibleOperatorHeadComponent) && possibleOperatorTailSymbol.equals(possibleOperatorHeadSymbol)) {
					operatorTailActivitiesToInsert.add(problematicActivity);
				}
				else {
					VariablePrototype tailActivity = new VariablePrototype(groundSolver, possibleOperatorTailComponent, possibleOperatorTailSymbol);
					if (possibleOperator instanceof PlanningOperator) {
						if (((PlanningOperator)possibleOperator).isEffect(possibleOperatorTail)) {
							tailActivity.setMarking(markings.JUSTIFIED);
						}
						else {
							tailActivity.setMarking(markings.UNJUSTIFIED);
						}
					}
					else {
						tailActivity.setMarking(markings.UNJUSTIFIED);
					}
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
		
		int[] usages = possibleOperator.getUsages();
		if (usages != null) {
			for (int i = 0; i < usages.length; i++) {
				if (usages[i] != 0) {
					HashMap<Activity, Integer> utilizers = currentResourceUtilizers.get(resourcesMap.get(resourceNames[i]));
					utilizers.put(problematicActivity, usages[i]);
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

	public void addSensor(String sensor) {
		this.sensors.add(sensor);
	}

	public void addContextVar(String cv) {
		this.contextVars.add(cv);
	}

	public boolean isSensor(String component) {
		if (sensors.contains(component)) return true;
		return false;
	}

	public boolean isContextVar(String component) {
		if (contextVars.contains(component)) return true;
		return false;
	}
	
	private ConstraintNetwork[] getUnifications(Activity activity) {
		ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)this.metaCS.getConstraintSolvers()[0];
		Variable[] acts = groundSolver.getVariables();
		Vector<Activity> possibleUnifications = new Vector<Activity>();
		Vector<ConstraintNetwork> unifications = new Vector<ConstraintNetwork>();
		for (Variable var : acts) {
			if (!var.equals(activity)) {
				Activity act = (Activity)var;
				String problematicActivitySymbolicDomain = activity.getSymbolicVariable().getSymbols()[0];
				if (act.getComponent().equals(activity.getComponent())) {
					String[] actSymbols = act.getSymbolicVariable().getSymbols();
					for (String symbol : actSymbols) {
						if (problematicActivitySymbolicDomain.contains(symbol)) {
							if (act.getMarking().equals(markings.JUSTIFIED)) {
								possibleUnifications.add(act);
							}
							break;
						}
					}
				}
			}
		}
		for (Activity act : possibleUnifications) {
			AllenIntervalConstraint equals = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals);
			equals.setFrom(activity);
			equals.setTo(act);
			ConstraintNetwork oneUnification = new ConstraintNetwork(null);
			oneUnification.addConstraint(equals);
			unifications.add(oneUnification);
		}
		if (unifications.isEmpty()) return null;
		return unifications.toArray(new ConstraintNetwork[unifications.size()]);		
	}

	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable) {
		Vector<ConstraintNetwork> retPossibleConstraintNetworks = new Vector<ConstraintNetwork>();
		ConstraintNetwork problematicNetwork = metaVariable.getConstraintNetwork();
		Activity problematicActivity = (Activity)problematicNetwork.getVariables()[0]; 

		if (isSensor(problematicActivity.getComponent())) {
			return this.getUnifications(problematicActivity);
		}
		
		if (isContextVar(problematicActivity.getComponent())) {
			ConstraintNetwork[] unifications = getUnifications(problematicActivity);
			if (unifications != null) {
				for (ConstraintNetwork oneUnification : unifications) {
					retPossibleConstraintNetworks.add(oneUnification);
					oneUnification.setAnnotation(2);
				}
			}
		}
		
		for (SimpleOperator r : operators) {
			String problematicActivitySymbolicDomain = problematicActivity.getSymbolicVariable().getSymbols()[0];
			String operatorHead = r.getHead();
			String opeatorHeadComponent = operatorHead.substring(0, operatorHead.indexOf("::"));
			String operatorHeadSymbol = operatorHead.substring(operatorHead.indexOf("::")+2, operatorHead.length());
			if (opeatorHeadComponent.equals(problematicActivity.getComponent())) {
				if (problematicActivitySymbolicDomain.contains(operatorHeadSymbol)) {
					ConstraintNetwork newResolver = expandOperator(r,problematicActivity);
					newResolver.setAnnotation(1);
					retPossibleConstraintNetworks.add(newResolver);
				}
			}
			if (r instanceof PlanningOperator) {
				System.out.println("Using oprerator " + r);
				for (String reqState : r.getRequirementActivities()) {
					String operatorEffect = reqState;
					String opeatorEffectComponent = operatorEffect.substring(0, operatorEffect.indexOf("::"));
					String operatorEffectSymbol = operatorEffect.substring(operatorEffect.indexOf("::")+2, operatorEffect.length());
					System.out.println("REQSTATE: " + reqState + " <-?-> ");
					if (((PlanningOperator)r).isEffect(reqState)) {
						System.out.println("Check1");
						if (opeatorEffectComponent.equals(problematicActivity.getComponent())) {
							System.out.println("Check2");
							if (problematicActivitySymbolicDomain.contains(operatorEffectSymbol)) {
								System.out.println("Check3");
								ConstraintNetwork newResolver = expandOperator(r,problematicActivity);
								newResolver.setAnnotation(1);
								retPossibleConstraintNetworks.add(newResolver);								
							}
						}
					}
				}
			}
		}
		
		if (!retPossibleConstraintNetworks.isEmpty()) return retPossibleConstraintNetworks.toArray(new ConstraintNetwork[retPossibleConstraintNetworks.size()]);
		ConstraintNetwork nullActivityNetwork = new ConstraintNetwork(null);
		return new ConstraintNetwork[] {nullActivityNetwork};
	}

	@Override
	public void markResolvedSub(MetaVariable con, ConstraintNetwork metaValue) {
		if (con.getConstraintNetwork().getVariables().length != 0)
			con.getConstraintNetwork().getVariables()[0].setMarking(markings.JUSTIFIED);
	}

	@Override
	public void draw(ConstraintNetwork network) {
		// TODO Auto-generated method stub	
	}
	
	public HashMap<String, SimpleReusableResource> getResources() {
		return resourcesMap;
	}

	// Given a variable act, it returns all the resources that are currently exploited by the variable
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
		String ret = this.getClass().getSimpleName() + " " + this.name;
//		ret += "\nResources:\n";
//		for (SimpleReusableResource rr : resourcesMap.values())
//			ret += "  " + rr + "\n";
//		for (SimpleOperator op : operators) {
//			ret += "--- Operator:\n";
//			ret += op + "\n";
//		}
		return ret;
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
	
	/**
	 * Creates a {@link SimpleOperator} from a textual specification (used by the
	 * domain parser {@link parseDomain}.
	 * @param textualSpecification A textual specification of an operator
	 * @return a {@link SimpleOperator} build according to the textual specification.
	 */
	public static SimpleOperator parseOperator(String textualSpecification, String[] resources) {
		HashMap<String,String> requiredStates = new HashMap<String, String>();
		String head = null;
		Vector<AllenIntervalConstraint> constraints = new Vector<AllenIntervalConstraint>();
		Vector<String> froms = new Vector<String>();
		Vector<String> tos = new Vector<String>();
		String[] args = null;
		int[] resourceRequirements = new int[resources.length];
		HashMap<String,Boolean> effects = new HashMap<String, Boolean>();
		boolean planningOp = false;

		String[] operatorType = parseKeyword("PlanningOperator", textualSpecification);
		if (operatorType.length == 0) {
			operatorType = parseKeyword("SimpleOperator", textualSpecification);
			planningOp = false;
		}
		else planningOp = true;
		
		String[] headElement = parseKeyword("Head", textualSpecification);
		head = headElement[0].trim();

		String[] requiredStateElements = parseKeyword("RequiredState", textualSpecification);
		for (String reqElement : requiredStateElements) {
			String reqKey = reqElement.substring(0,reqElement.indexOf(" ")).trim();
			String reqState = reqElement.substring(reqElement.indexOf(" ")).trim();
			requiredStates.put(reqKey, reqState);
			effects.put(reqKey,false);
		}

		String[] achievedStateElements = parseKeyword("AchievedState", textualSpecification);
		for (String achElement : achievedStateElements) {
			String achKey = achElement.substring(0,achElement.indexOf(" ")).trim();
			String achState = achElement.substring(achElement.indexOf(" ")).trim();
			requiredStates.put(achKey, achState);
			effects.put(achKey,true);
		}
		
		String[] constraintElements = parseKeyword("Constraint", textualSpecification);
		for (String conElement : constraintElements) {
			String constraintName = null;
			Vector<Bounds> bounds = null;
			if (conElement.contains("[")) {
				constraintName = conElement.substring(0,conElement.indexOf("[")).trim();
				String boundsString = conElement.substring(conElement.indexOf("["),conElement.indexOf("]")+1);
				String[] splitBounds = boundsString.split("\\[");
				bounds = new Vector<Bounds>();
				for (String oneBound : splitBounds) {
					if (!oneBound.trim().equals("")) {
						String lbString = oneBound.substring(oneBound.indexOf("[")+1,oneBound.indexOf(",")).trim();
						String ubString = oneBound.substring(oneBound.indexOf(",")+1,oneBound.indexOf("]")).trim();
						long lb, ub;
						if (lbString.equals("INF")) lb = org.metacsp.time.APSPSolver.INF;
						else lb = Long.parseLong(lbString);
						if (ubString.equals("INF")) ub = org.metacsp.time.APSPSolver.INF;
						else ub = Long.parseLong(ubString);
						bounds.add(new Bounds(lb,ub));
					}
				}
			}
			else {
				constraintName = conElement.substring(0,conElement.indexOf("(")).trim();
			}
			String from = null;
			String to = null;
			if (constraintName.equals("Duration")) {
				from = conElement.substring(conElement.indexOf("(")+1, conElement.indexOf(")")).trim();
				to = from;
			}
			else {
				from = conElement.substring(conElement.indexOf("(")+1, conElement.indexOf(",")).trim();
				to = conElement.substring(conElement.indexOf(",")+1, conElement.indexOf(")")).trim();
			}

			AllenIntervalConstraint con = null;
			if (bounds != null) con = new AllenIntervalConstraint(AllenIntervalConstraint.Type.valueOf(constraintName),bounds.toArray(new Bounds[bounds.size()]));
			else con = new AllenIntervalConstraint(AllenIntervalConstraint.Type.valueOf(constraintName));
			constraints.add(con);
			froms.add(from);
			tos.add(to);
		}
		
		String[] resourceElements = parseKeyword("RequiredResource", textualSpecification);
		for (String resElement : resourceElements) {
			String requiredResource = resElement.substring(0,resElement.indexOf("(")).trim();
			int requiredAmount = Integer.parseInt(resElement.substring(resElement.indexOf("(")+1,resElement.indexOf(")")).trim());
			for (int k = 0; k < resources.length; k++) {
				if (resources[k].equals(requiredResource)) {
					resourceRequirements[k] = requiredAmount;
				}
			}

		}
		
//		String[] split = textualSpecification.split("\\(");
//		boolean inConstraint = false;
//		boolean headInConstraint = false;
//		for (int i = 0; i < split.length; i++) {
//			String oneElement = split[i];
//			if (oneElement != null) {
//				if (oneElement.trim().equals("")) split[i] = null;
//				else {
//					split[i] = "(" + split[i];
//					boolean found = false;
//					for (SimpleOperator.ReservedWord reservedWord : SimpleOperator.ReservedWord.values()) {
//						if (oneElement.contains(reservedWord.toString())) {
//							if (reservedWord.equals(SimpleOperator.ReservedWord.Constraint)) {
//								inConstraint = true;
//							}
//							found = true;
//							if (reservedWord.equals(SimpleOperator.ReservedWord.Head) && inConstraint) {
//								headInConstraint = true;
//								inConstraint = false;
//							}
//							break;
//						}				
//					}
//					if (!found || headInConstraint) {
//						//join two split elements
//						split[i-1] = split[i-1] + split[i];
//						split[i] = null;
//						if (headInConstraint) headInConstraint = false;
//					}	
//				}
//			}
//		}
		
//		for (int i = 0; i < split.length; i++) {
//			String oneElement = split[i];
//			if (oneElement != null) {
//				SimpleOperator.ReservedWord rv = null;
//				for (SimpleOperator.ReservedWord reservedWord : SimpleOperator.ReservedWord.values()) {
//					if (oneElement.substring(oneElement.indexOf("(")+1).trim().startsWith(reservedWord.toString())) {
//						rv = reservedWord;
//						break;
//					}				
//				}
//				if (rv.equals(SimpleOperator.ReservedWord.SimpleOperator)) {
//					//do nothing
//				}
//				else if (rv.equals(SimpleOperator.ReservedWord.PlanningOperator)) {
//					planningOp = true;
//				}
//				else if (rv.equals(SimpleOperator.ReservedWord.Head)) {
//					head = oneElement.substring(oneElement.indexOf("Head")+4,oneElement.lastIndexOf(")")).trim();
//					String argString = head.substring(head.indexOf("(")+1,head.indexOf(")"));
//					if (!argString.trim().equals("")) {
//						args = argString.split(",");
//						//resources = argString.split(",");
//						//resourceRequirements = new int[resources.length];
//					}
//				}
//				else if (rv.equals(SimpleOperator.ReservedWord.RequiredState)) {
//					String reqKey = oneElement.substring(oneElement.indexOf("RequiredState")+13).trim();
//					//reqKey = "req1 LaserScanner1::On())"
//					String req = null;
//					req = reqKey.substring(reqKey.indexOf(" "),reqKey.lastIndexOf(")")).trim();
//					reqKey = reqKey.substring(0,reqKey.indexOf(" ")).trim();
//					//reqKey = "req1"
//					requiredStates.put(reqKey,req);
//					if (planningOp) effects.put(reqKey,false);
//				}
//				else if (rv.equals(SimpleOperator.ReservedWord.AchievedState)) {
//					String reqKey = oneElement.substring(oneElement.indexOf("AchievedState")+13).trim();
//					//reqKey = "req1 LaserScanner1::On())"
//					String req = null;
//					req = reqKey.substring(reqKey.indexOf(" "),reqKey.lastIndexOf(")")).trim();
//					reqKey = reqKey.substring(0,reqKey.indexOf(" ")).trim();
//					//reqKey = "req1"
//					requiredStates.put(reqKey,req);
//					effects.put(reqKey,true);
//				}
//				else if (rv.equals(SimpleOperator.ReservedWord.RequiredResource)) {
//					String reqKey = oneElement.substring(oneElement.indexOf("RequiredResource")+16).trim();
//					//reqKey = "power(5))"
//					String requiredResource = reqKey.substring(0,reqKey.indexOf("(")).trim();
//					int requiredAmount = Integer.parseInt(reqKey.substring(reqKey.indexOf("(")+1,reqKey.indexOf(")")).trim());
//					for (int k = 0; k < resources.length; k++) {
//						if (resources[k].equals(requiredResource)) {
//							resourceRequirements[k] = requiredAmount;
//						}
//					}
//				}
//				else if (rv.equals(SimpleOperator.ReservedWord.Constraint)) {
//					String aux = oneElement.substring(oneElement.indexOf("Constraint")+10);
//					String constraintName = null;
//					Vector<Bounds> bounds = null;
//					if (aux.contains("[")) {
//						constraintName = aux.substring(0,aux.indexOf("[")).trim();
//						String boundsString = aux.substring(aux.indexOf("["),aux.indexOf("]")+1);
//						String[] splitBounds = boundsString.split("\\[");
//						bounds = new Vector<Bounds>();
//						for (String oneBound : splitBounds) {
//							if (!oneBound.trim().equals("")) {
//								String lbString = oneBound.substring(oneBound.indexOf("[")+1,oneBound.indexOf(",")).trim();
//								String ubString = oneBound.substring(oneBound.indexOf(",")+1,oneBound.indexOf("]")).trim();
//								long lb, ub;
//								if (lbString.equals("INF")) lb = org.metacsp.time.APSPSolver.INF;
//								else lb = Long.parseLong(lbString);
//								if (ubString.equals("INF")) ub = org.metacsp.time.APSPSolver.INF;
//								else ub = Long.parseLong(ubString);
//								bounds.add(new Bounds(lb,ub));
//							}
//						}
//					}
//					else {
//						constraintName = aux.substring(0,aux.indexOf("(")).trim();
//					}
//					String from = null;
//					String to = null;
//					if (constraintName.equals("Duration")) {
//						from = aux.substring(aux.indexOf("(")+1, aux.indexOf(")")).trim();
//						to = from;
//					}
//					else {
//						from = aux.substring(aux.indexOf("(")+1, aux.indexOf(",")).trim();
//						to = aux.substring(aux.indexOf(",")+1, aux.indexOf(")")).trim();
//					}
//					AllenIntervalConstraint con = null;
//					if (bounds != null) con = new AllenIntervalConstraint(AllenIntervalConstraint.Type.valueOf(constraintName),bounds.toArray(new Bounds[bounds.size()]));
//					else con = new AllenIntervalConstraint(AllenIntervalConstraint.Type.valueOf(constraintName));
//					constraints.add(con);
//					froms.add(from);
//					tos.add(to);
//				}
//			}
//		}
		
		class AdditionalConstraint {
			AllenIntervalConstraint con;
			int from, to;
			public AdditionalConstraint(AllenIntervalConstraint con, int from, int to) {
				this.con = con;
				this.from = from;
				this.to = to;
			}
			public void addAdditionalConstraint(SimpleOperator op) {
				op.addConstraint(con, from, to);
			}
		}
		
		//What I have:
		//constraints = {During, Duration, Before}
		//froms = {Head, Head, req1}
		//tos = {req1, Head, req2}
		//requirements = {req2 = Robot1::At(room), req1 = Robot1::MoveTo()}
	
		int reqCounter = 0;
		
		//pass this to constructor
		String[] requirementStrings = new String[requiredStates.keySet().size()];
		boolean[] effectBools = new boolean[requiredStates.keySet().size()];
		
		//pass this to constructor
		AllenIntervalConstraint[] consFromHeadtoReq = new AllenIntervalConstraint[requiredStates.keySet().size()];
		Vector<AdditionalConstraint> acs = new Vector<AdditionalConstraint>();
		
		for (String reqKey : requiredStates.keySet()) {
			String requirement = requiredStates.get(reqKey);
			requirementStrings[reqCounter] = requirement;
			if (planningOp) {
				if (effects.get(reqKey)) effectBools[reqCounter] = true;
				else effectBools[reqCounter] = false;
			}
			for (int i = 0; i < froms.size(); i++) {
				if (froms.elementAt(i).equals("Head") && tos.elementAt(i).equals(reqKey)) {
					consFromHeadtoReq[reqCounter] = constraints.elementAt(i);
				}
			}
			reqCounter++;
		}
	
		//What I have:
		//constraints = {During, Duration, Before}
		//froms = {Head, Head, req1}
		//tos = {req1, Head, req2}
		//requirements = {req2 = Robot1::At(room), req1 = Robot1::MoveTo()}
		//requirementStrings = [Robot1::At(room), Robot1::MoveTo()]
		//consFromHeadtoReq = [During,null]
		
		//addConstraint(durationMoveTo, 0, 0);
		//addConstraint(beforeReq1Req2, 1, 2);
		
		for (int i = 0; i < froms.size(); i++) {
			if (froms.elementAt(i).equals("Head") && tos.elementAt(i).equals("Head")) {
				AdditionalConstraint ac = new AdditionalConstraint(constraints.elementAt(i), 0, 0);
				acs.add(ac);
			}
			else if (!froms.elementAt(i).equals("Head") && !tos.elementAt(i).equals("Head")) {
				String reqFromKey = froms.elementAt(i);
				String reqToKey = tos.elementAt(i);
				int reqFromIndex = -1;
				int reqToIndex = -1;
				AllenIntervalConstraint con = constraints.elementAt(i);
				String reqFrom = requiredStates.get(reqFromKey);
				String reqTo = requiredStates.get(reqToKey);
				for (int j = 0; j < requirementStrings.length; j++) {
					if (requirementStrings[i].equals(reqFrom)) reqFromIndex = j;
					if (requirementStrings[i].equals(reqTo)) reqToIndex = j;
				}
				AdditionalConstraint ac = new AdditionalConstraint(con, reqFromIndex, reqToIndex);
				acs.add(ac);
			}
		}
				
		//Call constructor
		SimpleOperator ret = null;
		if (!planningOp) ret = new SimpleOperator(head,consFromHeadtoReq,requirementStrings,resourceRequirements);
		else ret = new PlanningOperator(head,consFromHeadtoReq,requirementStrings,effectBools,resourceRequirements);
		for (AdditionalConstraint ac : acs) ac.addAdditionalConstraint(ret);
		//System.out.println("OP: " + ret);
		return ret;
	}

	protected static String[] parseKeyword(String keyword, String everything) {
		Vector<String> elements = new Vector<String>();
		int lastElement = everything.lastIndexOf(keyword);
		while (lastElement != -1) {
			int bw = lastElement;
			int fw = lastElement;
			boolean skip = false;
			while (everything.charAt(--bw) != '(') { 
				if (everything.charAt(bw) != ' ' && everything.charAt(bw) != '(') {
					everything = everything.substring(0,bw);
					lastElement = everything.lastIndexOf(keyword);
					skip = true;
					break;
				}
			}
			if (!skip) {
				int parcounter = 1;
				while (parcounter != 0) {
					if (everything.charAt(fw) == '(') parcounter++;
					else if (everything.charAt(fw) == ')') parcounter--;
					fw++;
				}
				String element = everything.substring(bw,fw).trim();
				element = element.substring(element.indexOf(keyword)+keyword.length(),element.lastIndexOf(")")).trim();
				if (!element.startsWith(",") && !element.trim().equals("")) elements.add(element);
				everything = everything.substring(0,bw);
				lastElement = everything.lastIndexOf(keyword);
			}
		}
		return elements.toArray(new String[elements.size()]);		
	}

//	protected static String[] parseSensors(String everything) {
//		Vector<String> sensors = new Vector<String>();
//		int lastSensor = everything.lastIndexOf("Sensor");
//		while (lastSensor != -1) {
//			int bw = lastSensor;
//			int fw = lastSensor;
//			while (everything.charAt(--bw) != '(') { }
//			int parcounter = 1;
//			while (parcounter != 0) {
//				if (everything.charAt(fw) == '(') parcounter++;
//				else if (everything.charAt(fw) == ')') parcounter--;
//				fw++;
//			}
//			String sensor = everything.substring(bw,fw).trim();
//			sensor = sensor.substring(sensor.indexOf("Sensor")+6,sensor.indexOf(")")).trim();
//			sensors.add(sensor);
//			everything = everything.substring(0,bw);
//			lastSensor = everything.lastIndexOf("Sensor");
//		}
//		return sensors.toArray(new String[sensors.size()]);		
//	}

//	protected static String[] parseContextVars(String everything) {
//		Vector<String> contexts = new Vector<String>();
//		int lastContext = everything.lastIndexOf("ContextVariable");
//		while (lastContext != -1) {
//			int bw = lastContext;
//			int fw = lastContext;
//			while (everything.charAt(--bw) != '(') { }
//			int parcounter = 1;
//			while (parcounter != 0) {
//				if (everything.charAt(fw) == '(') parcounter++;
//				else if (everything.charAt(fw) == ')') parcounter--;
//				fw++;
//			}
//			String context = everything.substring(bw,fw).trim();
//			context = context.substring(context.indexOf("ContextVariable")+15,context.indexOf(")")).trim();
//			contexts.add(context);
//			everything = everything.substring(0,bw);
//			lastContext = everything.lastIndexOf("ContextVariable");
//		}
//		return contexts.toArray(new String[contexts.size()]);		
//	}

//	protected static String[] parseSimpleOperators(String everything) {
//		Vector<String> operators = new Vector<String>();
//		int lastOp = everything.lastIndexOf("SimpleOperator");
//		while (lastOp != -1) {
//			int bw = lastOp;
//			int fw = lastOp;
//			while (everything.charAt(--bw) != '(') { }
//			int parcounter = 1;
//			while (parcounter != 0) {
//				if (everything.charAt(fw) == '(') parcounter++;
//				else if (everything.charAt(fw) == ')') parcounter--;
//				fw++;
//			}
//			operators.add(everything.substring(bw,fw));
//			everything = everything.substring(0,bw);
//			lastOp = everything.lastIndexOf("SimpleOperator");
//		}
//		return operators.toArray(new String[operators.size()]);
//	}

//	protected static String[] parsePlanningOperators(String everything) {
//		Vector<String> operators = new Vector<String>();
//		int lastOp = everything.lastIndexOf("PlanningOperator");
//		while (lastOp != -1) {
//			int bw = lastOp;
//			int fw = lastOp;
//			while (everything.charAt(--bw) != '(') { }
//			int parcounter = 1;
//			while (parcounter != 0) {
//				if (everything.charAt(fw) == '(') parcounter++;
//				else if (everything.charAt(fw) == ')') parcounter--;
//				fw++;
//			}
//			operators.add(everything.substring(bw,fw));
//			everything = everything.substring(0,bw);
//			lastOp = everything.lastIndexOf("PlanningOperator");
//		}
//		return operators.toArray(new String[operators.size()]);
//	}

	protected static HashMap<String,Integer> processResources (String[] resources) {
		HashMap<String, Integer> ret = new HashMap<String, Integer>();
		for (String resourceElement : resources) {
			String resourceName = resourceElement.substring(0,resourceElement.indexOf(" ")).trim();
			int resourceCap = Integer.parseInt(resourceElement.substring(resourceElement.indexOf(" ")).trim());
			ret.put(resourceName, resourceCap);
		}
		return ret;
	}
	
//	protected static HashMap<String,Integer> parseResources(String everything) {
//		HashMap<String, Integer> ret = new HashMap<String, Integer>();
//		int lastRes = everything.lastIndexOf("Resource");
//		while (lastRes != -1) {
//			int bw = lastRes;
//			int fw = lastRes;
//			while (everything.charAt(--bw) != '(') { }
//			int parcounter = 1;
//			while (parcounter != 0) {
//				if (everything.charAt(fw) == '(') parcounter++;
//				else if (everything.charAt(fw) == ')') parcounter--;
//				fw++;
//			}
//			String resourceElement = everything.substring(bw,fw);
//			String resourceName = resourceElement.substring(resourceElement.indexOf("Resource")+8).trim();
//			try {
//				int resourceCap = Integer.parseInt(resourceName.substring(resourceName.indexOf(" "),resourceName.indexOf(")")).trim());
//				resourceName = resourceName.substring(0,resourceName.indexOf(" ")).trim();
//				ret.put(resourceName, resourceCap);
//			}
//			catch (java.lang.StringIndexOutOfBoundsException e) { /* ignore, was the end of "RequiredResource" */ }
//			finally {
//				everything = everything.substring(0,bw);
//				lastRes = everything.lastIndexOf("Resource");
//			}
//		}
//		return ret;
//	}
	
	
//	protected static String parseName(String everything) {
//		String ret = everything.substring(everything.indexOf("SimpleDomain")+12);
//		ret = ret.substring(0,ret.indexOf(")")).trim();
//		return ret;
//	}
	
	/**
	 * Parses a domain file (see domains/testDomain.ddl for an example), instantiates
	 * the necessary {@link MetaConstraint}s and adds them to the provided {@link SimplePlanner}.
	 * @param sp The {@link SimplePlanner} that will use this domain.
	 * @param filename Text file containing the domain definition. 
	 */
	public static void parseDomain(SimplePlanner sp, String filename) {
		String everything = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			try {
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();
				while (line != null) {
					if (!line.startsWith("#")) {
						sb.append(line);
						sb.append('\n');
					}
					line = br.readLine();
				}
				everything = sb.toString();
				String name = "";
				String[] nameArray = parseKeyword("SimpleDomain", everything);
				if (nameArray.length != 0) name = nameArray[0];
				else name = parseKeyword("PlanningDomain", everything)[0];
				String[] resourceElements = parseKeyword("Resource", everything);
				HashMap<String,Integer> resources = processResources(resourceElements);
				String[] operators = parseKeyword("SimpleOperator", everything);
				String[] sensors = parseKeyword("Sensor", everything);
				String[] contextVars = parseKeyword("ContextVariable", everything);
				
				int[] resourceCaps = new int[resources.keySet().size()];
				String[] resourceNames = new String[resources.keySet().size()];
				int resourceCounter = 0;
				for (String rname : resources.keySet()) {
					resourceNames[resourceCounter] = rname;
					resourceCaps[resourceCounter] = resources.get(rname);
					resourceCounter++;
				}
				SimpleDomain dom = new SimpleDomain(resourceCaps, resourceNames, name);
				for (String sensor : sensors) dom.addSensor(sensor);
				for (String cv : contextVars) dom.addContextVar(cv);
				for (String operator : operators) {
					dom.addOperator(SimpleDomain.parseOperator(operator,resourceNames));
				}
				//This adds the domain as a meta-constraint of the SimplePlanner
				sp.addMetaConstraint(dom);
				//... and we also add all its resources as separate meta-constraints
				for (Schedulable sch : dom.getSchedulingMetaConstraints()) sp.addMetaConstraint(sch);
			}
			finally { br.close(); }
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
	}

}
