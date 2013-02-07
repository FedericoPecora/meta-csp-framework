/*******************************************************************************
 * Copyright (c) 2010-2013 Federico Pecora <federico.pecora@oru.se>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
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
