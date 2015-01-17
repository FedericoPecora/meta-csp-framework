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
package org.metacsp.meta.symbolsAndTime;

import java.util.Arrays;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.activity.Timeline;

public class SymbolicTimeline extends Timeline {

	private Object[] values = null;
	
	public class ArrayOfStrings {
		private String[] strings;
		public ArrayOfStrings(String[] strings) {
			this.strings = strings;
		}
		public String toString() { return Arrays.toString(strings); }
		public String[] getStrings() { return strings; }
		public void union(String[] other) {
			String[] ret = new String[this.strings.length+other.length];
			for (int i = 0; i < this.strings.length; i++) ret[i] = this.strings[i];
			for (int i = this.strings.length; i < this.strings.length+other.length; i++) ret[i] = other[i-this.strings.length];
			this.strings = ret;
		}
	}
	
	public SymbolicTimeline(ConstraintNetwork an, String component) {
		super(an, component);
		cacheValues();
	}
	
	@Deprecated
	public SymbolicTimeline(ActivityNetworkSolver ans, String component) {
		super(ans, component);
		cacheValues();
		// TODO Auto-generated constructor stub
	}
	
	private void cacheValues() {
		ArrayOfStrings[] ret = new ArrayOfStrings[getPulses().length];
		for (int i = 0; i < getPulses().length-1; i++) {
			for (Variable var : getConstraintNetwork().getVariables(component)) {
				//SymbolicVariableActivity act = (SymbolicVariableActivity)var;
				Activity act = (Activity)var;
				if (act.getTemporalVariable().getEST() <= getPulses()[i] && act.getTemporalVariable().getEET() >= getPulses()[i+1]) {
					//String[] dom = act.getSymbolicVariable().getSymbols();
					String[] dom = act.getSymbols();
					if (ret[i] == null) ret[i] = new ArrayOfStrings(dom);
					else ret[i].union(dom);
				}
			}
		}
		values = ret;		
	}

	@Override
	public Object[] getValues()  { return values; }

	@Override
	public boolean isUndetermined(Object o) {
		return (o == null);
	}

//	@Override
//	public boolean isCritical(Object o) {
//		if (o instanceof SymbolicDomain) return (((SymbolicDomain)o).getSymbols().length == 1);
//		return false;
//	}
//
//	@Override
//	public boolean isInconsistent(Object o) {
//		if (o instanceof SymbolicDomain) return (((SymbolicDomain)o).getSymbols().length == 0);
//		return false;
//	}

	@Override
	public boolean isCritical(Object o) {
		if (o instanceof ArrayOfStrings) return (((ArrayOfStrings)o).getStrings().length == 1);
		return false;
	}

	@Override
	public boolean isInconsistent(Object o) {
		if (o instanceof ArrayOfStrings) return (((ArrayOfStrings)o).getStrings().length == 0);
		return false;
	}


}
