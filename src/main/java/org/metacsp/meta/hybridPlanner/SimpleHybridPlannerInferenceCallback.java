package org.metacsp.meta.hybridPlanner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.logging.Logger;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.meta.simplePlanner.SimpleOperator;
import org.metacsp.meta.simplePlanner.SimpleReusableResource;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.SymbolicVariableActivity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.temporalRectangleAlgebra.SpatialFluentSolver;
import org.metacsp.sensing.InferenceCallback;
import org.metacsp.spatial.utility.SpatialRule;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;

public class SimpleHybridPlannerInferenceCallback implements InferenceCallback, Serializable {

	private static final long serialVersionUID = -6730506457770817729L;
	private SimpleHybridPlanner planner = null;
	private transient Logger logger = MetaCSPLogging.getLogger(this.getClass());
	private FluentBasedSimpleDomain domain = null;
	
	public SimpleHybridPlannerInferenceCallback(SimpleHybridPlanner planner) {
		this.planner = planner;
		MetaConstraint[] metaConstraints = planner.getMetaConstraints();
		for (MetaConstraint mc : metaConstraints) {
			if (mc instanceof FluentBasedSimpleDomain) {
				domain = (FluentBasedSimpleDomain) mc;
				break;
			}
		}
	}
	
	@Override
	public void doInference(long timeNow) {
		if (planner != null) {
			domain.updateTimeNow(timeNow);
//			hybridPlanner.clearResolvers();
//			hybridPlanner.backtrack();
			
			long timeNow1 = Calendar.getInstance().getTimeInMillis();

			if(!planner.backtrack()){
//				System.out.println("komaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaak");
				logger.info("Time now: " + timeNow);
//				Vector<ConstraintNetwork> toBeRemoved = new Vector<ConstraintNetwork>();
				planner.operatorsAlongBranch.clear();
				Vector<SymbolicVariableActivity> constraintDomainHasTobeRemoved = new Vector<SymbolicVariableActivity>();
				Vector<SymbolicVariableActivity> actsToBeremoved = new Vector<SymbolicVariableActivity>();
				Vector<SymbolicVariableActivity> currentSituation = new Vector<SymbolicVariableActivity>();
				
				for (ConstraintNetwork cn : planner.getResolvers().keySet()) {
					for (int i = 0; i < planner.getGoals().size(); i++) {
						SymbolicVariableActivity metaVarAct = ((SymbolicVariableActivity)cn.getVariables()[0]);
						if(metaVarAct.equals(planner.getGoals().get(i))){
							metaVarAct.setMarking(org.metacsp.meta.simplePlanner.SimpleDomain.markings.UNJUSTIFIED);
							constraintDomainHasTobeRemoved.add(metaVarAct);
						}								
						else if(metaVarAct.getTemporalVariable().getLST() >= timeNow - 1){
							constraintDomainHasTobeRemoved.add(metaVarAct);
							actsToBeremoved.add(metaVarAct);
//							System.out.println("has to be removed: " + metaVarAct);
						}
						else if(metaVarAct.getTemporalVariable().getEET() > timeNow ){
							currentSituation.add(metaVarAct);
//							System.out.println("current situation: " + metaVarAct);
						}
					}							
				}						

				
				
				//delete all the constraints involves planned activity
				ActivityNetworkSolver groundActSolver =  ((ActivityNetworkSolver)((SpatialFluentSolver)planner.getConstraintSolvers()[0]).getConstraintSolvers()[1]);
				Vector<Constraint> consToBeRemoved = new Vector<Constraint>();
				for (int i = 0; i < groundActSolver.getConstraints().length; i++) {
//					System.out.println("trying constraint: " + groundActSolver.getConstraints()[i]);
					for (int j = 0; j < constraintDomainHasTobeRemoved.size(); j++) {
						if(groundActSolver.getConstraints()[i].getScope()[0].equals(constraintDomainHasTobeRemoved.get(j)) || 
								groundActSolver.getConstraints()[i].getScope()[1].equals(constraintDomainHasTobeRemoved.get(j))){
//							System.out.println("to be removed: " + groundActSolver.getConstraints()[i]);
							consToBeRemoved.add(groundActSolver.getConstraints()[i]);
							break;									
						}
					}
				}
				
				groundActSolver.removeConstraints(consToBeRemoved.toArray(new Constraint[consToBeRemoved.size()]));
				
				Vector<SymbolicVariableActivity> activityOnResourceUse = new Vector<SymbolicVariableActivity>();
				//print all resources in use
				for (int j = 0; j < planner.getMetaConstraints().length; j++){
					if(planner.getMetaConstraints()[j] instanceof SimpleReusableResource ){
						SimpleReusableResource rr = (SimpleReusableResource)planner.getMetaConstraints()[j];
						for (Activity act : rr.getActivityOnUse()) activityOnResourceUse.add((SymbolicVariableActivity)act.getVariable());																
					}
				}
				
//				System.out.println("ActivityOnUse: " + activityOnResourceUse);
				
				//it delets the activities currently uses resources
				for (int j = 0; j < planner.getMetaConstraints().length; j++){ 
					if(planner.getMetaConstraints()[j] instanceof FluentBasedSimpleDomain ){
						FluentBasedSimpleDomain mcc = (FluentBasedSimpleDomain)planner.getMetaConstraints()[j];
						for (Variable v : activityOnResourceUse) {
							for (SimpleReusableResource rr : mcc.getCurrentReusableResourcesUsedByActivity((SymbolicVariableActivity)v)) {
//								System.out.println("--->" + (Activity)v);
								rr.removeUsage((SymbolicVariableActivity)v);
							}
						}
					}
				}
				
				groundActSolver.removeVariables(actsToBeremoved.toArray(new SymbolicVariableActivity[actsToBeremoved.size()]));
				//hybridPlanner.clearResolvers();
				
				//it is deleting all the allocation of resource in the previous failed backtrack search
				FluentBasedSimpleDomain causalReasoner = null;
				for (int j = 0; j < planner.getMetaConstraints().length; j++){ 
					if(planner.getMetaConstraints()[j] instanceof FluentBasedSimpleDomain ){
						causalReasoner = (FluentBasedSimpleDomain)planner.getMetaConstraints()[j];
//						System.out.println(" @@@@@@@@@ " + mcc.getAllResourceUsageLevel());
						causalReasoner.resetAllResourceAllocation();
						//mcc.activeHeuristic(false);
						break;
					}
				}			
				planner.clearResolvers();

				
				for (int j = 0; j < planner.getMetaConstraints().length; j++){ 
					if(planner.getMetaConstraints()[j] instanceof MetaOccupiedConstraint ){
						MetaOccupiedConstraint mcc = (MetaOccupiedConstraint)planner.getMetaConstraints()[j];
//						System.out.println(" @@@@@@@@@ " + mcc.getAllResourceUsageLevel());
//						mcc.activeHeuristic(true);
//						mcc.activeHeuristic(false);
						break;
					}
				}			
				
				
				
				if(planner.learningFromFailure()){
					//get Overlapped objects
					Vector<String> overlappedObject = new Vector<String>();
//					overlappedObject.add("atLocation::at_cup1_table1()");
					
					
					HashMap<SymbolicVariableActivity, Vector<SimpleOperator>> alternativeOperators = new HashMap<SymbolicVariableActivity, Vector<SimpleOperator>>();
					//extract current robot act rather the placement (e.g., holding)
					for (int i = 0; i < currentSituation.size(); i++) {
						if(currentSituation.get(i).getComponent().compareTo("RobotProprioception") == 0){
							Vector<SimpleOperator> ops = new Vector<SimpleOperator>();
							for (int j = 0; j < causalReasoner.getOperators().length; j++) {
								String head = currentSituation.get(i).getComponent() + "::" + (currentSituation.get(i).getSymbolicVariable().getSymbols()[0]);
								if(causalReasoner.getOperators()[j].getHead().compareTo(head) == 0){
									for (int k = 0; k <  causalReasoner.getOperators().length; k++) {
//										System.out.println("@@@@@@"+causalReasoner.getOperators()[j].getRequirementActivities()[0]);
										if(causalReasoner.getOperators()[k].getHead().compareTo(causalReasoner.getOperators()[j].getRequirementActivities()[0]) == 0){
											ops.add(causalReasoner.getOperators()[k]);
										}
									}	
								}
							}
							alternativeOperators.put(currentSituation.get(i), ops);
						}
					}
					
					SimpleOperator bestApplicableOpertor = getBestExapansion(currentSituation, alternativeOperators, overlappedObject);
//					System.out.println("bestApplicabaleOPrator" + bestApplicableOpertor);
					for (int i = 0; i < bestApplicableOpertor.getRequirementActivities().length; i++) {
						
						String operatorHead = bestApplicableOpertor.getRequirementActivities()[i];
						String opeatorHeadComponent = operatorHead.substring(0, operatorHead.indexOf("::"));
						String operatorHeadSymbol = operatorHead.substring(operatorHead.indexOf("::")+2, operatorHead.length());
						
						if(opeatorHeadComponent.compareTo("atLocation") == 0){
							Vector<Constraint> cons = new Vector<Constraint>();

							long duration = 1000;
							SymbolicVariableActivity two = (SymbolicVariableActivity)((SpatialFluentSolver)planner.getConstraintSolvers()[0]).getConstraintSolvers()[1].createVariable(opeatorHeadComponent);
							two.setSymbolicDomain(operatorHeadSymbol);
							two.setMarking(org.metacsp.meta.simplePlanner.SimpleDomain.markings.UNJUSTIFIED);
							
							AllenIntervalConstraint durationHolding = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(duration,APSPSolver.INF));
							durationHolding.setFrom(two);
							durationHolding.setTo(two);
							cons.add(durationHolding);

							AllenIntervalConstraint before= new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, AllenIntervalConstraint.Type.Before.getDefaultBounds());
							before.setFrom(two);
							before.setTo(planner.getGoals().get(0));
							cons.add(before);

							
							((SpatialFluentSolver)planner.getConstraintSolvers()[0]).getConstraintSolvers()[1].addConstraints(cons.toArray(new Constraint[cons.size()]));

						}
					}
				}						
			}
			System.out.println("TOTAL TIME: " + (Calendar.getInstance().getTimeInMillis()-timeNow1));
		}
	}
	
	private SimpleOperator getBestExapansion(Vector<SymbolicVariableActivity> currentSituation, HashMap<SymbolicVariableActivity, Vector<SimpleOperator>> alternativeOperators, Vector<String> overlappedObject) {
		
		HashMap<SimpleOperator, Integer> rank = new HashMap<SimpleOperator, Integer>();
		for (SymbolicVariableActivity activity : alternativeOperators.keySet()) {
			for (int i = 0; i < alternativeOperators.get(activity).size(); i++) {
				rank.put(alternativeOperators.get(activity).get(i), alternativeOperators.get(activity).get(i).getRequirementActivities().length);
			}
		}
		HashMap<SimpleOperator, Integer> retH = sortHashMapByValues(rank);
		
		SimpleOperator ret = null;
		for (SimpleOperator so : retH.keySet()) {
			ret = so;
			break;
		}
		
		return ret;
	}

