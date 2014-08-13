package org.metacsp.sensing;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.logging.Logger;

import org.metacsp.dispatching.Dispatcher;
import org.metacsp.dispatching.DispatchingFunction;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.framework.meta.MetaVariable;
import org.metacsp.meta.fuzzyActivity.FuzzyActivityDomain.markings;
import org.metacsp.meta.hybridPlanner.FluentBasedSimpleDomain;
import org.metacsp.meta.hybridPlanner.MetaOccupiedConstraint;
import org.metacsp.meta.hybridPlanner.SimpleHybridPlanner;
import org.metacsp.meta.simplePlanner.ProactivePlanningDomain;
import org.metacsp.meta.simplePlanner.SimpleDomain;
import org.metacsp.meta.simplePlanner.SimpleOperator;
import org.metacsp.meta.simplePlanner.SimplePlanner;
import org.metacsp.meta.simplePlanner.SimpleReusableResource;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.spatioTemporal.SpatialFluent;
import org.metacsp.multi.spatioTemporal.SpatialFluentSolver;
import org.metacsp.spatial.utility.SpatialRule;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;

public class ConstraintNetworkAnimator extends Thread {
	
	private ConstraintNetwork cn = null;
	private ActivityNetworkSolver ans = null;
	private Activity future = null;
	private long originOfTime;
	private long firstTick;
	private long period;
	private AllenIntervalConstraint currentReleaseFuture = null;
	private HashMap<Sensor,HashMap<Long,String>> sensorValues = new HashMap<Sensor, HashMap<Long,String>>();
	private SimplePlanner planner = null;
	private ProactivePlanningDomain domain = null;
	private Dispatcher dis = null;
	
	private SimpleHybridPlanner hybridPlanner = null;
	private FluentBasedSimpleDomain fsDomain = null;
	private HashMap<Controllable,HashMap<Long,String>> controllableValues = new HashMap<Controllable, HashMap<Long,String>>();
	
	private transient Logger logger = MetaCSPLogging.getLogger(this.getClass());

	public ConstraintNetworkAnimator(SimpleHybridPlanner planner, long period) {
		this((ActivityNetworkSolver)(((SpatialFluentSolver)planner.getConstraintSolvers()[0])).getConstraintSolvers()[1],period);
		this.hybridPlanner = planner;
		MetaConstraint[] metaConstraints = planner.getMetaConstraints();
		for (MetaConstraint mc : metaConstraints) {
			if (mc instanceof FluentBasedSimpleDomain) {
				fsDomain = (FluentBasedSimpleDomain) mc;
				break;
			}
		}
	}
	
