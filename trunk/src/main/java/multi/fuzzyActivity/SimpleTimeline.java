package multi.fuzzyActivity;

import java.util.LinkedHashMap;

import time.APSPSolver;
import time.Bounds;
import framework.Variable;

public class SimpleTimeline {
	
	private LinkedHashMap<Bounds, Variable> mapping;
	private String component;
	
	public SimpleTimeline(String component) {
		this.component = component;
		mapping = new LinkedHashMap<Bounds,Variable>();
	}
	
	public void addVariable(Variable v) {
		//Interval i = new Interval(null, -APSPSolver.INF, APSPSolver.INF);
		Bounds i = new Bounds(-APSPSolver.INF, APSPSolver.INF);
		this.mapping.put(i, v);
	}
	
	public String toString() {
		String ret = "[" + this.component + "]";
		//for (Interval i : this.mapping.keySet()) {
		for (Bounds i : this.mapping.keySet()) {
			ret += (" " + i + " " + mapping.get(i));
		}
		return ret;
	}
	
	public LinkedHashMap<Bounds, Variable> getMapping() {
		return mapping;
	}
	
	public long getStart(Variable v) {
		//for (Interval i : mapping.keySet()) {
		for (Bounds i : mapping.keySet()) {
			if (mapping.get(i).equals(v)) {
				//return (Long)i.chooseValue("ET");
				return i.min;
			}
		}
		return -1;
	}
	
	public long getEnd(Variable v) {
		for (Bounds i : mapping.keySet()) {
			if (mapping.get(i).equals(v)) {
				//return (Long)i.chooseValue("LT");
				return i.max;
			}
		}
		return -1;
	}
	
	public void setStart(Variable v, long t) {
		Bounds toReplace = null;
		Bounds toRemove = null;
		for (Bounds i : mapping.keySet()) {
			if (mapping.get(i).equals(v)) {
				toRemove = i;
				//toReplace = new Interval(null, t, (Long)toRemove.chooseValue("LT"));
				toReplace = new Bounds(t, toRemove.max);
			}
		}
		if (toRemove != null) {
			mapping.remove(toRemove);
			mapping.put(toReplace, v);
		}
		//else mapping.put(new Interval(null, t, APSPSolver.INF), v);
		else mapping.put(new Bounds(t, APSPSolver.INF), v);
	}

	public void setEnd(Variable v, long t) {
		Bounds toReplace = null;
		Bounds toRemove = null;
		for (Bounds i : mapping.keySet()) {
			if (mapping.get(i).equals(v)) {
				toRemove = i;
				toReplace = new Bounds (toRemove.min, t);
			}
		}
		if (toRemove != null) {
			mapping.remove(toRemove);
			mapping.put(toReplace, v);
		}
		else mapping.put(new Bounds(-APSPSolver.INF, t), v);
	}
}