//	private Integer getApllicabilityRank(Vector<Activity> currentSituation,SimpleOperator simpleOperator, Vector<String> overlappedObject) {
//		
//		int counter = 0;
////		System.out.println("--" + simpleOperator.getRequirementActivities().length);
//		for (int i = 0; i < simpleOperator.getRequirementActivities().length; i++) {
//			System.out.println("operator: " + simpleOperator.getRequirementActivities()[i]);
//			for (int j = 0; j < currentSituation.size(); j++) {
//				String head = currentSituation.get(j).getComponent() + "::" + (currentSituation.get(j).getSymbolicVariable().getSymbols()[0]);				
//				System.out.println("state: " + head);
//				if(simpleOperator.getRequirementActivities()[i].compareTo(head) == 0 && !overlappedObject.contains(head)){
//					counter++;
//					continue;
//				}
//				
//			}
//		}
//		
//		System.out.println("head: " +simpleOperator.getHead());
//		System.out.println("rank: " + counter);
//		
//		return counter;
//	}
	
	private static LinkedHashMap sortHashMapByValues(HashMap passedMap) {
		ArrayList mapKeys = new ArrayList(passedMap.keySet());
		ArrayList mapValues = new ArrayList(passedMap.values());
		Collections.sort(mapValues);
		//Collections.sort(mapKeys);

		LinkedHashMap sortedMap = 
				new LinkedHashMap();

		Iterator valueIt = ((java.util.List<SpatialRule>) mapValues).iterator();
		while (valueIt.hasNext()) {
			int val = (Integer) valueIt.next();
			Iterator keyIt = ((java.util.List<SpatialRule>) mapKeys).iterator();

			while (keyIt.hasNext()) {
				SimpleOperator key = (SimpleOperator) keyIt.next();
				int comp1 = (Integer) passedMap.get(key);
				int comp2 = val;

				if (comp1 == comp2){
					passedMap.remove(key);
					mapKeys.remove(key);
					sortedMap.put(key, val);
					break;
				}
			}
		}
		return sortedMap;
	}


}