	public ConstraintNetworkAnimator(SimplePlanner planner, long period) {
		this((ActivityNetworkSolver)planner.getConstraintSolvers()[0],period);
		this.planner = planner;
		MetaConstraint[] metaConstraints = planner.getMetaConstraints();
		for (MetaConstraint mc : metaConstraints) {
			if (mc instanceof ProactivePlanningDomain) {
				domain = (ProactivePlanningDomain) mc;
				break;
			}
		}
	}
	
	
	public ConstraintNetworkAnimator(ActivityNetworkSolver ans, long period) {
		synchronized(ans) {
			this.ans = ans;
			this.period = period;
			//if (!realClock) originOfTime = 0;
			//else originOfTime = ans.getOrigin();
			originOfTime = ans.getOrigin();
			firstTick = Calendar.getInstance().getTimeInMillis();
			
			this.cn = ans.getConstraintNetwork();
			
			future = (Activity)ans.createVariable("Time");
			future.setSymbolicDomain("Future");
			future.setMarking(markings.JUSTIFIED);
			long timeNow = getTimeNow();
			AllenIntervalConstraint releaseFuture = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(timeNow, timeNow));
			releaseFuture.setFrom(future);
			releaseFuture.setTo(future);
			AllenIntervalConstraint deadlineFuture = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(ans.getHorizon(), ans.getHorizon()));
			deadlineFuture.setFrom(future);
			deadlineFuture.setTo(future);
			currentReleaseFuture = releaseFuture;
			if (!ans.addConstraints(currentReleaseFuture,deadlineFuture)) {
				throw new NetworkMaintenanceError(currentReleaseFuture,deadlineFuture);
			}
			this.start();
		}
	}
	
	public ConstraintNetwork getConstraintNetwork() { return this.cn; }
	
	public ActivityNetworkSolver getActivityNetworkSolver() { return this.ans; }

	public synchronized void postSensorValueToDispatch(Sensor sensor, long time, String value) {
		if (!this.sensorValues.keySet().contains(sensor))
			this.sensorValues.put(sensor, new HashMap<Long, String>());
		HashMap<Long, String> sensorVal = this.sensorValues.get(sensor);
		sensorVal.put(time, value);
	}
	
	public synchronized void postControllableValueToDispatch(Controllable controllable, long time, String value) {
		if (!this.controllableValues.keySet().contains(controllable))
			this.controllableValues.put(controllable, new HashMap<Long, String>());
		HashMap<Long, String> contrVal = this.controllableValues.get(controllable);
		contrVal.put(time, value);
	}

	public void registerSensorValuesToDispatch(Sensor sensor, HashMap<Long,String> values) {
		this.sensorValues.put(sensor, values);
	}
	
	public void registerControllableValuesToDispatch(Controllable controllable, HashMap<Long,String> values) {
		this.controllableValues.put(controllable, values);
	}
	
	public void addDispatchingFunctions(SimplePlanner planner, DispatchingFunction ... dfs) {
		boolean start = false;
		if (this.dis == null) {
			this.dis = new Dispatcher(planner, period);
			start = true;
		}
		for (DispatchingFunction df : dfs) dis.addDispatchingFunction(df.getComponent(), df);
		if (start) dis.start();
	}

	public void addDispatchingFunctions(SimpleHybridPlanner planner, DispatchingFunction ... dfs) {
		boolean start = false;
		if (this.dis == null) {
			this.dis = new Dispatcher(planner, period);
			start = true;
		}
		for (DispatchingFunction df : dfs) dis.addDispatchingFunction(df.getComponent(), df);
		if (start) dis.start();
	}

	
	private long getTimeNow() {
		return Calendar.getInstance().getTimeInMillis()-firstTick+originOfTime;
	}
	
	public void run() {
		int iteration = 0;
		while (true) {
			try { Thread.sleep(period); }
			catch (InterruptedException e) { e.printStackTrace(); }
			
			synchronized(ans) {
				//Update release constraint of Future
				long timeNow = getTimeNow();
				AllenIntervalConstraint releaseFuture = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(timeNow, timeNow));
				releaseFuture.setFrom(future);
				releaseFuture.setTo(future);
				if (currentReleaseFuture != null) ans.removeConstraint(currentReleaseFuture);
				if (!ans.addConstraint(releaseFuture)) {
					throw new NetworkMaintenanceError(releaseFuture);
				}
				currentReleaseFuture = releaseFuture;
				
				//If there are registered sensor traces, animate them too
				for (Sensor sensor : sensorValues.keySet()) {
					Vector<Long> toRemove = new Vector<Long>();
					HashMap<Long,String> values = sensorValues.get(sensor);
					for (long time : values.keySet()) {
						if (time <= timeNow) {
							sensor.modelSensorValue(values.get(time), time);
							toRemove.add(time);
						}
					}
					for (long time : toRemove) values.remove(time);
				}
				
				//If there are registered controllable sensor traces, animate them too
//				for (Controllable controllable : controllableValues.keySet()) {
//					Vector<Long> toRemove = new Vector<Long>();
//					HashMap<Long,String> values = controllableValues.get(controllable);
//					for (long time : values.keySet()) {
//						if (time <= timeNow) {
//							controllable.modelSensorValue(values.get(time), time);
//							toRemove.add(time);
//						}
//					}
//					for (long time : toRemove) values.remove(time);
//				}
				
				//If there is a registered planner, do the planning/context inference
				if (planner != null) {
					logger.info("Iteration " + iteration++);
					domain.resetContextInference();
					domain.updateTimeNow(timeNow);
					planner.clearResolvers();
					planner.backtrack();
//					Vector<Activity> oldInference = new Vector<Activity>();
					for (ConstraintNetwork cn : planner.getAddedResolvers()) {
						VariablePrototype var = null;
						for (Variable v : cn.getVariables()) {
							if (v instanceof VariablePrototype) {
								if (((VariablePrototype)v).getParameters().length > 2) {
									if (((VariablePrototype)v).getParameters()[2].equals("Inference")) {
										var = (VariablePrototype)v;
									}
								}
							}
						}
						if (var != null) {
							Activity act = (Activity)cn.getSubstitution(var);
							domain.setOldInference(act.getComponent(), act);
							//oldInference.add(act);
						}
					}
//					if (!oldInference.isEmpty()) {
//						domain.setOldInference(oldInference.toArray(new Activity[oldInference.size()]));
//					}
				}
				
				if (hybridPlanner != null) {
					logger.info("Iteration " + iteration++);
					fsDomain.updateTimeNow(timeNow);
//					hybridPlanner.clearResolvers();
//					hybridPlanner.backtrack();
					
					long timeNow1 = Calendar.getInstance().getTimeInMillis();

					if(!hybridPlanner.backtrack()){
						System.out.println("komaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaak");
						System.out.println("Time now: " + timeNow);
//						Vector<ConstraintNetwork> toBeRemoved = new Vector<ConstraintNetwork>();
						hybridPlanner.operatorsAlongBranch.clear();
						Vector<Activity> constraintDomainHasTobeRemoved = new Vector<Activity>();
						Vector<Activity> actsToBeremoved = new Vector<Activity>();
						Vector<Activity> currentSituation = new Vector<Activity>();
						
						for (ConstraintNetwork cn : hybridPlanner.getResolvers().keySet()) {
							for (int i = 0; i < hybridPlanner.getGoals().size(); i++) {
								Activity metaVarAct = ((Activity)cn.getVariables()[0]);
								if(metaVarAct.equals(hybridPlanner.getGoals().get(i))){
									metaVarAct.setMarking(org.metacsp.meta.simplePlanner.SimpleDomain.markings.UNJUSTIFIED);
									constraintDomainHasTobeRemoved.add(metaVarAct);
								}								
								else if(metaVarAct.getTemporalVariable().getLST() >= timeNow - 1){
									constraintDomainHasTobeRemoved.add(metaVarAct);
									actsToBeremoved.add(metaVarAct);
//									System.out.println("has to be removed: " + metaVarAct);
								}
								else if(metaVarAct.getTemporalVariable().getEET() > timeNow ){
									currentSituation.add(metaVarAct);
//									System.out.println("current situation: " + metaVarAct);
								}
							}							
						}						

						
						
						//delete all the constraints involves planned activity
						ActivityNetworkSolver groundActSolver =  ((ActivityNetworkSolver)((SpatialFluentSolver)hybridPlanner.getConstraintSolvers()[0]).getConstraintSolvers()[1]);
						Vector<Constraint> consToBeRemoved = new Vector<Constraint>();
						for (int i = 0; i < groundActSolver.getConstraints().length; i++) {
//							System.out.println("trying constraint: " + groundActSolver.getConstraints()[i]);
							for (int j = 0; j < constraintDomainHasTobeRemoved.size(); j++) {
								if(groundActSolver.getConstraints()[i].getScope()[0].equals(constraintDomainHasTobeRemoved.get(j)) || 
										groundActSolver.getConstraints()[i].getScope()[1].equals(constraintDomainHasTobeRemoved.get(j))){
//									System.out.println("to be removed: " + groundActSolver.getConstraints()[i]);
									consToBeRemoved.add(groundActSolver.getConstraints()[i]);
									break;									
								}
							}
						}
						
						groundActSolver.removeConstraints(consToBeRemoved.toArray(new Constraint[consToBeRemoved.size()]));
						
						Vector<Activity> activityOnResourceUse = new Vector<Activity>();
						//print all resources in use
						for (int j = 0; j < hybridPlanner.getMetaConstraints().length; j++){
							if(hybridPlanner.getMetaConstraints()[j] instanceof SimpleReusableResource ){
								SimpleReusableResource rr = (SimpleReusableResource)hybridPlanner.getMetaConstraints()[j];
								activityOnResourceUse.addAll(rr.getActivityOnUse());																
							}
						}
						
//						System.out.println("ActivityOnUse: " + activityOnResourceUse);
						
						//it delets the activities currently uses resources
						for (int j = 0; j < hybridPlanner.getMetaConstraints().length; j++){ 
							if(hybridPlanner.getMetaConstraints()[j] instanceof FluentBasedSimpleDomain ){
								FluentBasedSimpleDomain mcc = (FluentBasedSimpleDomain)hybridPlanner.getMetaConstraints()[j];
								for (Variable v : activityOnResourceUse) {
									for (SimpleReusableResource rr : mcc.getCurrentReusableResourcesUsedByActivity((Activity)v)) {
//										System.out.println("--->" + (Activity)v);
										rr.removeUsage((Activity)v);
									}
								}
							}
						}
						
						groundActSolver.removeVariables(actsToBeremoved.toArray(new Activity[actsToBeremoved.size()]));
						//hybridPlanner.clearResolvers();
						
						//it is deleting all the allocation of resource in the previous failed backtrack search
						FluentBasedSimpleDomain causalReasoner = null;
						for (int j = 0; j < hybridPlanner.getMetaConstraints().length; j++){ 
							if(hybridPlanner.getMetaConstraints()[j] instanceof FluentBasedSimpleDomain ){
								causalReasoner = (FluentBasedSimpleDomain)hybridPlanner.getMetaConstraints()[j];
//								System.out.println(" @@@@@@@@@ " + mcc.getAllResourceUsageLevel());
								causalReasoner.resetAllResourceAllocation();
								//mcc.activeHeuristic(false);
								break;
							}
						}			
						hybridPlanner.clearResolvers();

						
						for (int j = 0; j < hybridPlanner.getMetaConstraints().length; j++){ 
							if(hybridPlanner.getMetaConstraints()[j] instanceof MetaOccupiedConstraint ){
								MetaOccupiedConstraint mcc = (MetaOccupiedConstraint)hybridPlanner.getMetaConstraints()[j];
//								System.out.println(" @@@@@@@@@ " + mcc.getAllResourceUsageLevel());
//								mcc.activeHeuristic(true);
//								mcc.activeHeuristic(false);
								break;
							}
						}			
						
						
						
						if(hybridPlanner.learningFromFailure()){
							//get Overlapped objects
							Vector<String> overlappedObject = new Vector<String>();
//							overlappedObject.add("atLocation::at_cup1_table1()");
							
							
							HashMap<Activity, Vector<SimpleOperator>> alternativeOperators = new HashMap<Activity, Vector<SimpleOperator>>();
							//extract current robot act rather the placement (e.g., holding)
							for (int i = 0; i < currentSituation.size(); i++) {
								if(currentSituation.get(i).getComponent().compareTo("RobotProprioception") == 0){
									Vector<SimpleOperator> ops = new Vector<SimpleOperator>();
									for (int j = 0; j < causalReasoner.getOperators().length; j++) {
										String head = currentSituation.get(i).getComponent() + "::" + (currentSituation.get(i).getSymbolicVariable().getSymbols()[0]);
										if(causalReasoner.getOperators()[j].getHead().compareTo(head) == 0){
											for (int k = 0; k <  causalReasoner.getOperators().length; k++) {
//												System.out.println("@@@@@@"+causalReasoner.getOperators()[j].getRequirementActivities()[0]);
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
//							System.out.println("bestApplicabaleOPrator" + bestApplicableOpertor);
							for (int i = 0; i < bestApplicableOpertor.getRequirementActivities().length; i++) {
								
								String operatorHead = bestApplicableOpertor.getRequirementActivities()[i];
								String opeatorHeadComponent = operatorHead.substring(0, operatorHead.indexOf("::"));
								String operatorHeadSymbol = operatorHead.substring(operatorHead.indexOf("::")+2, operatorHead.length());
								
								if(opeatorHeadComponent.compareTo("atLocation") == 0){
									Vector<Constraint> cons = new Vector<Constraint>();

									long duration = 1000;
									Activity two = (Activity)((SpatialFluentSolver)hybridPlanner.getConstraintSolvers()[0]).getConstraintSolvers()[1].createVariable(opeatorHeadComponent);
									two.setSymbolicDomain(operatorHeadSymbol);
									two.setMarking(org.metacsp.meta.simplePlanner.SimpleDomain.markings.UNJUSTIFIED);
									
									AllenIntervalConstraint durationHolding = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(duration,APSPSolver.INF));
									durationHolding.setFrom(two);
									durationHolding.setTo(two);
									cons.add(durationHolding);

									AllenIntervalConstraint before= new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, AllenIntervalConstraint.Type.Before.getDefaultBounds());
									before.setFrom(two);
									before.setTo(hybridPlanner.getGoals().get(0));
									cons.add(before);

									
									((SpatialFluentSolver)hybridPlanner.getConstraintSolvers()[0]).getConstraintSolvers()[1].addConstraints(cons.toArray(new Constraint[cons.size()]));

								}
							}
						}						
					}
					System.out.println("TOTAL TIME: " + (Calendar.getInstance().getTimeInMillis()-timeNow1));
				}				
			}
		}
	}

	private SimpleOperator getBestExapansion(Vector<Activity> currentSituation, HashMap<Activity, Vector<SimpleOperator>> alternativeOperators, Vector<String> overlappedObject) {
		
		HashMap<SimpleOperator, Integer> rank = new HashMap<SimpleOperator, Integer>();
		for (Activity activity : alternativeOperators.keySet()) {
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