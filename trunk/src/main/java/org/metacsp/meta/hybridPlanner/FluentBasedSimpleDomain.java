package org.metacsp.meta.hybridPlanner;

import java.util.HashMap;
import java.util.Vector;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.framework.meta.MetaVariable;
import org.metacsp.meta.simplePlanner.PlanningOperator;
import org.metacsp.meta.simplePlanner.SimpleDomain;
import org.metacsp.meta.simplePlanner.SimpleOperator;
import org.metacsp.meta.simplePlanner.SimpleDomain.markings;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.spatioTemporal.SpatialFluentSolver;
import org.metacsp.spatial.reachability.ConfigurationVariable;
import org.metacsp.spatial.reachability.ReachabilityContraintSolver;


public class FluentBasedSimpleDomain extends SimpleDomain {
	
	private long timeNow = -1;
	private boolean activeFreeArmHeuristic = false;
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
	public ConstraintNetwork[] getMetaVariables() {
		
		ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)getGroundSolver();//(ActivityNetworkSolver)this.metaCS.getConstraintSolvers()[0];
		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		// for every variable that is marked as UNJUSTIFIED a ConstraintNetwork is built
		Vector<Variable> tasks = new Vector<Variable>();
		HashMap<Variable, String> oprParameter = new HashMap<Variable, String>();
		for (Variable task : groundSolver.getVariables()) {
			if (task.getMarking().equals(markings.UNJUSTIFIED)) {
				tasks.add(task);
				oprParameter.put(task, getParameter(task));
			}
		}
		
		HashMap<ConstraintNetwork, Integer> sortedConflict = new HashMap<ConstraintNetwork, Integer>();
		if(((SimpleHybridPlanner)this.metaCS).getConflictRanking() != null){
			for (Variable task : oprParameter.keySet()) {
				ConstraintNetwork nw = new ConstraintNetwork(null);
				nw.addVariable(task);
				if(((SimpleHybridPlanner)this.metaCS).getConflictRanking().get(oprParameter.get(task)) != null)
					sortedConflict.put(nw, ((SimpleHybridPlanner)this.metaCS).getConflictRanking().get(oprParameter.get(task)));
				else 
					sortedConflict.put(nw, 0);
						
				//create constraint network
			}
//			System.out.println("++++++++++++++++++++++++++++++++++++++++++++");
//			System.out.println(sortedConflict);
			sortedConflict = sortHashMapByValues(sortedConflict);
//			System.out.println("+++++++++++++++++++++++++++++++++++++++");
//			System.out.println(sortedConflict);
//			System.out.println("___________________________________________");
			ret.addAll(sortedConflict.keySet());
		}else{
			for (int i = 0; i < tasks.size(); i++) {
				ConstraintNetwork nw = new ConstraintNetwork(null);
				nw.addVariable(tasks.get(i));
				ret.add(nw);				
			}
		}
		
		
		
