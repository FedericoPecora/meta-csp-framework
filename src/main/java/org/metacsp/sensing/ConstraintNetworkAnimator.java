package org.metacsp.sensing;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Logger;

import org.metacsp.dispatching.Dispatcher;
import org.metacsp.dispatching.DispatchingFunction;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.meta.fuzzyActivity.FuzzyActivityDomain.markings;
import org.metacsp.multi.activity.SymbolicVariableActivity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;

public class ConstraintNetworkAnimator extends Thread {

	private ConstraintNetwork cn = null;
	private ActivityNetworkSolver ans = null;
	private SymbolicVariableActivity future = null;
	private long originOfTime;
	private long firstTick;
	private long period;
	private AllenIntervalConstraint currentReleaseFuture = null;
	private HashMap<Sensor,HashMap<Long,String>> sensorValues = new HashMap<Sensor, HashMap<Long,String>>();
	private InferenceCallback cb = null;
	private Dispatcher dis = null;
	private boolean paused = false;

	private HashMap<Controllable,HashMap<Long,String>> controllableValues = new HashMap<Controllable, HashMap<Long,String>>();

	private transient Logger logger = MetaCSPLogging.getLogger(ConstraintNetworkAnimator.class);

	protected long getCurrentTimeInMillis() {
		return Calendar.getInstance().getTimeInMillis();
	}

	public ConstraintNetworkAnimator(ActivityNetworkSolver ans, long period, InferenceCallback cb, boolean startPaused) {
		this(ans, period, startPaused);
		this.cb = cb;
	}

	public ConstraintNetworkAnimator(ActivityNetworkSolver ans, long period, InferenceCallback cb) {
		this(ans, period);
		this.cb = cb;
	}

	public ConstraintNetworkAnimator(ActivityNetworkSolver ans, long period) {
		this(ans,period,false);
	}

	public ConstraintNetworkAnimator(ActivityNetworkSolver ans, long period, boolean startPaused) {
		this.paused = startPaused;
		synchronized(ans) {
			this.ans = ans;
			this.period = period;
			originOfTime = ans.getOrigin();
			firstTick = getCurrentTimeInMillis();
			this.cn = ans.getConstraintNetwork();
			future = (SymbolicVariableActivity)ans.createVariable("Time");
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

	public void postSensorValueToDispatch(Sensor sensor, long time, String value) {
		synchronized(ans) {
			if (!this.sensorValues.keySet().contains(sensor))
				this.sensorValues.put(sensor, new HashMap<Long, String>());
			HashMap<Long, String> sensorVal = this.sensorValues.get(sensor);
			sensorVal.put(time, value);
		}
	}

	public void postControllableValueToDispatch(Controllable controllable, long time, String value) {
		synchronized(ans) {
			if (!this.controllableValues.keySet().contains(controllable))
				this.controllableValues.put(controllable, new HashMap<Long, String>());
			HashMap<Long, String> contrVal = this.controllableValues.get(controllable);
			contrVal.put(time, value);
		}
	}

	public void registerSensorValuesToDispatch(Sensor sensor, HashMap<Long,String> values) {
		this.sensorValues.put(sensor, values);
	}

	public void registerControllableValuesToDispatch(Controllable controllable, HashMap<Long,String> values) {
		this.controllableValues.put(controllable, values);
	}

	public void addDispatchingFunctions(ActivityNetworkSolver ans, DispatchingFunction ... dfs) {
		boolean start = false;
		if (this.dis == null) {
			this.dis = new Dispatcher(ans, period);
			start = true;
		}
		for (DispatchingFunction df : dfs) dis.addDispatchingFunction(df.getComponent(), df);
		if (start) dis.start();
	}

	private long getTimeNow() {
		return getCurrentTimeInMillis()-firstTick+originOfTime;
	}
	
	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	public void run() {
		int iteration = 0;
		while (true) {
			try { Thread.sleep(period); }
			catch (InterruptedException e) { e.printStackTrace(); }

			if (!paused) {
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

					//If there is a registered InferenceCallback (e.g., call a planner), run it
					if (this.cb != null) cb.doInference(timeNow);

					//Print iteration number
					logger.info("Iteration " + iteration++ + " @" + timeNow);
				}
			}
		}
	}

    public Dispatcher getDispatcher() {
        return dis;
    }

}
