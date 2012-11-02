package meta.simplePlanner;

import meta.symbolsAndTime.Schedulable;
import multi.activity.Activity;
import framework.Constraint;
import framework.ConstraintNetwork;
import framework.ValueOrderingH;
import framework.VariableOrderingH;



// For the moment just look at that like a capacity with associated a set of activities:
// this class comes from Schedulable that implements sophisticated methods to 
// detect peaks in resource consumption
public class SimpleReusableResource extends Schedulable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7860488618227112837L;
	private int capacity;
	private SimpleDomain rd;
	private String name;
	
	public SimpleReusableResource(VariableOrderingH varOH, ValueOrderingH valOH, int capacity, SimpleDomain rd, String name) {
		super(varOH, valOH);
		this.capacity = capacity;
		this.rd = rd;
		this.name = name;
	}

	@Override
	public boolean isConflicting(Activity[] peak) {
		int sum = 0;
		for (Activity act : peak) {
			sum += rd.getResourceUsageLevel(this, act);
			if (sum > capacity) return true;
		}
		return false;
	}

	@Override
	public void draw(ConstraintNetwork network) {
		// TODO Auto-generated method stub
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "SimpleReusableResource " + name + ", capacity = " + capacity;
	}

	@Override
	public String getEdgeLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEquivalent(Constraint c) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
