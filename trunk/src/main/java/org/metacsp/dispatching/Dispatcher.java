package org.metacsp.dispatching;

import java.util.HashMap;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.meta.hybridPlanner.SimpleHybridPlanner;
import org.metacsp.meta.simplePlanner.SimplePlanner;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.spatioTemporal.SpatialFluentSolver;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;

public class Dispatcher extends Thread {

	public static enum ACTIVITY_STATE {PLANNED, STARTED, FINISHING, FINISHED};
	private ConstraintNetwork cn;
	private ActivityNetworkSolver ans;
	private long period;
	private HashMap<Activity,ACTIVITY_STATE> acts;
	private HashMap<Activity,AllenIntervalConstraint> overlapFutureConstraints;
	private HashMap<String,DispatchingFunction> dfs;
	private Activity future;
	
	public Dispatcher(SimplePlanner planner, long period) {
		ans = (ActivityNetworkSolver)planner.getConstraintSolvers()[0];
		cn = ans.getConstraintNetwork();
		this.period = period;
		acts = new HashMap<Activity, ACTIVITY_STATE>();
		overlapFutureConstraints = new HashMap<Activity, AllenIntervalConstraint>();
		dfs = new HashMap<String, DispatchingFunction>();
		for (Variable var : cn.getVariables()) {
			if (var instanceof Activity) {
				Activity candidateFuture = (Activity)var;
				if (candidateFuture.getSymbolicVariable().getSymbols()[0].equals("Future")) {
					future = candidateFuture;
					break;
				}
			}
		}
	}
	
	public Dispatcher(SimpleHybridPlanner planner, long period) {
		ans = (ActivityNetworkSolver)(((SpatialFluentSolver)planner.getConstraintSolvers()[0]).getConstraintSolvers()[1]);
		cn = ans.getConstraintNetwork();
		this.period = period;
		acts = new HashMap<Activity, ACTIVITY_STATE>();
		overlapFutureConstraints = new HashMap<Activity, AllenIntervalConstraint>();
		dfs = new HashMap<String, DispatchingFunction>();
		for (Variable var : cn.getVariables()) {
			if (var instanceof Activity) {
				Activity candidateFuture = (Activity)var;
				if (candidateFuture.getSymbolicVariable().getSymbols()[0].equals("Future")) {
					future = candidateFuture;
					break;
				}
			}
		}
	}
	
	public void run() {
		while (true) {
			try { Thread.sleep(period); }
			catch (InterruptedException e) { e.printStackTrace(); }
			
			synchronized(ans) {
				for (String component : dfs.keySet()) {
					for (Variable var : cn.getVariables(component)) {
						if (var instanceof Activity) {
							Activity act = (Activity)var;
							
							//New act, tag as not dispatched
							if (!acts.containsKey(act)) acts.put(act, ACTIVITY_STATE.PLANNED);
							
							//Not dispatched, check if need to dispatch
							if (acts.get(act).equals(ACTIVITY_STATE.PLANNED)) {
								//time to dispatch, do it!
								if (act.getTemporalVariable().getEST() < future.getTemporalVariable().getEST()) {
									acts.put(act, ACTIVITY_STATE.STARTED);
									AllenIntervalConstraint overlapsFuture = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Overlaps);
									overlapsFuture.setFrom(act);
									overlapsFuture.setTo(future);
									boolean ret = ans.addConstraint(overlapsFuture);
									System.out.println("ADD OVL: " + ret);
									overlapFutureConstraints.put(act, overlapsFuture);
									this.dfs.get(component).dispatch(act);
								}
							}
							
							//If finished, tag as finished
							else if (acts.get(act).equals(ACTIVITY_STATE.FINISHING)) {
								acts.put(act, ACTIVITY_STATE.FINISHED);
								ans.removeConstraint(overlapFutureConstraints.get(act));
								AllenIntervalConstraint deadline = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(future.getTemporalVariable().getEST(),future.getTemporalVariable().getEST()));
								deadline.setFrom(act);
								deadline.setTo(act);
								ans.addConstraint(deadline);
							}
						}
					}
				}
			
			}
			
		}
	}
	
	public void addDispatchingFunction(String component, DispatchingFunction df) {
		df.registerDispatcher(this);
		this.dfs.put(component, df);
	}
	
	public void finish(Activity act) { acts.put(act, ACTIVITY_STATE.FINISHING); }
}
