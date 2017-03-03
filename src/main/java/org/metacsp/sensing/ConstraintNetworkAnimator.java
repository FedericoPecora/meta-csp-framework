package org.metacsp.sensing;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Logger;

import org.metacsp.dispatching.Dispatcher;
import org.metacsp.dispatching.DispatchingFunction;
import org.metacsp.dispatching.Dispatcher.ACTIVITY_STATE;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiVariable;
import org.metacsp.meta.fuzzyActivity.FuzzyActivityDomain.markings;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.activity.SymbolicVariableActivity;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;

import cern.colt.Arrays;

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
	private ArrayList<PeriodicCallback> pcbs = null;
	private Dispatcher dis = null;
	private boolean paused = false;
	private boolean teardown = false;
	private boolean autoClean = false;

	private HashMap<Controllable,HashMap<Long,String>> controllableValues = new HashMap<Controllable, HashMap<Long,String>>();

	private transient Logger logger = MetaCSPLogging.getLogger(ConstraintNetworkAnimator.class);

	public void addPeriodicCallbacks(PeriodicCallback ... callbacks) {
		if (this.pcbs == null) this.pcbs = new ArrayList<PeriodicCallback>();
		for (PeriodicCallback pc : callbacks) this.pcbs.add(pc);
	}
	
	protected long getCurrentTimeInMillis() {
		return Calendar.getInstance().getTimeInMillis();
	}

	public ConstraintNetworkAnimator(final ActivityNetworkSolver ans, long period, InferenceCallback cb, boolean startPaused) {
		this(ans, period, startPaused);
		this.cb = cb;
	}

	public ConstraintNetworkAnimator(final ActivityNetworkSolver ans, long period, InferenceCallback cb) {
		this(ans, period, cb, false);
	}
	
	public void setInferenceCallback(InferenceCallback cb) {
		this.cb = cb;
	}

	public ConstraintNetworkAnimator(final ActivityNetworkSolver ans, long period) {
		this(ans, period, false);
	}
	
	public void setAutoCleanFinishedVariables(boolean ac) {
		this.autoClean = ac;
	}
	
	public boolean isInState(SymbolicVariableActivity act, ACTIVITY_STATE st) {
		for (SymbolicVariableActivity disAct : dis.getActsInState(st)) {
			if (disAct.equals(act)) return true;
		}
		return false;
	}
	
	public boolean isStarted(SymbolicVariableActivity act) {
		for (SymbolicVariableActivity startedAct : dis.getStartedActs()) {
			if (startedAct.equals(act)) return true;
		}
		return false;
	}

	public boolean isFinished(SymbolicVariableActivity act) {
		for (SymbolicVariableActivity finishedAct : dis.getFinishedActs()) {
			if (finishedAct.equals(act)) return true;
		}
		return false;
	}
		
	private void cleanUp() {
		SymbolicVariableActivity[] finishedActs = this.getDispatcher().getFinishedActs();
		for (int i = 0; i < finishedActs.length; i++) {		
			Variable finishedVar = finishedActs[i].getRootVariable();
			ConstraintSolver varSolver = finishedVar.getConstraintSolver();
			
			if (varSolver.getConstraintNetwork().containsVariable(finishedVar) && !finishedVar.isDependentVariable()) {

				int countCons = 0;
				
				//Remove var constraints
				Constraint[] consToRemove = varSolver.getConstraintNetwork().getIncidentEdgesIncludingDependentVariables(finishedVar);
				varSolver.removeConstraints(consToRemove);
				if (consToRemove != null) countCons += consToRemove.length;
	
				Variable[] depVars = finishedVar.getRecursivelyDependentVariables();
				for (Variable depVar : depVars) {
					
					//Remove constraints of dependent variables
					ConstraintSolver depVarSolver = depVar.getConstraintSolver();
					Constraint[] consToRemoveDepVars = depVar.getConstraintSolver().getConstraintNetwork().getIncidentEdges(depVar);
					depVarSolver.removeConstraints(consToRemoveDepVars);
					if (consToRemoveDepVars != null) countCons += consToRemoveDepVars.length;

					//Remove act constraints of dependent variables
					SymbolicVariableActivity depAct = (SymbolicVariableActivity)((MultiVariable)depVar).getVariablesFromVariableHierarchy(SymbolicVariableActivity.class)[0];
					Constraint[] consToRemoveDepAct = ans.getConstraintNetwork().getIncidentEdges(depAct);
					ans.removeConstraints(consToRemoveDepAct);
					if (consToRemoveDepAct != null) countCons += consToRemoveDepAct.length;
					
					//Notify dispatcher that dep act is no longer there
					this.getDispatcher().removeFinishedVariable(depAct);
				}
					
				//Notify dispatcher that act is no longer there
				this.getDispatcher().removeFinishedVariable(finishedActs[i]);
	
				logger.info("Cleaned up " + depVars.length + " variables and " + countCons + " constraints");
	
				//Remove TE
				varSolver.removeVariable(finishedVar);
			}			
		}
	}

	public ConstraintNetworkAnimator(final ActivityNetworkSolver ans, long period, boolean startPaused) {
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

	@Deprecated
	public void addDispatchingFunctions(ActivityNetworkSolver ans, DispatchingFunction ... dfs) {
		boolean start = false;
		if (this.dis == null) {
			this.dis = new Dispatcher(ans, period);
			start = true;
		}
		for (DispatchingFunction df : dfs) dis.addDispatchingFunction(df.getComponent(), df);
		if (start) dis.start();
	}

	public void addDispatchingFunctions(DispatchingFunction ... dfs) {
		boolean start = false;
		if (this.dis == null) {
			this.dis = new Dispatcher(ans, period);
			start = true;
		}
		for (DispatchingFunction df : dfs) dis.addDispatchingFunction(df.getComponent(), df);
		if (start) dis.start();
	}

	public long getTimeNow() {
		return getCurrentTimeInMillis()-firstTick+originOfTime;
	}
	
	public void setPaused(boolean paused) {
		this.paused = paused;
	}
	
	public void teardown() {
		this.teardown = true;
	}

	public void run() {
		int iteration = 0;
		while (true && !teardown) {
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
						System.out.println("????????? " + Arrays.toString(ans.getConstraintNetwork().getIncidentEdges(future)));
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

					if (this.pcbs != null) {
						for (PeriodicCallback pc : pcbs) pc.callback(timeNow);
					}

					//Remove finished vars
					if (this.autoClean) {
						int finishedVars = this.getDispatcher().getFinishedActs().length;
						if (finishedVars > 0) {
							cleanUp();
						}
					}
					
					//Print iteration number
					logger.info("Iteration " + iteration++ + " @" + timeNow);
				}
			}
		}
		if (this.dis != null) dis.teardown();
		logger.info("Shut down");
	}

    public Dispatcher getDispatcher() {
        return dis;
    }

}
