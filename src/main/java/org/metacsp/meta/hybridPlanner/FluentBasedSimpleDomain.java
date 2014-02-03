package org.metacsp.meta.hybridPlanner;

import org.metacsp.framework.ConstraintSolver;
import org.metacsp.meta.simplePlanner.SimpleDomain;
import org.metacsp.multi.spatioTemporal.SpatialFluentSolver;


public class FluentBasedSimpleDomain extends SimpleDomain {

	public FluentBasedSimpleDomain(int[] capacities, String[] resourceNames,
			String domainName) {
		super(capacities, resourceNames, domainName);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 8380363685271158262L;
	
	@Override
	public ConstraintSolver getGroundSolver() {
		return ((SpatialFluentSolver)metaCS.getConstraintSolvers()[0]).getConstraintSolvers()[1];
	}
	
//	public static void parseHybridDomain(SimpleHybridPlanner shp, String filename, Class domainType) {
//	String everything = null;
//	try {
//		BufferedReader br = new BufferedReader(new FileReader(filename));
//		try {
//			StringBuilder sb = new StringBuilder();
//			String line = br.readLine();
//			while (line != null) {
//				if (!line.startsWith("#")) {
//					sb.append(line);
//					sb.append('\n');
//				}
//				line = br.readLine();
//			}
//			everything = sb.toString();
//			String name = "";
//			String[] nameArray = parseKeyword("SimpleDomain", everything);
//			if (nameArray.length != 0) name = nameArray[0];
//			else name = parseKeyword("PlanningDomain", everything)[0];
//			String[] resourceElements = parseKeyword("Resource", everything);
//			HashMap<String,Integer> resources = processResources(resourceElements);
//			String[] simpleOperators = parseKeyword("SimpleOperator", everything);
//			String[] planningOperators = parseKeyword("PlanningOperator", everything);
//			String[] sensors = parseKeyword("Sensor", everything);
//			String[] contextVars = parseKeyword("ContextVariable", everything);
//			
//			int[] resourceCaps = new int[resources.keySet().size()];
//			String[] resourceNames = new String[resources.keySet().size()];
//			int resourceCounter = 0;
//			for (String rname : resources.keySet()) {
//				resourceNames[resourceCounter] = rname;
//				resourceCaps[resourceCounter] = resources.get(rname);
//				resourceCounter++;
//			}
//
//			FluentBasedSimpleDomain dom = null;
//			if (domainType.equals(FluentBasedSimpleDomain.class)) {
//				dom = new FluentBasedSimpleDomain(resourceCaps, resourceNames, name);
//			}
////			else if (domainType.equals(ProactivePlanningDomain.class)) {
////				dom = new ProactivePlanningDomain(resourceCaps, resourceNames, name);
////				//Try to support most inferred activities first
////				ValueOrderingH valOH = new ValueOrderingH() {
////					@Override
////					public int compare(ConstraintNetwork arg0, ConstraintNetwork arg1) {
////						if (arg0.getAnnotation() != null && arg1.getAnnotation() != null) {
////							if (arg0.getAnnotation() instanceof Integer && arg1.getAnnotation() instanceof Integer) {
////								return (Integer)arg1.getAnnotation()-(Integer)arg0.getAnnotation(); 
////							}
////						}
////						return arg1.getVariables().length - arg0.getVariables().length;
////					}
////				};
////				
////				//No variable ordering
////				VariableOrderingH varOH = new VariableOrderingH() {
////					@Override
////					public int compare(ConstraintNetwork o1, ConstraintNetwork o2) { return 0; }
////					@Override
////					public void collectData(ConstraintNetwork[] allMetaVariables) { }
////				};
////
////				dom.setValOH(valOH);
////				dom.setVarOH(varOH);
////			}
//			//SimpleDomain dom = new SimpleDomain(resourceCaps, resourceNames, name);
//			//ProactivePlanningDomain dom = new ProactivePlanningDomain(resourceCaps, resourceNames, name);
//
//			for (String sensor : sensors) dom.addSensor(sensor);
//			for (String cv : contextVars) dom.addContextVar(cv);
//			for (String operator : simpleOperators) {
//				dom.addOperator(FluentBasedSimpleDomain.parseOperator(operator,resourceNames,false));
//			}
//			System.out.println("planningOperators size: " + planningOperators.length);
//			for (String operator : planningOperators) {
//				dom.addOperator(FluentBasedSimpleDomain.parseOperator(operator,resourceNames,true));
//			}
//			//This adds the domain as a meta-constraint of the SimplePlanner
//			shp.addMetaConstraint(dom);
//			//... and we also add all its resources as separate meta-constraints
//			for (Schedulable sch : dom.getSchedulingMetaConstraints()) shp.addMetaConstraint(sch);
//		}
//		finally { br.close(); }
//	}
//	catch (FileNotFoundException e) { e.printStackTrace(); }
//	catch (IOException e) { e.printStackTrace(); }
//}

//	public FluentBasedSimpleDomain(int[] capacities, String[] resourceNames,
//			String domainName) {
//		super(capacities, resourceNames, domainName);
//		// TODO Auto-generated constructor stub
//	}
//
//	
//	@Override
//	public ConstraintNetwork[] getMetaVariables() {
//		ConstraintSolver gs = ((SpatialFluentSolver)metaCS.getConstraintSolvers()[0]).getConstraintSolvers()[1];
////		ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)this.metaCS.getConstraintSolvers()[0];
////		(SpatialFluentSolver)this.metaCS.getConstraintSolvers()[0]).getConstraintSolvers()[0]
//		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
//		for (int i = 0; i < ((ActivityNetworkSolver)(((SpatialFluentSolver)this.metaCS.getConstraintSolvers()[0]).
//				getConstraintSolvers()[1])).getVariables().length; i++) {
//			if(((ActivityNetworkSolver)(((SpatialFluentSolver)this.metaCS.getConstraintSolvers()[0]).getConstraintSolvers()[1])).
//					getVariables()[i].getMarking().equals(markings.UNJUSTIFIED)){
////				System.out.println("inside: " +((ActivityNetworkSolver)(((SpatialFluentSolver)this.metaCS.getConstraintSolvers()[0]).
////						getConstraintSolvers()[1])).getVariables()[i]);
//				ConstraintNetwork nw = new ConstraintNetwork(null);
//				nw.addVariable(((ActivityNetworkSolver)(((SpatialFluentSolver)this.metaCS.getConstraintSolvers()[0]).
//						getConstraintSolvers()[1])).getVariables()[i]);
//				ret.add(nw);
//			}
//		}
//		
//		return ret.toArray(new ConstraintNetwork[ret.size()]);
//	}
//
//	
//	
//	
//	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable) {
//		Vector<ConstraintNetwork> retPossibleConstraintNetworks = new Vector<ConstraintNetwork>();
//		ConstraintNetwork problematicNetwork = metaVariable.getConstraintNetwork();
//		Activity problematicActivity = (Activity)problematicNetwork.getVariables()[0]; 
//
//		//If it's a sensor, it needs to be unified
//		if (isSensor(problematicActivity.getComponent())) {
//			return this.getUnifications(problematicActivity);
//		}
//		
//		//If it's a context var, it needs to be unified (or expanded, see later) 
//		if (isContextVar(problematicActivity.getComponent())) {
//			ConstraintNetwork[] unifications = getUnifications(problematicActivity);
//			if (unifications != null) {
//				for (ConstraintNetwork oneUnification : unifications) {
//					retPossibleConstraintNetworks.add(oneUnification);
//					oneUnification.setAnnotation(2);
//				}
//			}
//		}
//		
//		//Find all expansions
//		for (SimpleOperator r : operators) {
//			String problematicActivitySymbolicDomain = problematicActivity.getSymbolicVariable().getSymbols()[0];
//			String operatorHead = r.getHead();
//			String opeatorHeadComponent = operatorHead.substring(0, operatorHead.indexOf("::"));
//			String operatorHeadSymbol = operatorHead.substring(operatorHead.indexOf("::")+2, operatorHead.length());
//			if (opeatorHeadComponent.equals(problematicActivity.getComponent())) {
//				if (problematicActivitySymbolicDomain.contains(operatorHeadSymbol)) {
//					ConstraintNetwork newResolver = expandOperator(r,problematicActivity);
//					newResolver.setAnnotation(1);
//					retPossibleConstraintNetworks.add(newResolver);
//				}
//			}
//			if (r instanceof PlanningOperator) {
//				for (String reqState : r.getRequirementActivities()) {
//					String operatorEffect = reqState;
//					String opeatorEffectComponent = operatorEffect.substring(0, operatorEffect.indexOf("::"));
//					String operatorEffectSymbol = operatorEffect.substring(operatorEffect.indexOf("::")+2, operatorEffect.length());
//					if (((PlanningOperator)r).isEffect(reqState)) {
//						if (opeatorEffectComponent.equals(problematicActivity.getComponent())) {
//							if (problematicActivitySymbolicDomain.contains(operatorEffectSymbol)) {
//								ConstraintNetwork newResolver = expandOperator(r,problematicActivity);
//								newResolver.setAnnotation(1);
//								retPossibleConstraintNetworks.add(newResolver);
//							}
//						}
//					}
//				}
//			}
//		}
//		
//		if (!retPossibleConstraintNetworks.isEmpty()) return retPossibleConstraintNetworks.toArray(new ConstraintNetwork[retPossibleConstraintNetworks.size()]);
//		ConstraintNetwork nullActivityNetwork = new ConstraintNetwork(null);
//		return new ConstraintNetwork[] {nullActivityNetwork};
//	}
//	
//	private ConstraintNetwork expandOperator(SimpleOperator possibleOperator, Activity problematicActivity) {
//		logger.finest("Expanding operator " + possibleOperator.getHead());
//		ConstraintNetwork activityNetworkToReturn = new ConstraintNetwork(null);
//		//ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)this.metaCS.getConstraintSolvers()[0];
//		ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)(((SpatialFluentSolver)this.metaCS.getConstraintSolvers()[0]).getConstraintSolvers()[1]);
//		String possibleOperatorHead = possibleOperator.getHead();
//		String possibleOperatorHeadSymbol = possibleOperatorHead.substring(possibleOperatorHead.indexOf("::")+2, possibleOperatorHead.length());
//		String possibleOperatorHeadComponent = possibleOperatorHead.substring(0, possibleOperatorHead.indexOf("::"));
//		Variable headActivity = null;
//
//		boolean problematicActIsEffect = false;
//		Variable[] operatorTailActivitiesToInsert = new Variable[0];
//
//		if (possibleOperator.getRequirementActivities() != null) {
//			operatorTailActivitiesToInsert = new Variable[possibleOperator.getRequirementActivities().length];
//
//			for (int i = 0; i < possibleOperator.getRequirementActivities().length; i++) {
//				String possibleOperatorTail = possibleOperator.getRequirementActivities()[i];
//				String possibleOperatorTailComponent = possibleOperatorTail.substring(0, possibleOperatorTail.indexOf("::"));
//				String possibleOperatorTailSymbol = possibleOperatorTail.substring(possibleOperatorTail.indexOf("::")+2, possibleOperatorTail.length());
//				
//				//If this req is the prob act, then insert prob act
//				if (possibleOperatorTailComponent.equals(problematicActivity.getComponent()) && possibleOperatorTailSymbol.equals(problematicActivity.getSymbolicVariable().getSymbols()[0])) {
//					operatorTailActivitiesToInsert[i] = problematicActivity;
//					problematicActIsEffect = true;
//				}
//				//else make a new var prototype and insert it
//				else {
//					VariablePrototype tailActivity = new VariablePrototype(groundSolver, possibleOperatorTailComponent, possibleOperatorTailSymbol);
//					operatorTailActivitiesToInsert[i] = tailActivity;
//					if (possibleOperator instanceof PlanningOperator) {
//						if (((PlanningOperator)possibleOperator).isEffect(possibleOperatorTail)) {
//							tailActivity.setMarking(markings.JUSTIFIED);
//						}
//						else {
//							tailActivity.setMarking(markings.UNJUSTIFIED);
//						}
//					}
//					else {
//						tailActivity.setMarking(markings.UNJUSTIFIED);
//					}
//				}
//			}
//			
//			//Also add head if the prob activity was unified with an effect
//			if (problematicActIsEffect) {
//				headActivity = new VariablePrototype(groundSolver, possibleOperatorHeadComponent, possibleOperatorHeadSymbol);
//				headActivity.setMarking(markings.JUSTIFIED);
//			}
//
//			Vector<AllenIntervalConstraint> allenIntervalConstraintsToAdd = new Vector<AllenIntervalConstraint>();
//
//			for (int i = 0; i < possibleOperator.getRequirementConstraints().length; i++) {
//				if (possibleOperator.getRequirementConstraints()[i] != null) {
//					AllenIntervalConstraint con = (AllenIntervalConstraint)possibleOperator.getRequirementConstraints()[i].clone();
//					if (problematicActIsEffect) con.setFrom(headActivity);
//					else con.setFrom(problematicActivity);
//					con.setTo(operatorTailActivitiesToInsert[i]);
//					allenIntervalConstraintsToAdd.add(con);
//				}
//			}
//			for (AllenIntervalConstraint con : allenIntervalConstraintsToAdd) activityNetworkToReturn.addConstraint(con);
//		}
//		
//		Vector<AllenIntervalConstraint> toAddExtra = new Vector<AllenIntervalConstraint>();
//		for (int i = 0; i < operatorTailActivitiesToInsert.length+1; i++) {
//			AllenIntervalConstraint[][] ec = possibleOperator.getExtraConstraints();
//			if (ec != null) {
//				AllenIntervalConstraint[] con = ec[i];
//				for (int j = 0; j < con.length; j++) {
//					if (con[j] != null) {
//						AllenIntervalConstraint newCon = (AllenIntervalConstraint) con[j].clone();
//						if (i == 0) {
//							if (problematicActIsEffect) newCon.setFrom(headActivity);
//							else newCon.setFrom(problematicActivity);
//						}
//						else {
//							newCon.setFrom(operatorTailActivitiesToInsert[i-1]);
//						}
//						if (j == 0) {
//							if (problematicActIsEffect) newCon.setTo(headActivity);
//							else newCon.setTo(problematicActivity);
//						}
//						else {
//							newCon.setTo(operatorTailActivitiesToInsert[j-1]);
//						}
//						toAddExtra.add(newCon);
//					}
//				}
//			}
//		}
//
//		for (Variable v : operatorTailActivitiesToInsert) activityNetworkToReturn.addVariable(v);
//		if (!toAddExtra.isEmpty()) {
//			for (AllenIntervalConstraint con : toAddExtra) activityNetworkToReturn.addConstraint(con);
//		}
//				
//		int[] usages = possibleOperator.getUsages();
//		if (usages != null) {
//			for (int i = 0; i < usages.length; i++) {
//				if (usages[i] != 0) {
//					HashMap<Variable, Integer> utilizers = currentResourceUtilizers.get(resourcesMap.get(resourceNames[i]));
//					if (problematicActIsEffect) utilizers.put(headActivity, usages[i]);
//					else utilizers.put(problematicActivity, usages[i]);
//					activityNetworkToReturn.addVariable(problematicActivity);
//				}
//			}
//		}
//		return activityNetworkToReturn;						
//	}
//	
//	
//	
//	
//	protected ConstraintNetwork[] getUnifications(Activity activity) {
////		ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)this.metaCS.getConstraintSolvers()[0];
//		ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)(((SpatialFluentSolver)this.metaCS.getConstraintSolvers()[0]).getConstraintSolvers()[1]);
//		Variable[] acts = groundSolver.getVariables();
//		Vector<Activity> possibleUnifications = new Vector<Activity>();
//		Vector<ConstraintNetwork> unifications = new Vector<ConstraintNetwork>();
//		for (Variable var : acts) {
//			if (!var.equals(activity)) {
//				Activity act = (Activity)var;
//				String problematicActivitySymbolicDomain = activity.getSymbolicVariable().getSymbols()[0];
//				if (act.getComponent().equals(activity.getComponent())) {
//					String[] actSymbols = act.getSymbolicVariable().getSymbols();
//					for (String symbol : actSymbols) {
//						if (problematicActivitySymbolicDomain.contains(symbol)) {
//							if (act.getMarking().equals(markings.JUSTIFIED)) {
//								possibleUnifications.add(act);
//							}
//							break;
//						}
//					}
//				}
//			}
//		}
//		for (Activity act : possibleUnifications) {
//			AllenIntervalConstraint equals = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals);
//			equals.setFrom(activity);
//			equals.setTo(act);
//			ConstraintNetwork oneUnification = new ConstraintNetwork(null);
//			oneUnification.addConstraint(equals);
//			unifications.add(oneUnification);
//		}
//		if (unifications.isEmpty()) return null;
//		return unifications.toArray(new ConstraintNetwork[unifications.size()]);		
//	}

}
