package multi.TCSP;

import time.Bounds;
import time.SimpleDistanceConstraint;
import framework.Constraint;
import framework.Variable;
import framework.multi.MultiBinaryConstraint;

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
		time.TimePoint tpFrom = (time.TimePoint)((MultiTimePoint)from).getInternalVariables()[0];
		time.TimePoint tpTo = (time.TimePoint)((MultiTimePoint)to).getInternalVariables()[0];
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