		return ret.toArray(new ConstraintNetwork[ret.size()]);
	}
	
	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable) {
		Vector<ConstraintNetwork> retPossibleConstraintNetworks = new Vector<ConstraintNetwork>();
		ConstraintNetwork problematicNetwork = metaVariable.getConstraintNetwork();
		Activity problematicActivity = (Activity)problematicNetwork.getVariables()[0]; 
		
		Vector<ConstraintNetwork> operatorsConsNetwork = new Vector<ConstraintNetwork>();
		Vector<ConstraintNetwork> unificationConsNetwork = new Vector<ConstraintNetwork>();
		
		//If it's a sensor, it needs to be unified
		if (isSensor(problematicActivity.getComponent())) {
			return super.getUnifications(problematicActivity);
		}
		
		
//		System.out.println("+++++++++++++++++++++++++++++++++++++++++");
		//If it's a controllable sensor, it needs to be unified (or expanded, see later) 
		if (isControllable(problematicActivity.getComponent())) {
			ConstraintNetwork[] unifications = getUnifications(problematicActivity);
//			System.out.println("I AM AT LEAST CONTRAOLLABLE");
			if(unifications != null){
//				System.out.println("TRYING: " + problematicActivity);
				for (int i = 0; i < unifications.length; i++) {
					//add if it is not the key and is true					
					Activity unifiedAct = null;
					for (int j = 0; j < unifications[i].getVariables().length; j++) {
						if(!((Activity)unifications[i].getVariables()[j]).equals(problematicActivity))
							unifiedAct = (Activity)unifications[i].getVariables()[j];
					}
					if(!unificationTrack.keySet().contains(unifiedAct)){						
						unificationConsNetwork.add(unifications[i]);
						unificationTrack.put(problematicActivity, unifiedAct);
//						System.out.println("UNIFIED: " + unifiedAct);
					}
					else{						
//						System.out.println("SKIPED: " +unifiedAct);						
					}
				}
			}
		}

//		System.out.println("+++++++++++++++++++++++++++++++++++++++++");
		
		
		

//		//If it's a controllable sensor, it needs to be unified (or expanded, see later) 
//		if (isControllable(problematicActivity.getComponent())) {
//			ConstraintNetwork[] unifications = getUnifications(problematicActivity);
//			if(unifications != null){
//				for (int i = 0; i < unifications.length; i++) {
//					System.out.println(unifications[i]);
//					unificationConsNetwork.add(unifications[i]);
//				}
//			}
//		}
		
		//If it's a context var, it needs to be unified (or expanded, see later) 
		if (isContextVar(problematicActivity.getComponent())) {
			ConstraintNetwork[] unifications = getUnifications(problematicActivity);
			if (unifications != null) {
				System.out.println("lenght: " + unifications.length);
				for (ConstraintNetwork oneUnification : unifications) {
					retPossibleConstraintNetworks.add(oneUnification);
					oneUnification.setAnnotation(2);
				}
			}
		}
		
		
		
		
		//Find all expansions
		for (SimpleOperator r : operators) {
			String problematicActivitySymbolicDomain = problematicActivity.getSymbolicVariable().getSymbols()[0];
			String operatorHead = r.getHead();
			String opeatorHeadComponent = operatorHead.substring(0, operatorHead.indexOf("::"));
			String operatorHeadSymbol = operatorHead.substring(operatorHead.indexOf("::")+2, operatorHead.length());
			if (opeatorHeadComponent.equals(problematicActivity.getComponent())) {
				if (problematicActivitySymbolicDomain.contains(operatorHeadSymbol)) {
					ConstraintNetwork newResolver = super.expandOperator(r,problematicActivity);
					newResolver.setAnnotation(1);
					newResolver.setSpecilizedAnnotation(r);
					operatorsConsNetwork.add(newResolver);
					//retPossibleConstraintNetworks.add(newResolver);					
				}
			}
			
//			System.out.println("__________________________________");
//			System.out.println(operatorsConsNetwork);
//			System.out.println("__________________________________");

			if (r instanceof PlanningOperator) {
				for (String reqState : r.getRequirementActivities()) {
					String operatorEffect = reqState;
					String opeatorEffectComponent = operatorEffect.substring(0, operatorEffect.indexOf("::"));
					String operatorEffectSymbol = operatorEffect.substring(operatorEffect.indexOf("::")+2, operatorEffect.length());
					if (((PlanningOperator)r).isEffect(reqState)) {
						if (opeatorEffectComponent.equals(problematicActivity.getComponent())) {
							if (problematicActivitySymbolicDomain.contains(operatorEffectSymbol)) {
								ConstraintNetwork newResolver = expandOperator(r,problematicActivity);
								newResolver.annotation = r;
								newResolver.setAnnotation(1);
								retPossibleConstraintNetworks.add(newResolver);
							}
						}
					}
				}
			}
		}
		
		activeFreeArmHeuristic = false;
		if(!activeFreeArmHeuristic ){			
			retPossibleConstraintNetworks.addAll(unificationConsNetwork);
			retPossibleConstraintNetworks.addAll(operatorsConsNetwork);				
		}
		else{
			retPossibleConstraintNetworks.addAll(unificationConsNetwork);		
			HashMap<ConstraintNetwork, Integer> sortedResolvers = new HashMap<ConstraintNetwork, Integer>();
			for (int j = 0; j < operatorsConsNetwork.size(); j++) {
				if(operatorsConsNetwork.get(j).getSpecilizedAnnotation() != null)
					sortedResolvers.put(operatorsConsNetwork.get(j), super.operatorsLevels.get(operatorsConsNetwork.get(j).getSpecilizedAnnotation()));
			}
			//sortedResolvers.putAll(rankedUnification);
			sortedResolvers = sortHashMapByValues(sortedResolvers);
//			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
//			System.out.println(sortedResolvers);
//			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
			retPossibleConstraintNetworks.addAll(sortedResolvers.keySet());
		}
		
		//this is hard coded, it has to be unhard coded
		//this is for the situation we have to force unification of manipulation activity with spatial flunets and avoid self unification (like duration)
		//ReachabilityContraintSolver rchCs = (ReachabilityContraintSolver)((SpatialFluentSolver)this.getGroundSolver()).getConstraintSolvers()[2];
//		if(problematicActivity.getSymbolicVariable().getSymbols()[0].contains("at_robot1_manipulationArea")) {
////			System.out.println("*****" + retPossibleConstraintNetworks.get(retPossibleConstraintNetworks.size() - 1));
//			retPossibleConstraintNetworks.removeElementAt(retPossibleConstraintNetworks.size() - 1);
//		}

		
		
//		System.out.println("============================================");
//		System.out.println("..." + retPossibleConstraintNetworks);
//		System.out.println("============================================");
		
		if (!retPossibleConstraintNetworks.isEmpty()) return retPossibleConstraintNetworks.toArray(new ConstraintNetwork[retPossibleConstraintNetworks.size()]);
		else if (isControllable(problematicActivity.getComponent())) {
			ConstraintNetwork nullActivityNetwork = new ConstraintNetwork(null);
			nullActivityNetwork.setSpecilizedAnnotation(false);
			return new ConstraintNetwork[] {nullActivityNetwork};
			
		}
		ConstraintNetwork nullActivityNetwork = new ConstraintNetwork(null);
		return new ConstraintNetwork[] {nullActivityNetwork};
	}
	

	
	private String getParameter(Variable task) {
		
		String ret = "";
		String sym = ((Activity)task).getSymbolicVariable().getSymbols()[0];
		
		if(sym.contains("hold")){
			ret = sym.substring(sym.indexOf("_")+6, sym.indexOf("(")).concat("_table1");
		}
		else if(sym.contains("sensing")){
			ret = sym.substring(sym.indexOf("_")+16, sym.indexOf("("));
		}
		else if(sym.contains("manipulationArea")){
			ret = sym.substring(sym.indexOf("_")+25, sym.indexOf("("));
		}
		else{
			String first_ = sym.substring(sym.indexOf("_")+1, sym.length());
			ret = first_.substring(0, first_.indexOf("_"));
		}
		
//		System.out.println("task: " + task + " -- " + ret);
		
		return ret;
	}

	@Override
	public ConstraintSolver getGroundSolver() {
		return ((SpatialFluentSolver)metaCS.getConstraintSolvers()[0]).getConstraintSolvers()[1];
	}
	

	public void updateTimeNow(long timeNow) {
		this.timeNow = timeNow;
	}
	
	public void activeHeuristic(boolean active){
		this.activeFreeArmHeuristic = active;
	}
}
