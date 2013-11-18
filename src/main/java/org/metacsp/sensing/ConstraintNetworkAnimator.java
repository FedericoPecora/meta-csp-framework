package org.metacsp.sensing;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Logger;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.meta.fuzzyActivity.FuzzyActivityDomain.markings;
import org.metacsp.meta.simplePlanner.ProactivePlanningDomain;
import org.metacsp.meta.simplePlanner.SimplePlanner;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;

public class ConstraintNetworkAnimator extends Thread {
	
	private ConstraintNetwork cn = null;
	private ActivityNetworkSolver ans = null;
	private Activity future = null;
	private long originOfTime;
	private long period;
	private AllenIntervalConstraint currentReleaseFuture = null;
	private HashMap<Sensor,HashMap<Long,String>> sensorValues = new HashMap<Sensor, HashMap<Long,String>>();
	private SimplePlanner planner = null;
	private ProactivePlanningDomain domain = null;
	
	private transient Logger logger = MetaCSPLogging.getLogger(this.getClass());
	
	public ConstraintNetworkAnimator(SimplePlanner planner, boolean realClock, long period) {
		this((ActivityNetworkSolver)planner.getConstraintSolvers()[0],realClock,period);
		this.planner = planner;
		MetaConstraint[] metaConstraints = planner.getMetaConstraints();
		for (MetaConstraint mc : metaConstraints) {
			if (mc instanceof ProactivePlanningDomain) {
				domain = (ProactivePlanningDomain) mc;
				break;
			}
		}
	}
	
	public ConstraintNetworkAnimator(ActivityNetworkSolver ans, boolean realClock, long period) {
		synchronized(ans) {
			this.ans = ans;
			
			this.period = period;
			if (!realClock) originOfTime = Calendar.getInstance().getTimeInMillis();
			else originOfTime = ans.getOrigin();
			
			this.cn = ans.getConstraintNetwork();
			
			future = (Activity)ans.createVariable("Time");
			future.setSymbolicDomain("Future");
			future.setMarking(markings.JUSTIFIED);
			long timeNow = Calendar.getInstance().getTimeInMillis()-originOfTime;
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
	
	public void registerSensorValuesToDispatch(Sensor sensor, HashMap<Long,String> values) {
		this.sensorValues.put(sensor, values);
	}
	
	public void run() {
		int iteration = 0;
		while (true) {
			try { Thread.sleep(period); }
			catch (InterruptedException e) { e.printStackTrace(); }
			
			synchronized(ans) {
				//Update release constraint of Future
				long timeNow = Calendar.getInstance().getTimeInMillis()-originOfTime;
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
				
				//If there is a registered planner, do the planning/context inference
				if (planner != null) {
					logger.info("Iteration " + iteration++);
					domain.resetContextInference();
					planner.clearResolvers();
					planner.backtrack();
					Vector<Activity> oldInference = new Vector<Activity>();
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
							oldInference.add(act);
						}
					}
					if (!oldInference.isEmpty()) {
						domain.setOldInference(oldInference.toArray(new Activity[oldInference.size()]));
					}
				}
			}
		}
	}
	
}
