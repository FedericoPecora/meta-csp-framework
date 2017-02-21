package org.metacsp.dispatching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;

import org.hamcrest.core.IsEqual;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.multi.activity.SymbolicVariableActivity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;

public class Dispatcher extends Thread {

	public static enum ACTIVITY_STATE {PLANNED, STARTED, FINISHING, FINISHED, SKIP_BECAUSE_UNIFICATION};
	private ConstraintNetwork cn;
	private ActivityNetworkSolver ans;
	private long period;
	private HashMap<SymbolicVariableActivity,ACTIVITY_STATE> acts;
	private HashMap<SymbolicVariableActivity,AllenIntervalConstraint> overlapFutureConstraints;
	private HashMap<String,DispatchingFunction> dfs;
	private SymbolicVariableActivity future;
	private Logger logger = MetaCSPLogging.getLogger(this.getClass());
	private boolean teardown = false;
	
	public void teardown() {
		this.teardown = true;
	}

	public Dispatcher(final ActivityNetworkSolver ans, long period) {
		this.ans = ans;
		cn = ans.getConstraintNetwork();
		this.period = period;
		acts = new HashMap<SymbolicVariableActivity, ACTIVITY_STATE>();
		overlapFutureConstraints = new HashMap<SymbolicVariableActivity, AllenIntervalConstraint>();
		dfs = new HashMap<String, DispatchingFunction>();
		for (Variable var : cn.getVariables()) {
			if (var instanceof SymbolicVariableActivity) {
				SymbolicVariableActivity candidateFuture = (SymbolicVariableActivity)var;
				if (candidateFuture.getSymbolicVariable().getSymbols().length > 0 && candidateFuture.getSymbolicVariable().getSymbols()[0].equals("Future")) {
					future = candidateFuture;
					break;
				}
			}
		}
	}
	
	public SymbolicVariableActivity getFuture() {
		return future;
	}
	
	public void removeFinishedVariable(SymbolicVariableActivity toRemove) {
		this.acts.remove(toRemove);
	}

	private boolean equivalentActivities(SymbolicVariableActivity act1, SymbolicVariableActivity act2) {
		if (!act1.getComponent().equals(act2.getComponent())) return false;
		if (!act1.getSymbolicVariable().getSymbols()[0].equals(act2.getSymbolicVariable().getSymbols()[0])) return false;
		if (act1.getTemporalVariable().getEST() != act2.getTemporalVariable().getEST()) return false;
		if (act1.getTemporalVariable().getEET() != act2.getTemporalVariable().getEET()) return false;
		return true;
	}

	public void run() {
		while (true && !teardown) {
			try { Thread.sleep(period); }
			catch (InterruptedException e) { e.printStackTrace(); }

			synchronized(ans) {
				for (String component : dfs.keySet()) {
					Variable[] currentVars = cn.getVariables(component);
					Arrays.sort(currentVars);
					for (Variable var : currentVars) {
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
											logger.warning("IGNORED UNIFICATION " + aic);
											break;
										}
									}
								}
								if (!skip) acts.put(act, ACTIVITY_STATE.PLANNED);
								else acts.put(act, ACTIVITY_STATE.SKIP_BECAUSE_UNIFICATION);
							}

							//Not dispatched, check if need to dispatch
							if (acts.get(act).equals(ACTIVITY_STATE.PLANNED)) {
								//System.out.println("PLANNED: " + act);
								//time to dispatch, do it!
								if (act.getTemporalVariable().getEST() < future.getTemporalVariable().getEST()) {
									acts.put(act, ACTIVITY_STATE.STARTED);
									AllenIntervalConstraint overlapsFuture = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Overlaps);
									overlapsFuture.setFrom(act);
									overlapsFuture.setTo(future);
									boolean ret = ans.addConstraint(overlapsFuture);
									if(!ret){
										logger.warning("IGNORED dispatching (future is at " + future.getTemporalVariable().getEST() + "):\n\t" + act);
										logger.warning("Constraints on ignored activity are:");
										Constraint[] incident = ans.getConstraintNetwork().getIncidentEdges(act);
										for (Constraint c : incident) {
											logger.warning("\t" + c);
										}
										logger.warning(Arrays.toString(currentVars));
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
								if (!ans.addConstraint(deadline)) {
									AllenIntervalConstraint defaultDeadline = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(act.getTemporalVariable().getEET(),act.getTemporalVariable().getEET()));
									defaultDeadline.setFrom(act);
									defaultDeadline.setTo(act);
									ans.addConstraint(defaultDeadline);
									//System.out.println("++++++++++++++++++++ SHIT: " + act + " DAEDLINE AT " + future.getTemporalVariable().getEST());
								}
							}							
						}
					}
				}
			}
		}
		logger.info("Shut down");
	}

	public void addDispatchingFunction(String component, DispatchingFunction df) {
		df.registerDispatcher(this);
		this.dfs.put(component, df);
	}
	
	public SymbolicVariableActivity[] getFinishedActs() {
		ArrayList<SymbolicVariableActivity> ret = new ArrayList<SymbolicVariableActivity>();
		for (SymbolicVariableActivity act : acts.keySet()) {
			if (acts.get(act).equals(ACTIVITY_STATE.FINISHED)) ret.add(act);
		}
		return ret.toArray(new SymbolicVariableActivity[ret.size()]);
	}

	public void finish(SymbolicVariableActivity act) { acts.put(act, ACTIVITY_STATE.FINISHING); }
	
	public ConstraintNetwork getConstraintNetwork() {
		return ans.getConstraintNetwork();
	}

    public DispatchingFunction getDispatchingFunction(String component) {
        return this.dfs.get(component);
    }
}
