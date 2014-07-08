package org.metacsp.spatial.reachability;

import org.metacsp.framework.BinaryConstraint;
import org.metacsp.framework.Constraint;
import org.metacsp.fuzzyAllenInterval.FuzzyAllenIntervalConstraint;
import org.metacsp.spatial.RCC.RCCConstraint;
import org.metacsp.time.qualitative.QualitativeAllenIntervalConstraint.Type;

public class ReachabilityConstraint extends BinaryConstraint{

	public static enum Type {
		none,
		activityReachable,
		basePickingupReachable,
		baseplacingReachable
	}
	protected Type[] types;
	
	public ReachabilityConstraint(Type... types) {
		this.types = types;
		//this.isSensoryRelation = false;
	}
	
	@Override
	public String getEdgeLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object clone() {
		return new ReachabilityConstraint(this.types);
	}

	@Override
	public boolean isEquivalent(Constraint c) {
		// TODO Auto-generated method stub
		return false;
	}
	public String toString() {
		String ret = "[";
		for (int i = 0; i < types.length; i++) {
			ret +="(" + this.getFrom() + ") --" + this.types[i] + "--> (" + this.getTo() + ")"; 
		}
		ret += "]";
		return ret;
	}
}
