package org.metacsp.sensing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.logging.Logger;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.meta.simplePlanner.SimpleDomain.markings;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.SymbolicVariableActivity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;

public class Sensor implements Serializable {
	
	private static final long serialVersionUID = -852002916221212114L;
	protected ActivityNetworkSolver ans = null;
	private ConstraintNetwork cn = null;
	protected String name;
	private SymbolicVariableActivity future = null;
	private Activity currentAct = null;
	private AllenIntervalConstraint currentMeetsFuture = null;
	protected ConstraintNetworkAnimator animator = null;
	
	private transient Logger logger = MetaCSPLogging.getLogger(this.getClass());

	public Sensor(String name, ConstraintNetworkAnimator animator) {
		this.animator = animator;
		this.ans = animator.getActivityNetworkSolver();
		this.cn = animator.getConstraintNetwork();
		this.name = name;
		for (Variable timeAct : cn.getVariables("Time")) {
			if (((SymbolicVariableActivity)timeAct).getSymbolicVariable().getSymbols()[0].equals("Future")) future = (SymbolicVariableActivity)timeAct;
		}
	}
	
	public String getName() { return this.name; }
	
	public void modelSensorValue(String value, long timeNow) {
		synchronized(ans) {
			boolean makeNew = false;
			if (currentAct == null) { makeNew = true; }
			else if (currentAct != null) {
				//If it has not changed, do nothing - otherwise:
				if (this.hasChanged(value)) {
					//change value
					AllenIntervalConstraint deadline = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(timeNow,timeNow));
					deadline.setFrom(currentAct.getVariable());
					deadline.setTo(currentAct.getVariable());
					ans.removeConstraint(currentMeetsFuture);
					boolean ret = ans.addConstraint(deadline);
					if (!ret) throw new NetworkMaintenanceError(deadline);
					//if (!ret) throw new NetworkMaintenanceError(future.getTemporalVariable().getEST(),timeNow);
					makeNew = true;
				}
			}
			//First reading or value changed --> make new activity
			if (makeNew) {
				Activity act = this.createNewActivity(value);
				AllenIntervalConstraint rel = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(timeNow,timeNow));
				rel.setFrom(act.getVariable());
				rel.setTo(act.getVariable());
				AllenIntervalConstraint meetsFuture = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets);
				meetsFuture.setFrom(act.getVariable());
				meetsFuture.setTo(future);
				currentAct = act;
				currentMeetsFuture = meetsFuture;
				boolean ret = ans.addConstraints(new Constraint[] {rel,meetsFuture});
				//if (!ret) throw new NetworkMaintenanceError(rel,meetsFuture);
				if (!ret) throw new NetworkMaintenanceError(future.getTemporalVariable().getEST(),timeNow);
				logger.info("" + currentAct);
			}
		}
	}
	
	protected static String parseName(String everything) {
		String ret = everything.substring(everything.indexOf("Sensor")+6);
		ret = ret.substring(0,ret.indexOf(")")).trim();
		return ret;
	}
	
	protected static HashMap<Long,String> parseSensorValue(String everything, long delta) {
		HashMap<Long,String> ret = new HashMap<Long,String>();
		int lastSV = everything.lastIndexOf("SensorValue");
		while (lastSV != -1) {
			int bw = lastSV;
			int fw = lastSV;
			while (everything.charAt(--bw) != '(') { }
			int parcounter = 1;
			while (parcounter != 0) {
				if (everything.charAt(fw) == '(') parcounter++;
				else if (everything.charAt(fw) == ')') parcounter--;
				fw++;
			}
			String element = everything.substring(bw,fw);
			String value = element.substring(element.indexOf("SensorValue")+11).trim();
			long time = Long.parseLong(value.substring(value.indexOf(" "),value.lastIndexOf(")")).trim());
			time += delta;
			value = value.substring(0,value.indexOf(" ")).trim();
			ret.put(time,value);
			everything = everything.substring(0,bw);
			lastSV = everything.lastIndexOf("SensorValue");
		}
		return ret;
	}

	public void postSensorValue(String sensorValue, long time) {
		animator.postSensorValueToDispatch(this, time, sensorValue);
	}

	public void registerSensorTrace(String sensorTraceFile) {
		this.registerSensorTrace(sensorTraceFile, 0);
	}
	
	protected boolean hasChanged(String value) {
		return (!((SymbolicVariableActivity)currentAct.getVariable()).getSymbolicVariable().getSymbols()[0].equals(value));
	}
	
	protected Activity createNewActivity(String value) {
		SymbolicVariableActivity act = (SymbolicVariableActivity)ans.createVariable(this.name);
		act.setSymbolicDomain(value);
		act.setMarking(markings.JUSTIFIED);
		return act;
	}
	
	public void registerSensorTrace(String sensorTraceFile, long delta) {
		String everything = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(sensorTraceFile));
			try {
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();
				while (line != null) {
					if (!line.startsWith("#")) {
						sb.append(line);
						sb.append('\n');
					}
					line = br.readLine();
				}
				everything = sb.toString();
				String name = parseName(everything);
				if (name.equals(this.name)) {
					HashMap<Long,String> sensorValues = parseSensorValue(everything,delta);
					animator.registerSensorValuesToDispatch(this, sensorValues);
				}
			}
			finally { br.close(); }
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
	}

}
