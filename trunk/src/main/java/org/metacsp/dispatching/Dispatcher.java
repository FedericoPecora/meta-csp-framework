package org.metacsp.dispatching;

import java.util.HashMap;

import org.hamcrest.core.IsEqual;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.multi.activity.SymbolicVariableActivity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.Bounds;

public class Dispatcher extends Thread {

	public static enum ACTIVITY_STATE {PLANNED, STARTED, FINISHING, FINISHED, SKIP_BECAUSE_UNIFICATION};
	private ConstraintNetwork cn;
	private ActivityNetworkSolver ans;
	private long period;
	private HashMap<SymbolicVariableActivity,ACTIVITY_STATE> acts;
	private HashMap<SymbolicVariableActivity,AllenIntervalConstraint> overlapFutureConstraints;
	private HashMap<String,DispatchingFunction> dfs;
	private SymbolicVariableActivity future;

	public Dispatcher(ActivityNetworkSolver ans, long period) {
		this.ans = ans;
		cn = ans.getConstraintNetwork();
		this.period = period;
		acts = new HashMap<SymbolicVariableActivity, ACTIVITY_STATE>();
		overlapFutureConstraints = new HashMap<SymbolicVariableActivity, AllenIntervalConstraint>();
		dfs = new HashMap<String, DispatchingFunction>();
		for (Variable var : cn.getVariables()) {
			if (var instanceof SymbolicVariableActivity) {
				SymbolicVariableActivity candidateFuture = (SymbolicVariableActivity)var;
				if (candidateFuture.getSymbolicVariable().getSymbols()[0].equals("Future")) {
					future = candidateFuture;
					break;
				}
			}
		}
	}

	private boolean equivalentActivities(SymbolicVariableActivity act1, SymbolicVariableActivity act2) {
		if (!act1.getComponent().equals(act2.getComponent())) return false;
		if (!act1.getSymbolicVariable().getSymbols()[0].equals(act2.getSymbolicVariable().getSymbols()[0])) return false;
		if (act1.getTemporalVariable().getEST() != act2.getTemporalVariable().getEST()) return false;
		if (act1.getTemporalVariable().getEET() != act2.getTemporalVariable().getEET()) return false;
		return true;
	}

	public void run() {
		while (true) {
			try { Thread.sleep(period); }
			catch (InterruptedException e) { e.printStackTrace(); }

			synchronized(ans) {
				for (String component : dfs.keySet()) {
					for (Variable var : cn.getVariables(component)) {
						if (var instanceof SymbolicVariableActivity) {
							SymbolicVariableActivity act = (SymbolicVariableActivity)var;
							if (dfs.get(component).skip(act)) continue;
							
							//New act, tag as not dispatched
							if (!acts.containsKey(act)) {
								boolean skip = false;
								//... but test if activity is a unification - if so, ignore it!
								Constraint[] outgoing = ans.getConstraintNetwork().getOutgoingEdges(act);
								for (Constraint con : outgoing) {
									if (con instanceof AllenIntervalConstraint) {
										AllenIntervalConstraint aic = (AllenIntervalConstraint)con;
										SymbolicVariableActivity to = (SymbolicVariableActivity)aic.getTo();
										if (to.getComponent().equals(act.getComponent()) && to.getSymbolicVariable().getSymbols()[0].equals(act.getSymbolicVariable().getSymbols()[0]) && aic.getTypes()[0].equals(AllenIntervalConstraint.Type.Equals)) {
											skip = true;
											System.out.println("IGNORED UNIFICATION " + aic);
											break;
										}
									}
								}
								if (!skip) acts.put(act, ACTIVITY_STATE.PLANNED);
								else acts.put(act, ACTIVITY_STATE.SKIP_BECAUSE_UNIFICATION);
							}

							//Not dispatched, check if need to dispatch
							if (acts.get(act).equals(ACTIVITY_STATE.PLANNED)) {
								//time to dispatch, do it!
								if (act.getTemporalVariable().getEST() < future.getTemporalVariable().getEST()) {
									acts.put(act, ACTIVITY_STATE.STARTED);
									AllenIntervalConstraint overlapsFuture = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Overlaps);
									overlapsFuture.setFrom(act);
									overlapsFuture.setTo(future);
									boolean ret = ans.addConstraint(overlapsFuture);
									if(!ret){
										System.out.println("IGNORED: " + act);
									}
									else {
										overlapFutureConstraints.put(act, overlapsFuture);
										this.dfs.get(component).dispatch(act);
									}

								}
							}

							//If finished, tag as finished
							else if (acts.get(act).equals(ACTIVITY_STATE.FINISHING)) {
								acts.put(act, ACTIVITY_STATE.FINISHED);
								ans.removeConstraint(overlapFutureConstraints.get(act));
								AllenIntervalConstraint deadline = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(future.getTemporalVariable().getEST(),future.getTemporalVariable().getEST()));
								deadline.setFrom(act);
								deadline.setTo(act);
								if (!ans.addConstraint(deadline)) System.out.println("++++++++++++++++++++ SHIT: " + act);
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

	public void finish(SymbolicVariableActivity act) { acts.put(act, ACTIVITY_STATE.FINISHING); }
	
	public ConstraintNetwork getConstraintNetwork() {
		return ans.getConstraintNetwork();
	}
}
