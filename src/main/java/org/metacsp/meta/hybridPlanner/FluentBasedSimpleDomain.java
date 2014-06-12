package org.metacsp.meta.hybridPlanner;

import java.util.HashMap;
import java.util.Vector;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.meta.simplePlanner.SimpleDomain;
import org.metacsp.meta.simplePlanner.SimpleDomain.markings;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.spatioTemporal.SpatialFluentSolver;


public class FluentBasedSimpleDomain extends SimpleDomain {
	
	private long timeNow = -1;
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
	
	private String getParameter(Variable task) {
		
		String ret = "";
		String sym = ((Activity)task).getSymbolicVariable().getSymbols()[0];
		
		if(sym.contains("hold")){
			ret = sym.substring(sym.indexOf("_")+1, sym.indexOf("("));
		}
		else if(sym.contains("sensing")){
			ret = sym.substring(sym.indexOf("_")+1, sym.indexOf("("));
		}
		else{
			String first_ = sym.substring(sym.indexOf("_")+1, sym.length());
			ret = first_.substring(0, first_.indexOf("_"));
		}
		
		return ret;
	}

	@Override
	public ConstraintSolver getGroundSolver() {
		return ((SpatialFluentSolver)metaCS.getConstraintSolvers()[0]).getConstraintSolvers()[1];
	}
	

	public void updateTimeNow(long timeNow) {
		this.timeNow = timeNow;
	}
}
