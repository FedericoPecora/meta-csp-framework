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
package org.metacsp.multi.TCSP;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiBinaryConstraint;
import org.metacsp.time.Bounds;
import org.metacsp.time.SimpleDistanceConstraint;
import org.metacsp.time.TimePoint;

public class DistanceConstraint extends MultiBinaryConstraint {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7330355364374859352L;
	private Bounds[] intervals;
	
	public DistanceConstraint(Bounds... intervals) {
		this.intervals = intervals;
		if (intervals.length != 1) this.setPropagateLater();
		else this.setPropagateImmediately();
	}

	@Override
	protected Constraint[] createInternalConstraints(Variable from, Variable to) {
		SimpleDistanceConstraint[] ret = new SimpleDistanceConstraint[intervals.length];
		TimePoint tpFrom = (TimePoint)((MultiTimePoint)from).getInternalVariables()[0];
		TimePoint tpTo = (TimePoint)((MultiTimePoint)to).getInternalVariables()[0];
		for (int i = 0; i < intervals.length; i++) {
			SimpleDistanceConstraint con = new SimpleDistanceConstraint();
			con.setFrom(tpFrom);
			con.setTo(tpTo);
			con.setMinimum(intervals[i].min);
			con.setMaximum(intervals[i].max);
			ret[i] = con;
		}
		return ret;
	}

	@Override
	public Object clone() {
		return new DistanceConstraint(intervals);
	}

	@Override
	public String getEdgeLabel() {
		String ret = "";
		for (Bounds in : intervals) ret += in.toString();
		return ret;
	}
	
	public Bounds[] getBounds() {
		return intervals;
	}

	@Override
	public boolean isEquivalent(Constraint c) {
		DistanceConstraint dc = (DistanceConstraint)c;
		if (!(dc.getFrom().equals(this.getFrom()) && dc.getTo().equals(this.getTo()))) return false;
		for (Bounds t : this.getBounds()) {
			boolean found = false;
			for (Bounds t1 : dc.getBounds()) {
				if (t.equals(t1)) {
					found = true;
					break;
				}
				if (!found) return false;
			}
		}
		return true;
	}

}
